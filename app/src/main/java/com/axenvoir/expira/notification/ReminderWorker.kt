package com.axenvoir.expira.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.axenvoir.expira.R
import com.axenvoir.expira.data.local.ProductDao
import com.axenvoir.expira.domain.util.EXPIRY_THRESHOLD_DAYS
import com.axenvoir.expira.domain.util.today
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val productDao: ProductDao,
    private val notificationHelper: NotificationHelper,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val hour = inputData.getInt(INPUT_HOUR, ReminderScheduler.DEFAULT_HOUR)
        val minute = inputData.getInt(INPUT_MINUTE, ReminderScheduler.DEFAULT_MINUTE)
        val workSlot = inputData.getString(INPUT_WORK_SLOT) ?: PRIMARY_SLOT

        return try {
            currentCoroutineContext().ensureActive()
            if (!notificationHelper.canPostNotifications()) {
                scheduleNextReminder(hour, minute, workSlot)
                return Result.success()
            }

            val zoneId = ZoneId.systemDefault()
            val now = ZonedDateTime.now(zoneId)
            if (now.toLocalTime().isBefore(LocalTime.of(hour, minute))) {
                // A time-zone change can make the old one-time delay fire
                // before today's local target. Re-plan instead of notifying early.
                scheduleNextReminder(hour, minute, workSlot)
                return Result.success()
            }
            val todayEpochDay = today(zoneId).toEpochDay()
            val count = productDao.countExpiringSoon(
                todayEpochDay = todayEpochDay,
                thresholdDays = EXPIRY_THRESHOLD_DAYS,
            )
            currentCoroutineContext().ensureActive()

            if (count > 0) {
                val items = productDao.topExpiringNames(
                    todayEpochDay = todayEpochDay,
                    thresholdDays = EXPIRY_THRESHOLD_DAYS,
                )
                currentCoroutineContext().ensureActive()
                notificationHelper.showExpiryReminder(
                    count = count,
                    topNames = items.map { item ->
                        "${item.name} (${applicationContext.getString(R.string.reminder_days_short, item.daysLeft)})"
                    },
                )
            }
            currentCoroutineContext().ensureActive()
            scheduleNextReminder(hour, minute, workSlot)
            Result.success()
        } catch (cancellation: CancellationException) {
            // CoroutineWorker cancellation must remain cancellation, not a retry.
            throw cancellation
        } catch (_: Exception) {
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                // Do not create a retry loop, but keep future daily reminders alive.
                try {
                    scheduleNextReminder(hour, minute, workSlot)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    // The current attempt is still terminal even if continuation
                    // enqueueing is unavailable; opening the app schedules it again.
                }
                Result.failure()
            }
        }
    }

    private fun scheduleNextReminder(hour: Int, minute: Int, currentWorkSlot: String) {
        ReminderScheduler(applicationContext).scheduleNextReminder(
            hour = hour,
            minute = minute,
            currentWorkSlot = currentWorkSlot,
        )
    }

    companion object {
        const val UNIQUE_WORK_NAME = "daily_expiry_reminder"
        internal const val CONTINUATION_WORK_NAME = "daily_expiry_reminder_continuation"
        internal const val PRIMARY_SLOT = "primary"
        internal const val CONTINUATION_SLOT = "continuation"
        internal const val INPUT_HOUR = "reminder_hour"
        internal const val INPUT_MINUTE = "reminder_minute"
        internal const val INPUT_ZONE_ID = "reminder_zone_id"
        internal const val INPUT_WORK_SLOT = "reminder_work_slot"
        private const val MAX_RETRIES = 3
    }
}
