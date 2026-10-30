package com.axenvoir.expira.notification

import android.content.Context
import android.util.Log
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
    private val reminderHealthStore: ReminderHealthStore,
    private val reminderScheduler: ReminderScheduler,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val hour = inputData.getInt(INPUT_HOUR, ReminderScheduler.DEFAULT_HOUR)
        val minute = inputData.getInt(INPUT_MINUTE, ReminderScheduler.DEFAULT_MINUTE)
        val workSlot = inputData.getString(INPUT_WORK_SLOT) ?: PRIMARY_SLOT
        val generation = inputData.getLong(INPUT_GENERATION, LEGACY_GENERATION)

        // Pre-generation requests remain valid only until the first new schedule is created.
        if (!isCurrent(generation)) return Result.success()

        var notificationDelivered = false
        return try {
            currentCoroutineContext().ensureActive()
            if (!notificationHelper.canPostNotifications()) {
                rescheduleIfCurrent(hour, minute, workSlot, generation)
                return Result.success()
            }

            val zoneId = ZoneId.systemDefault()
            val now = ZonedDateTime.now(zoneId)
            if (now.toLocalTime().isBefore(LocalTime.of(hour, minute))) {
                // A time-zone change can make the old one-time delay fire before today's target.
                rescheduleIfCurrent(hour, minute, workSlot, generation)
                return Result.success()
            }

            val todayEpochDay = today(zoneId).toEpochDay()
            val count = productDao.countExpiringSoon(
                todayEpochDay = todayEpochDay,
                thresholdDays = EXPIRY_THRESHOLD_DAYS,
            )
            currentCoroutineContext().ensureActive()
            if (!isCurrent(generation)) return Result.success()

            if (count > 0) {
                val items = productDao.topExpiringNames(
                    todayEpochDay = todayEpochDay,
                    thresholdDays = EXPIRY_THRESHOLD_DAYS,
                )
                currentCoroutineContext().ensureActive()
                val deliveryResult = reminderScheduler.withCurrentGeneration(generation) {
                    notificationHelper.showExpiryReminder(
                        count = count,
                        topNames = items.map { item ->
                            "${item.name} (${applicationContext.getString(R.string.reminder_days_short, item.daysLeft)})"
                        },
                    )
                } ?: return Result.success()

                if (!deliveryResult) {
                    if (!notificationHelper.canPostNotifications()) {
                        rescheduleIfCurrent(hour, minute, workSlot, generation)
                        return Result.success()
                    }
                    throw IllegalStateException("Expiry reminder notification was not delivered")
                }
                notificationDelivered = true
            }

            currentCoroutineContext().ensureActive()
            rescheduleIfCurrent(hour, minute, workSlot, generation)
            Result.success()
        } catch (cancellation: CancellationException) {
            // CoroutineWorker cancellation must remain cancellation, not a retry.
            throw cancellation
        } catch (exception: Exception) {
            if (!isCurrent(generation)) return Result.success()

            if (notificationDelivered) {
                // Never repeat a delivered alert just because the next-day enqueue failed.
                val successorScheduled = try {
                    reminderScheduler.scheduleNextReminder(hour, minute, workSlot, generation)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (successorFailure: Exception) {
                    Log.e(TAG, "Reminder delivered, but its successor could not be enqueued", successorFailure)
                    false
                }
                if (!successorScheduled) {
                    markFailureIfCurrent(generation)
                } else {
                    reminderScheduler.withCurrentGeneration(generation) {
                        reminderHealthStore.clearTerminalFailureForGeneration(generation)
                    }
                }
                return Result.success()
            }

            if (runAttemptCount < MAX_RETRIES) return Result.retry()
            if (!markFailureIfCurrent(generation)) return Result.success()

            // Do not create a retry loop, but keep future daily reminders alive if possible.
            try {
                reminderScheduler.scheduleNextReminder(hour, minute, workSlot, generation)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (successorFailure: Exception) {
                Log.e(TAG, "Terminal reminder failure; successor enqueue also failed", successorFailure)
            }
            Log.e(TAG, "Reminder work reached its retry limit", exception)
            Result.failure()
        }
    }

    private suspend fun rescheduleIfCurrent(
        hour: Int,
        minute: Int,
        currentWorkSlot: String,
        generation: Long,
    ): Boolean {
        val scheduled = reminderScheduler.scheduleNextReminder(
            hour = hour,
            minute = minute,
            currentWorkSlot = currentWorkSlot,
            generation = generation,
        )
        if (scheduled) {
            reminderScheduler.withCurrentGeneration(generation) {
                reminderHealthStore.clearTerminalFailureForGeneration(generation)
            }
        }
        return scheduled
    }

    private fun markFailureIfCurrent(generation: Long): Boolean =
        reminderScheduler.withCurrentGeneration(generation) {
            reminderHealthStore.markTerminalFailure(generation)
        } != null

    private fun isCurrent(generation: Long): Boolean =
        reminderScheduler.isCurrentGeneration(generation)

    companion object {
        const val UNIQUE_WORK_NAME = "daily_expiry_reminder"
        internal const val CONTINUATION_WORK_NAME = "daily_expiry_reminder_continuation"
        internal const val PRIMARY_SLOT = "primary"
        internal const val CONTINUATION_SLOT = "continuation"
        internal const val INPUT_HOUR = "reminder_hour"
        internal const val INPUT_MINUTE = "reminder_minute"
        internal const val INPUT_WORK_SLOT = "reminder_work_slot"
        internal const val INPUT_GENERATION = "reminder_generation"
        private const val LEGACY_GENERATION = 0L
        private const val MAX_RETRIES = 3
        private const val TAG = "ExpiraReminder"
    }
}
