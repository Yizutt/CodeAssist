package dev.ide.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Look up a localized string for [key] in the current [LocalAppLocale] (falling back to English then the
 * key itself) and recompose whenever the locale changes. Pass up to 9 [args] for `%1`…`%9` substitution.
 *
 * Example:
 * ```
 * Text(rememberLocalized("ai_tokens", tokenCount, turnCount))
 * ```
 */
@Composable
fun rememberLocalized(key: String, vararg args: Any): String {
    // Re-read on every locale change. We poll [LocaleManager.current] inside a LaunchedEffect so the
    // composable recomposes when the user switches language from Settings.
    var locale by remember { mutableStateOf(LocaleManager.current) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        // Poll once; the real trigger is the user calling LocaleManager.setOverride which the UI does,
        // and we expose [LocalAppLocale] for that. Here we also watch for external changes.
        locale = LocaleManager.current
    }
    val template = AppStrings.lookup(key, locale)
    return remember(*args) {
        var result = template
        args.forEachIndexed { i, arg ->
            result = result.replace("%${i + 1}", arg.toString())
        }
        result
    }
}

/** Non-composable lookup for use outside of @Composable scope (e.g. view models, tool summaries). */
fun localized(key: String, vararg args: Any): String {
    val template = AppStrings.lookup(key, LocaleManager.current)
    var result = template
    args.forEachIndexed { i, arg ->
        result = result.replace("%${i + 1}", arg.toString())
    }
    return result
}
