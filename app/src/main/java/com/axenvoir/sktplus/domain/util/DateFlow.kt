package com.axenvoir.sktplus.domain.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Duration
import java.time.LocalDateTime

fun todayFlow(): Flow<java.time.LocalDate> = flow {
    while (true) {
        val now = LocalDateTime.now()
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
        emit(now.toLocalDate())
        delay(Duration.between(now, nextMidnight).toMillis())
    }
}
