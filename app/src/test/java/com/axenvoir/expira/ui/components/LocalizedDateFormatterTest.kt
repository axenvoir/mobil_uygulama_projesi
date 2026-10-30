package com.axenvoir.expira.ui.components

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import java.time.format.FormatStyle
import java.util.Locale
import org.junit.Test

class LocalizedDateFormatterTest {
    @Test
    fun medium_date_uses_the_requested_app_locale() {
        val date = LocalDate.of(2026, 6, 15)
        val english = localizedDateFormatter(Locale.US, FormatStyle.MEDIUM).format(date)
        val turkish = localizedDateFormatter(Locale.forLanguageTag("tr-TR"), FormatStyle.MEDIUM).format(date)

        assertThat(english).isNotEqualTo(turkish)
    }
}
