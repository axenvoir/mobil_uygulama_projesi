package com.axenvoir.expira.domain.util

import com.axenvoir.expira.domain.model.ExpiryStatus
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class FilterTest {

    private val today = LocalDate.of(2025, 6, 15)

    private fun products(): List<LocalDate> = listOf(
        today.minusDays(2),
        today,
        today.plusDays(2),
        today.plusDays(30),
    )

    @Test
    fun `expired filter keeps only past dates`() {
        val expired = products().filter { expiryStatus(it, today) == ExpiryStatus.EXPIRED }
        assertThat(expired).containsExactly(today.minusDays(2))
    }

    @Test
    fun `expiring soon filter keeps threshold range`() {
        val soon = products().filter { expiryStatus(it, today) == ExpiryStatus.EXPIRING_SOON }
        assertThat(soon).containsExactly(today, today.plusDays(2))
    }

    @Test
    fun `fresh filter keeps far dates`() {
        val fresh = products().filter { expiryStatus(it, today) == ExpiryStatus.FRESH }
        assertThat(fresh).containsExactly(today.plusDays(30))
    }
}
