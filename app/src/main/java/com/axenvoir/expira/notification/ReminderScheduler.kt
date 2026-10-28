package com.axenvoir.expira.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun scheduleDailyReminder(hour: Int = DEFAULT_HOUR, minute: Int = DEFAULT_MINUTE) {
        val initialDelay = computeInitialDelay(hour, minute)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(Duration.ofHours(24))
            .setInitialDelay(initialDelay)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ReminderWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    private fun computeInitialDelay(hour: Int, minute: Int): Duration {
        val now = LocalDateTime.now()
        var nextRun = now.toLocalDate().atTime(LocalTime.of(hour, minute))
        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }
        return Duration.between(now, nextRun)
    }

    companion object {
        const val DEFAULT_HOUR = 9
        const val DEFAULT_MINUTE = 0
    }
}
