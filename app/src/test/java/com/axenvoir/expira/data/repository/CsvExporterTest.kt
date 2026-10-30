package com.axenvoir.expira.data.repository

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CsvExporterTest {
    @Test
    fun formula_prefixes_are_neutralized_even_after_leading_whitespace() {
        assertThat(escapeCsvField("=1+1")).isEqualTo("'=1+1")
        assertThat(escapeCsvField("  +SUM(1, 2)")).isEqualTo("\"'  +SUM(1, 2)\"")
        assertThat(escapeCsvField("-10")).isEqualTo("'-10")
        assertThat(escapeCsvField("@HYPERLINK(\"https://example.test\")"))
            .isEqualTo("\"'@HYPERLINK(\"\"https://example.test\"\")\"")
    }

    @Test
    fun commas_quotes_and_line_breaks_follow_csv_escaping_rules() {
        assertThat(escapeCsvField("Smith, \"Jane\"\r\nApt 2"))
            .isEqualTo("\"Smith, \"\"Jane\"\"\r\nApt 2\"")
    }

    @Test
    fun plain_and_empty_values_are_preserved() {
        assertThat(escapeCsvField("  Milk & bread  ")).isEqualTo("  Milk & bread  ")
        assertThat(escapeCsvField("")).isEmpty()
    }
}
