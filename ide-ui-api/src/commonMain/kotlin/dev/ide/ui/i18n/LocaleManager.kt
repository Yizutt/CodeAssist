package dev.ide.ui.i18n

import androidx.compose.runtime.compositionLocalOf

/**
 * Runtime language override for the app. When [override] is null the app follows the system locale
 * (the Compose Resources default). When set to a language code ("en", "zh" …) that locale is used
 * until [setOverride] is called again. [init] wires persistence to the settings backend.
 */
object LocaleManager {
    private const val PREF_KEY = "settings.app.locale"
    @Volatile
    private var override: String? = null
    private var prefsRead: (String) -> String? = { null }
    private var prefsWrite: (String, String) -> Unit = { _, _ -> }

    /** The active locale code, or null to follow the system. */
    val current: String? get() = override

    /** Wire persistence. Call once at backend start-up. */
    fun init(read: (String) -> String?, write: (String, String) -> Unit) {
        prefsRead = read
        prefsWrite = write
        override = read(PREF_KEY)
    }

    /** Set the language override, or null to follow the system. Persists immediately. */
    fun setOverride(locale: String?) {
        override = locale?.takeIf { it.isNotBlank() }
        prefsWrite(PREF_KEY, override ?: "")
    }

    /** All locales the app ships translations for: (code, native name, english name). */
    fun available(): List<Triple<String, String, String>> = listOf(
        Triple("", "System default", "System default"),
        Triple("en", "English", "English"),
        Triple("zh", "中文", "Chinese"),
        Triple("ar", "العربية", "Arabic"),
        Triple("es", "Español", "Spanish"),
        Triple("in", "Bahasa Indonesia", "Indonesian"),
        Triple("pt-BR", "Português (Brasil)", "Portuguese (Brazil)"),
        Triple("ru", "Русский", "Russian"),
    )
}

/** CompositionLocal consumed by [rememberLocalized] to re-read when the locale changes. */
val LocalAppLocale = compositionLocalOf<String?> { null }
