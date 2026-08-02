package dev.ide.ui.i18n

import kotlinx.serialization.Serializable

/**
 * Application-wide language / locale support. [current] is the active locale key (e.g. "en", "zh"),
 * persisted through [set]. [t] looks up a [Tr] for the current locale, falling back to English and
 * then to the key itself.
 */
object Lang {
    private const val PREF_KEY = "settings.app.language"
    private var current: String = "en"
    private var prefs: ((String) -> String?)? = null
    private var setter: ((String, String) -> Unit)? = null

    /** Attach persistence. Call once at backend init. */
    fun init(read: (String) -> String?, write: (String, String) -> Unit) {
        prefs = read
        setter = write
        current = read(PREF_KEY) ?: "en"
    }

    fun get(): String = current

    fun set(locale: String) {
        current = locale
        setter?.invoke(PREF_KEY, locale)
    }

    fun available(): List<Pair<String, String>> = listOf(
        "en" to "English",
        "zh" to "中文",
    )

    /** Look up a translatable string for the current locale. */
    fun t(tr: Tr): String = tr.map[current] ?: tr.map["en"] ?: tr.key

    /** A single translatable string: a key plus per-locale translations. */
    data class Tr(val key: String, val map: Map<String, String>) {
        constructor(key: String, en: String, zh: String) : this(key, mapOf("en" to en, "zh" to zh))
    }

    // --- common strings --------------------------------------------------------------------------

    val chat_title = Tr("chat_title", "AI", "AI助手")
    val chat_new = Tr("chat_new", "New", "新会话")
    val chat_close = Tr("chat_close", "Close", "关闭")
    val chat_placeholder = Tr("chat_placeholder", "Ask anything…", "输入问题…")
    val chat_send = Tr("chat_send", "Send", "发送")
    val chat_stop = Tr("chat_stop", "Stop", "停止")
    val chat_thinking = Tr("chat_thinking", "Thinking…", "思考中…")
    val chat_need_key = Tr("chat_need_key", "Add an API key to use the agent.", "请添加 API 密钥。")
    val chat_add_key = Tr("chat_add_key", "Add key", "添加密钥")
    val chat_manage_keys = Tr("chat_manage_keys", "Manage keys", "管理密钥")
    val chat_retry = Tr("chat_retry", "Retry", "重试")
    val chat_copy = Tr("chat_copy", "Copy", "复制")
    val chat_copied = Tr("chat_copied", "Copied!", "已复制")
    val chat_empty_title = Tr("chat_empty_title", "How can I help?", "我能帮你做什么？")
    val chat_empty_body = Tr("chat_empty_body", "Describe a task and I'll use tools to get it done.", "描述任务，我会使用工具来完成。")
    val chat_mode_ask = Tr("chat_mode_ask", "Ask", "询问")
    val chat_mode_auto = Tr("chat_mode_auto", "Auto", "自动")
    val chat_mode_plan = Tr("chat_mode_plan", "Plan", "只规划")
    val chat_base_url = Tr("chat_base_url", "Base URL", "服务器地址")
    val chat_api_key = Tr("chat_api_key", "API Key", "API 密钥")
    val chat_connected = Tr("chat_connected", "Connected", "已连接")
    val chat_show = Tr("chat_show", "Show", "显示")
    val chat_hide = Tr("chat_hide", "Hide", "隐藏")
    val chat_done = Tr("chat_done", "Done", "完成")
    val chat_cancel = Tr("chat_cancel", "Cancel", "取消")
    val chat_ca_cert = Tr("chat_ca_cert", "CA Certificate", "CA 证书")
    val chat_ca_cert_hint = Tr("chat_ca_cert_hint", "Paste a PEM cert to trust for this provider.", "粘贴要信任的 PEM 证书。")
    val chat_antigravity_signin = Tr("chat_antigravity_signin", "Sign in with Google", "使用 Google 登录")
    val chat_antigravity_waiting = Tr("chat_antigravity_waiting", "Waiting for sign-in…", "等待登录…")
    val chat_antigravity_warning = Tr("chat_antigravity_warning", "Experimental: may violate ToS.", "实验性功能：可能违反服务条款。")
    val chat_gateway_hint = Tr("chat_gateway_hint", "For OpenRouter, Ollama, LiteLLM…", "用于 OpenRouter、Ollama、LiteLLM…")
    val settings_title = Tr("settings_title", "Settings", "设置")
    val settings_language = Tr("settings_language", "Language", "语言")
    val settings_general = Tr("settings_general", "General", "通用")
    val settings_agent = Tr("settings_agent", "AI Agent", "AI 助手")
    val settings_max_iterations = Tr("settings_max_iterations", "Tool rounds", "工具轮次")
    val settings_max_tokens = Tr("settings_max_tokens", "Max tokens", "最大 Token")
    val settings_thinking_budget = Tr("settings_thinking_budget", "Thinking budget", "思考预算")
}

/** Shorthand for looking up a [Lang.Tr]. */
val Lang.Tr.t: String get() = Lang.t(this)
