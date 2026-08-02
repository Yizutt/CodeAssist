package dev.ide.agent

import kotlinx.serialization.Serializable

/**
 * Metadata summary of a persisted agent session — everything except the full message history.
 * Used for the session list so we never have to load every session's whole transcript just to show
 * the picker.
 */
@Serializable
data class AgentSessionMeta(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val provider: String,
    val model: String,
    val messageCount: Int,
)

/**
 * A persisted agent conversation: the session's metadata plus the complete [LlmMessage] history the
 * model was (or will be) sent. One JSON file on disk per session ([SessionStore]).
 *
 * [title] is auto-generated from the first user message at creation and can be renamed by the user.
 * [provider] / [model] are captured at creation so a restored session routes to the same backend.
 */
@Serializable
data class AgentSession(
    val id: String,
    var title: String,
    val createdAt: Long,
    var updatedAt: Long,
    val provider: String,
    val model: String,
    val messages: List<LlmMessage>,
) {
    /** Strip the history for the lightweight list view. */
    fun toMeta(): AgentSessionMeta = AgentSessionMeta(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt,
        provider = provider,
        model = model,
        messageCount = messages.size,
    )
}
