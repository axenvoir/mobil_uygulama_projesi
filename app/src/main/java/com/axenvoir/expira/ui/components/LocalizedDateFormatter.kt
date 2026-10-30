package com.axenvoir.expira.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLocale
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
internal fun rememberLocalizedDateFormatter(style: FormatStyle): DateTimeFormatter {
    val appLocale = LocalLocale.current.platformLocale
    return remember(appLocale, style) {
        localizedDateFormatter(appLocale, style)
    }
}

internal fun localizedDateFormatter(locale: Locale, style: FormatStyle): DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(style).withLocale(locale)
