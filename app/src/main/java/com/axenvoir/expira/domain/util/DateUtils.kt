package com.axenvoir.expira.domain.util

import com.axenvoir.expira.domain.model.ExpiryStatus
import java.time.LocalDate

const val EXPIRY_THRESHOLD_DAYS = 3L

fun today(): LocalDate = LocalDate.now()

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
