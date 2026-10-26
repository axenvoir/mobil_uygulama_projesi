package com.axenvoir.sktplus.domain.util

import com.axenvoir.sktplus.domain.model.ExpiryStatus
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {

    private val reference = LocalDate.of(2025, 6, 15)

    @Test
    fun `daysUntil returns positive value for future date`() {
        val expiry = reference.plusDays(10)
        assertThat(daysUntil(expiry, reference)).isEqualTo(10)
    }

    @Test
    fun `daysUntil returns zero for same day`() {
        assertThat(daysUntil(reference, reference)).isEqualTo(0)
    }

    @Test
    fun `daysUntil returns negative value for past date`() {
        val expiry = reference.minusDays(5)
        assertThat(daysUntil(expiry, reference)).isEqualTo(-5)
    }

    @Test
    fun `expiry status is EXPIRED for past date`() {
        val expiry = reference.minusDays(1)
        assertThat(expiryStatus(expiry, reference)).isEqualTo(ExpiryStatus.EXPIRED)
    }

    @Test
    fun `expiry status is EXPIRING_SOON for today`() {
        assertThat(expiryStatus(reference, reference)).isEqualTo(ExpiryStatus.EXPIRING_SOON)
    }

    @Test
    fun `expiry status is EXPIRING_SOON exactly at threshold`() {
        val expiry = reference.plusDays(EXPIRY_THRESHOLD_DAYS)
        assertThat(expiryStatus(expiry, reference)).isEqualTo(ExpiryStatus.EXPIRING_SOON)
    }

    @Test
    fun `expiry status is FRESH one day after threshold`() {
        val expiry = reference.plusDays(EXPIRY_THRESHOLD_DAYS + 1)
        assertThat(expiryStatus(expiry, reference)).isEqualTo(ExpiryStatus.FRESH)
    }

    @Test
    fun `expiry status is FRESH for far future date`() {
        val expiry = reference.plusDays(365)
        assertThat(expiryStatus(expiry, reference)).isEqualTo(ExpiryStatus.FRESH)
    }

    @Test
    fun `day boundary is stable across month end`() {
        val endOfMonth = LocalDate.of(2025, 1, 31)
        val firstOfNext = LocalDate.of(2025, 2, 1)
        assertThat(daysUntil(firstOfNext, endOfMonth)).isEqualTo(1)
    }
}
