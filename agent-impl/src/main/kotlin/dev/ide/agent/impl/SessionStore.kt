package dev.ide.agent.impl

import dev.ide.agent.AgentSession
import dev.ide.agent.AgentSessionMeta
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

/**
 * Disk-backed store for agent sessions. Each session is one JSON file named `<id>.json` in [dir].
 * Serialization uses kotlinx.serialization-json (already a dependency of agent-impl); the
 * [AgentSession] model and its [dev.ide.agent.LlmMessage] history are all @Serializable.
 *
 * [list] reads only the metadata (id/title/timestamps/provider/model/messageCount) so the session
 * picker never has to load every session's full transcript. [load] reads the whole file for one session.
 */
class SessionStore(private val dir: File) {

    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    init {
        dir.mkdirs()
    }

    /** Write [session] to disk as `<id>.json`. Creates parent dirs if needed. */
    fun save(session: AgentSession) {
        dir.mkdirs()
        val file = File(dir, "${session.id}.json")
        val encoded = json.encodeToString(AgentSession.serializer(), session)
        // Atomic write: write to temp, then rename so a crash mid-write can't corrupt the existing file.
        val tmp = File(dir, ".${session.id}.tmp")
        tmp.writeText(encoded)
        tmp.renameTo(file)
    }

    /** Read a single session by [id], or null if the file is missing/corrupt. */
    fun load(id: String): AgentSession? = runCatching {
        val file = File(dir, "$id.json")
        if (!file.exists()) return null
        json.decodeFromString(AgentSession.serializer(), file.readText())
    }.getOrNull()

    /** List every session's metadata, newest-first. Skips unreadable files. */
    fun list(): List<AgentSessionMeta> = runCatching {
        dir.listFiles { f -> f.extension == "json" && !f.name.startsWith(".") }
            ?.mapNotNull { file -> runCatching {
                json.decodeFromString(AgentSession.serializer(), file.readText()).toMeta()
            }.getOrNull() }
            ?.sortedByDescending { it.updatedAt }
            ?: emptyList()
    }.getOrDefault(emptyList())

    /** Delete the file for [id]. No-op if it doesn't exist. */
    fun delete(id: String) {
        File(dir, "$id.json").delete()
    }

    /** Rename the session's title without rewriting the whole file (read-modify-save). */
    fun rename(id: String, title: String) {
        val session = load(id) ?: return
        save(session.copy(title = title, updatedAt = System.currentTimeMillis()))
    }

    companion object {
        fun newId(): String = UUID.randomUUID().toString()

        /** The directory an [AgentBackend] uses for its session store — under the app's data dir. */
        fun defaultDir(dataDir: File): File = File(dataDir, "agent-sessions")
    }
}
