package com.axenvoir.expira.domain.util

import com.axenvoir.expira.domain.model.ExpiryStatus
import com.axenvoir.expira.notification.ReminderScheduler
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

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

    @Test
    fun `today uses the requested zone when the device crosses a date boundary`() {
        val instant = Instant.parse("2025-06-15T23:30:00Z")
        val clock = Clock.fixed(instant, ZoneId.of("UTC"))

        assertThat(today(clock, ZoneId.of("America/Los_Angeles")))
            .isEqualTo(LocalDate.of(2025, 6, 15))
        assertThat(today(clock, ZoneId.of("Asia/Tokyo")))
            .isEqualTo(LocalDate.of(2025, 6, 16))
    }

    @Test
    fun `next midnight uses a real 23 hour spring DST interval`() {
        val zone = ZoneId.of("Europe/Berlin")
        val beforeTransition = ZonedDateTime.of(2025, 3, 30, 0, 0, 0, 0, zone)

        assertThat(millisUntilNextMidnight(beforeTransition))
            .isEqualTo(Duration.ofHours(23).toMillis())
    }

    @Test
    fun `next midnight uses a real 25 hour autumn DST interval`() {
        val zone = ZoneId.of("Europe/Berlin")
        val beforeTransition = ZonedDateTime.of(2025, 10, 26, 0, 0, 0, 0, zone)

        assertThat(millisUntilNextMidnight(beforeTransition))
            .isEqualTo(Duration.ofHours(25).toMillis())
    }

    @Test
    fun `next midnight remains the next local date at a day boundary`() {
        val zone = ZoneId.of("America/New_York")
        val now = ZonedDateTime.of(2025, 1, 31, 23, 59, 59, 0, zone)

        assertThat(nextMidnight(now).toLocalDate()).isEqualTo(LocalDate.of(2025, 2, 1))
    }

    @Test
    fun `reminder delay uses the DST length until the next local target`() {
        val zone = ZoneId.of("Europe/Berlin")
        val now = ZonedDateTime.of(2025, 3, 29, 9, 0, 0, 0, zone)

        assertThat(ReminderScheduler.computeInitialDelay(now, hour = 9, minute = 0))
            .isEqualTo(Duration.ofHours(23))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `system date flow recomputes in the new zone after a clock-change event`() = runTest {
        val timeChanges = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val instant = Instant.parse("2025-06-15T23:30:00Z")
        val zones = listOf(ZoneId.of("UTC"), ZoneId.of("Asia/Tokyo"))
        var zoneIndex = 0
        val observedDates = mutableListOf<LocalDate>()
        val job = launch {
            systemTodayFlow(
                timeChanges = timeChanges,
                zoneProvider = { zones[zoneIndex++] },
                clockProvider = { zone -> Clock.fixed(instant, zone) },
            ).collect { observedDates += it }
        }

        runCurrent()
        assertThat(observedDates).containsExactly(LocalDate.of(2025, 6, 15)).inOrder()

        timeChanges.emit(Unit)
        runCurrent()
        assertThat(observedDates)
            .containsExactly(LocalDate.of(2025, 6, 15), LocalDate.of(2025, 6, 16))
            .inOrder()

        job.cancelAndJoin()
    }

    @Test
    fun `todayFlow emits in its zone and first cancels the infinite flow`() = runTest {
        val clock = Clock.fixed(
            Instant.parse("2025-06-15T23:30:00Z"),
            ZoneId.of("UTC"),
        )

        assertThat(todayFlow(clock = clock, zoneId = ZoneId.of("Asia/Tokyo")).first())
            .isEqualTo(LocalDate.of(2025, 6, 16))
    }
}
