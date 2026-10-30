package com.axenvoir.expira.domain.util

import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive

/**
 * Emits the current local date and then emits again at each following local
 * midnight. The clock and zone are explicit so DST transitions and tests do
 * not depend on the JVM default time zone.
 */
fun todayFlow(
    clock: Clock = Clock.systemDefaultZone(),
    zoneId: ZoneId = clock.zone,
): Flow<LocalDate> = flow {
    val zoneClock = clock.withZone(zoneId)
    while (currentCoroutineContext().isActive) {
        val now = ZonedDateTime.now(zoneClock)
        emit(now.toLocalDate())
        delay(millisUntilNextMidnight(now))
    }
}

/**
 * Re-evaluates the system zone and local date after a wall-clock or timezone
 * event, then resumes midnight updates in the newly active zone.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun systemTodayFlow(
    timeChanges: Flow<Unit>,
    zoneProvider: () -> ZoneId = { ZoneId.systemDefault() },
    clockProvider: (ZoneId) -> Clock = { zoneId -> Clock.system(zoneId) },
): Flow<LocalDate> = timeChanges
    .onStart { emit(Unit) }
    .flatMapLatest {
        val zoneId = zoneProvider()
        todayFlow(clock = clockProvider(zoneId), zoneId = zoneId)
    }

/** Convenience overload for callers that only need to select a zone. */
fun todayFlow(zoneId: ZoneId): Flow<LocalDate> =
    todayFlow(clock = Clock.system(zoneId), zoneId = zoneId)

/** Returns the first valid instant at the next local midnight in [now]'s zone. */
internal fun nextMidnight(now: ZonedDateTime): ZonedDateTime =
    now.toLocalDate().plusDays(1).atStartOfDay(now.zone)

/**
 * Returns the real elapsed time to the next local midnight. This is 23 or 25
 * hours on DST transition days, rather than an assumed 24-hour duration.
 */
internal fun millisUntilNextMidnight(now: ZonedDateTime): Long {
    val duration = Duration.between(now, nextMidnight(now))
    return duration.toMillis().coerceAtLeast(1L)
}
