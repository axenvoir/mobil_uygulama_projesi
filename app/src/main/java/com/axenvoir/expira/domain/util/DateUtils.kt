package com.axenvoir.expira.domain.util

import com.axenvoir.expira.domain.model.ExpiryStatus
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

const val EXPIRY_THRESHOLD_DAYS = 3L

/**
 * Returns the current date in the device's current zone.
 *
 * Callers that need a reproducible value should use [today] with an explicit
 * [Clock] and/or [ZoneId].
 */
fun today(): LocalDate = today(Clock.systemDefaultZone())

/** Returns the current date in an explicit time zone. */
fun today(zoneId: ZoneId): LocalDate = LocalDate.now(zoneId)

/** Returns the date observed by an explicit clock (including its zone). */
fun today(clock: Clock): LocalDate = LocalDate.now(clock)

/**
 * Returns the date represented by [clock] in [zoneId]. Using [Clock.withZone]
 * prevents the clock's construction zone from changing the date calculation.
 */
fun today(clock: Clock, zoneId: ZoneId): LocalDate = LocalDate.now(clock.withZone(zoneId))

fun daysUntil(expiryDate: LocalDate, reference: LocalDate = today()): Long =
    expiryDate.toEpochDay() - reference.toEpochDay()

fun expiryStatus(expiryDate: LocalDate, reference: LocalDate = today()): ExpiryStatus {
    val days = daysUntil(expiryDate, reference)
    return when {
        days < 0 -> ExpiryStatus.EXPIRED
        days <= EXPIRY_THRESHOLD_DAYS -> ExpiryStatus.EXPIRING_SOON
        else -> ExpiryStatus.FRESH
    }
}
