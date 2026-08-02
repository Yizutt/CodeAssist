package dev.ide.agent.impl

import dev.ide.agent.AgentEvent
import dev.ide.agent.AgentEventSink
import dev.ide.agent.AgentPermissionGate
import dev.ide.agent.AgentSession
import dev.ide.agent.AgentToolRegistry
import dev.ide.agent.ContentPart
import dev.ide.agent.LlmClient
import dev.ide.agent.LlmMessage
import dev.ide.agent.LlmRequest
import dev.ide.agent.LlmStreamEvent
import dev.ide.agent.PermissionMode
import dev.ide.agent.StopReason
import dev.ide.agent.TokenUsage
import dev.ide.agent.ToolExecutionResult
import dev.ide.agent.WriteRequest
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Drives one conversation: request -> stream a turn -> if the model called tools, execute them (gating
 * mutating calls through [gate]) and feed the results back -> repeat until the model stops calling tools or
 * the iteration cap is hit. History is retained across user turns; [reset] starts a fresh conversation.
 *
 * The loop runs in the caller's coroutine, so cancelling that coroutine stops generation and tool work.
 * [systemPrompt] is a supplier so the host can refresh live project context each turn while keeping the
 * grounding prefix stable.
 *
 * When [store] and [sessionId] are supplied, the loop auto-saves the conversation after every completed
 * turn so it survives process death and can be resumed via [fromHistory].
 */
class AgentLoop(
    private val client: LlmClient,
    private val model: String,
    private val tools: AgentToolRegistry,
    private val gate: AgentPermissionGate,
    private val systemPrompt: () -> String,
    private val maxTokens: Int = 8192,
    private val maxIterations: Int = 24,
    /** Provider reasoning-token cap forwarded to each request; null leaves the model default. */
    private val thinkingBudget: Int? = null,
    /** Trims re-sent tool output so a long task does not re-bill the whole transcript each step. */
    private val compactor: HistoryCompactor = HistoryCompactor(),
    /** When set with [sessionId], auto-saves the conversation after each completed turn. */
    private val store: SessionStore? = null,
    private val sessionId: String? = null,
    private val providerId: String = "",
) {
    private val history = mutableListOf<LlmMessage>()

    fun reset() {
        history.clear()
    }

    suspend fun send(userText: String, sink: AgentEventSink) {
        history += LlmMessage.user(userText)
        sink.emit(AgentEvent.UserMessage(userText))
        runTurns(sink)
    }

    /** True when there is a conversation to resume (used to offer a retry after a failure). */
    fun canResume(): Boolean = history.isNotEmpty()

    /** Re-run the conversation from the current history after a transient failure, WITHOUT adding a new user
     *  turn (a failed turn leaves the user message, and any completed tool results, in place). No-op when
     *  there's nothing to resume. */
    suspend fun retry(sink: AgentEventSink) {
        if (history.isEmpty()) return
        runTurns(sink)
    }

    private suspend fun runTurns(sink: AgentEventSink) {
        var iteration = 0
        while (iteration++ < maxIterations) {
            val request = LlmRequest(
                model = model,
                system = systemPrompt(),
                messages = compactor.compact(history),
                tools = tools.specs(),
                maxTokens = maxTokens,
                thinking = true,
                thinkingBudget = thinkingBudget,
            )
            val turn = Turn()
            client.chat(request).collect { event -> turn.consume(event, sink) }

            turn.failure?.let { sink.emit(AgentEvent.Error(it)); return }

            history += LlmMessage.assistant(turn.assistantParts())
            val calls = turn.toolCalls()
            if (calls.isEmpty()) {
                sink.emit(AgentEvent.TurnCompleted(turn.stopReason, turn.usage))
                persist()
                return
            }

            history += executeCalls(calls, sink)
            persist()
        }
        sink.emit(AgentEvent.Error("Stopped after $maxIterations tool iterations without finishing."))
    }

    /** Write the current conversation to the store (if wired). No-op when [store] is null. */
    private fun persist() {
        val store = store ?: return
        val id = sessionId ?: return
        if (history.isEmpty()) return
        runCatching {
            store.save(
                AgentSession(
                    id = id,
                    title = title(),
                    createdAt = createdAt(),
                    updatedAt = System.currentTimeMillis(),
                    provider = providerId,
                    model = model,
                    messages = history.toList(),
                )
            )
        }
    }

    /** First user message, used as the auto-generated title for a new session. */
    private fun title(): String = history.firstOrNull { it.role == LlmRole.USER }
        ?.content?.filterIsInstance<ContentPart.Text>()?.firstOrNull()?.text?.take(60)
        ?: "New conversation"

    /** Timestamp for createdAt if the session is brand new (no persisted record yet). */
    private fun createdAt(): Long = System.currentTimeMillis()

    /**
     * Runs the turn's tool calls, preserving their order in the returned results. Read-only calls run
     * concurrently (a turn that reads several files pays one file's latency, not the sum); mutating and
     * unknown calls run sequentially afterward so their permission prompts never race and writes stay
     * ordered and deterministic.
     */
    private suspend fun executeCalls(calls: List<ContentPart.ToolUse>, sink: AgentEventSink): List<LlmMessage> {
        if (calls.size == 1) return listOf(executeCall(calls[0], sink))
        val results = arrayOfNulls<LlmMessage>(calls.size)
        coroutineScope {
            calls.forEachIndexed { i, call ->
                val tool = tools.find(call.name)
                if (tool != null && !tool.mutating) {
                    launch { results[i] = executeCall(call, sink) }
                }
            }
        }
        calls.forEachIndexed { i, call ->
            if (results[i] == null) results[i] = executeCall(call, sink)
        }
        return results.map { it!! }
    }

    private suspend fun executeCall(call: ContentPart.ToolUse, sink: AgentEventSink): LlmMessage {
        val tool = tools.find(call.name)
        val args = JsonToolArgs(parseArgsObject(call.arguments))
        if (tool == null) {
            sink.emit(AgentEvent.ToolCallStarted(call.id, call.name, call.name))
            sink.emit(AgentEvent.ToolCallFinished(call.id, ok = false, resultSummary = "unknown tool"))
            return LlmMessage.toolResult(call.id, "Error: unknown tool '${call.name}'.", isError = true)
        }

        val summary = runCatching { tool.summarize(args) }.getOrDefault(call.name)
        sink.emit(AgentEvent.ToolCallStarted(call.id, call.name, summary))

        if (tool.mutating) {
            val allowed = gate.authorize(WriteRequest(call.name, summary, args.optString("path")))
            if (!allowed) {
                val reason = when (gate.mode) {
                    PermissionMode.PLAN_ONLY ->
                        "Plan-only mode is active, so file changes are disabled. Describe the change instead of applying it."
                    else -> "The user declined this change."
                }
                sink.emit(AgentEvent.ToolCallDenied(call.id, reason))
                return LlmMessage.toolResult(call.id, "Denied: $reason", isError = true)
            }
        }

        val result = runCatching { tool.execute(args) }
            .getOrElse { ToolExecutionResult.error(it.message ?: "tool failed") }
        sink.emit(AgentEvent.ToolCallFinished(call.id, ok = !result.isError, resultSummary = brief(result.content)))
        return LlmMessage.toolResult(call.id, result.content, result.isError)
    }

    private fun brief(content: String): String {
        val firstLine = content.lineSequence().firstOrNull().orEmpty().trim()
        return if (firstLine.length > 160) firstLine.take(157) + "..." else firstLine
    }

    /** Accumulates a single streamed turn into an assistant message plus the tool calls to run. */
    private class Turn {
        val text = StringBuilder()
        private val thinkingParts = ArrayList<ContentPart.Thinking>()
        private val toolOrder = ArrayList<String>()
        private val toolById = HashMap<String, ContentPart.ToolUse>()
        var usage: TokenUsage? = null
        var stopReason: StopReason = StopReason.END_TURN
        var failure: String? = null

        suspend fun consume(event: LlmStreamEvent, sink: AgentEventSink) {
            when (event) {
                is LlmStreamEvent.TextDelta -> {
                    text.append(event.text)
                    sink.emit(AgentEvent.AssistantTextDelta(event.text))
                }
                is LlmStreamEvent.ThinkingDelta -> sink.emit(AgentEvent.AssistantThinkingDelta(event.text))
                is LlmStreamEvent.ThinkingCompleted -> thinkingParts += ContentPart.Thinking(event.text, event.signature)
                is LlmStreamEvent.ToolCallCompleted -> {
                    if (event.id !in toolById) toolOrder += event.id
                    toolById[event.id] = ContentPart.ToolUse(event.id, event.name, event.arguments, event.signature)
                }
                is LlmStreamEvent.Usage -> usage = event.usage
                is LlmStreamEvent.Completed -> stopReason = event.stopReason
                is LlmStreamEvent.Failed -> failure = event.message
                is LlmStreamEvent.ToolCallStarted, is LlmStreamEvent.ToolCallArgsDelta -> Unit
            }
        }

        fun toolCalls(): List<ContentPart.ToolUse> = toolOrder.mapNotNull { toolById[it] }

        fun assistantParts(): List<ContentPart> {
            val parts = ArrayList<ContentPart>()
            parts += thinkingParts
            if (text.isNotEmpty()) parts += ContentPart.Text(text.toString())
            parts += toolCalls()
            return parts
        }
    }

    companion object {
        /** Rehydrate a loop from a persisted session so the conversation can continue exactly where it left off. */
        fun fromHistory(
            session: AgentSession,
            client: LlmClient,
            tools: AgentToolRegistry,
            gate: AgentPermissionGate,
            systemPrompt: () -> String,
            store: SessionStore? = null,
            maxTokens: Int = 8192,
            maxIterations: Int = 24,
            thinkingBudget: Int? = null,
            compactor: HistoryCompactor = HistoryCompactor(),
        ): AgentLoop {
            val loop = AgentLoop(
                client = client,
                model = session.model,
                tools = tools,
                gate = gate,
                systemPrompt = systemPrompt,
                maxTokens = maxTokens,
                maxIterations = maxIterations,
                thinkingBudget = thinkingBudget,
                compactor = compactor,
                store = store,
                sessionId = session.id,
                providerId = session.provider,
            )
            loop.history.addAll(session.messages)
            return loop
        }
    }
}
