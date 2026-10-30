package com.axenvoir.expira.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    /**
     * Schedules the first reminder at the next local occurrence of the target
     * time. A one-time request is used intentionally: a 24-hour periodic
     * request drifts by an hour when the device crosses DST.
     */
    fun scheduleDailyReminder(hour: Int = DEFAULT_HOUR, minute: Int = DEFAULT_MINUTE) {
        val zoneId = ZoneId.systemDefault()
        WorkManager.getInstance(context)
            .cancelUniqueWork(ReminderWorker.CONTINUATION_WORK_NAME)
        enqueueReminder(
            workName = ReminderWorker.UNIQUE_WORK_NAME,
            workSlot = ReminderWorker.PRIMARY_SLOT,
            hour = hour,
            minute = minute,
            zoneId = zoneId,
            clock = Clock.system(zoneId),
            policy = ExistingWorkPolicy.REPLACE,
        )
    }

    /**
     * Enqueues the next local-day occurrence after a worker has completed.
     * Alternating work names keeps this continuation independent from the
     * currently running request, including when that request reaches failure.
     */
    internal fun scheduleNextReminder(
        hour: Int,
        minute: Int,
        currentWorkSlot: String,
    ) {
        val nextSlot = when (currentWorkSlot) {
            ReminderWorker.PRIMARY_SLOT -> ReminderWorker.CONTINUATION_SLOT
            ReminderWorker.CONTINUATION_SLOT -> ReminderWorker.PRIMARY_SLOT
            else -> ReminderWorker.CONTINUATION_SLOT
        }
        val zoneId = ZoneId.systemDefault()
        enqueueReminder(
            workName = workNameFor(nextSlot),
            workSlot = nextSlot,
            hour = hour,
            minute = minute,
            zoneId = zoneId,
            clock = Clock.system(zoneId),
            policy = ExistingWorkPolicy.REPLACE,
        )
    }

    private fun enqueueReminder(
        workName: String,
        workSlot: String,
        hour: Int,
        minute: Int,
        zoneId: ZoneId,
        clock: Clock,
        policy: ExistingWorkPolicy,
    ) {
        val initialDelay = computeInitialDelay(clock, hour, minute, zoneId)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(initialDelay)
            .setInputData(
                workDataOf(
                    ReminderWorker.INPUT_HOUR to hour,
                    ReminderWorker.INPUT_MINUTE to minute,
                    ReminderWorker.INPUT_WORK_SLOT to workSlot,
                ),
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(workName, policy, request)
    }

    companion object {
        const val DEFAULT_HOUR = 9
        const val DEFAULT_MINUTE = 0

        /**
         * Computes elapsed time to the next valid local target time. The
         * calculation is instant-based, so it remains correct through DST
         * gaps/overlaps and at an exact target-time boundary.
         */
        internal fun computeInitialDelay(
            now: ZonedDateTime,
            hour: Int,
            minute: Int,
        ): Duration {
            val targetTime = LocalTime.of(hour, minute)
            var nextRun = now.toLocalDate().atTime(targetTime).atZone(now.zone)
            if (!nextRun.isAfter(now)) {
                nextRun = now.toLocalDate().plusDays(1).atTime(targetTime).atZone(now.zone)
            }
            val delay = Duration.between(now, nextRun)
            return delay
        }

        internal fun computeInitialDelay(
            clock: Clock,
            hour: Int,
            minute: Int,
            zoneId: ZoneId = clock.zone,
        ): Duration = computeInitialDelay(
            now = ZonedDateTime.now(clock.withZone(zoneId)),
            hour = hour,
            minute = minute,
        )

        private fun workNameFor(workSlot: String): String = when (workSlot) {
            ReminderWorker.PRIMARY_SLOT -> ReminderWorker.UNIQUE_WORK_NAME
            else -> ReminderWorker.CONTINUATION_WORK_NAME
        }
    }
}
