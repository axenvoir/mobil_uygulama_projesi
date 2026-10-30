package com.axenvoir.expira.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
class ReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val generationPreferences = context.getSharedPreferences(
        GENERATION_PREFERENCES,
        Context.MODE_PRIVATE,
    )

    /**
     * Schedules the first reminder at the next local occurrence of the target
     * time. A one-time request is used intentionally: a 24-hour periodic
     * request drifts by an hour when the device crosses DST.
     *
     * Once replacement begins, finish the cancel/enqueue sequence even if the
     * caller's Activity or ViewModel is cancelled; otherwise the new generation
     * could invalidate the old chain without installing its replacement.
     */
    suspend fun scheduleDailyReminder(
        hour: Int = DEFAULT_HOUR,
        minute: Int = DEFAULT_MINUTE,
    ): Long = withContext(NonCancellable) {
        DAILY_SCHEDULE_MUTEX.withLock {
            try {
                val generation = withContext(Dispatchers.IO) { advanceGeneration() }
                val zoneId = ZoneId.systemDefault()
                WorkManager.getInstance(context)
                    .cancelUniqueWork(ReminderWorker.CONTINUATION_WORK_NAME)
                    .awaitCompletion()
                enqueueReminder(
                    workName = ReminderWorker.UNIQUE_WORK_NAME,
                    workSlot = ReminderWorker.PRIMARY_SLOT,
                    hour = hour,
                    minute = minute,
                    generation = generation,
                    zoneId = zoneId,
                    clock = Clock.system(zoneId),
                    policy = ExistingWorkPolicy.REPLACE,
                ).awaitCompletion()
                generation
            } catch (cancellation: kotlinx.coroutines.CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                throw ReminderScheduleException(currentGeneration(), exception)
            }
        }
    }

    /**
     * Enqueues the next local-day occurrence only while the originating work
     * still belongs to the current schedule generation. This prevents a worker
     * already in flight from recreating an obsolete chain after a reschedule.
     */
    internal suspend fun scheduleNextReminder(
        hour: Int,
        minute: Int,
        currentWorkSlot: String,
        generation: Long,
    ): Boolean {
        val nextSlot = when (currentWorkSlot) {
            ReminderWorker.PRIMARY_SLOT -> ReminderWorker.CONTINUATION_SLOT
            ReminderWorker.CONTINUATION_SLOT -> ReminderWorker.PRIMARY_SLOT
            else -> ReminderWorker.CONTINUATION_SLOT
        }
        val zoneId = ZoneId.systemDefault()
        val operation = synchronized(GENERATION_LOCK) {
            if (!isCurrentGenerationLocked(generation)) {
                null
            } else {
                enqueueReminder(
                    workName = workNameFor(nextSlot),
                    workSlot = nextSlot,
                    hour = hour,
                    minute = minute,
                    generation = generation,
                    zoneId = zoneId,
                    clock = Clock.system(zoneId),
                    policy = ExistingWorkPolicy.REPLACE,
                )
            }
        } ?: return false

        operation.awaitCompletion()
        return isCurrentGeneration(generation)
    }

    private fun enqueueReminder(
        workName: String,
        workSlot: String,
        hour: Int,
        minute: Int,
        generation: Long,
        zoneId: ZoneId,
        clock: Clock,
        policy: ExistingWorkPolicy,
    ): Operation {
        val initialDelay = computeInitialDelay(clock, hour, minute, zoneId)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(initialDelay)
            .setInputData(
                workDataOf(
                    ReminderWorker.INPUT_HOUR to hour,
                    ReminderWorker.INPUT_MINUTE to minute,
                    ReminderWorker.INPUT_WORK_SLOT to workSlot,
                    ReminderWorker.INPUT_GENERATION to generation,
                ),
            )
            .build()
        return WorkManager.getInstance(context).enqueueUniqueWork(workName, policy, request)
    }

    @android.annotation.SuppressLint("UseKtx")
    internal fun advanceGeneration(): Long = synchronized(GENERATION_LOCK) {
        val current = generationPreferences.getLong(KEY_GENERATION, 0L).coerceAtLeast(0L)
        val next = if (current == Long.MAX_VALUE) 1L else current + 1L
        // KTX edit() discards commit()'s Boolean; schedule replacement must fail closed on disk-write failure.
        check(generationPreferences.edit().putLong(KEY_GENERATION, next).commit()) {
            "Could not persist reminder schedule generation"
        }
        next
    }

    internal fun currentGeneration(): Long = synchronized(GENERATION_LOCK) {
        generationPreferences.getLong(KEY_GENERATION, 0L).coerceAtLeast(0L)
    }

    internal fun isCurrentGeneration(generation: Long): Boolean = synchronized(GENERATION_LOCK) {
        isCurrentGenerationLocked(generation)
    }

    internal fun <T> withCurrentGeneration(generation: Long, block: () -> T): T? =
        synchronized(GENERATION_LOCK) {
            if (isCurrentGenerationLocked(generation)) block() else null
        }

    private fun isCurrentGenerationLocked(generation: Long): Boolean =
        generation >= 0L && generationPreferences.getLong(KEY_GENERATION, 0L) == generation

    companion object {
        const val DEFAULT_HOUR = 9
        const val DEFAULT_MINUTE = 0
        private const val GENERATION_PREFERENCES = "reminder_schedule_preferences"
        private const val KEY_GENERATION = "schedule_generation"
        private val GENERATION_LOCK = Any()
        private val DAILY_SCHEDULE_MUTEX = Mutex()

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
            return Duration.between(now, nextRun)
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

class ReminderScheduleException internal constructor(
    val generation: Long,
    cause: Throwable,
) : IllegalStateException("Reminder schedule generation $generation was not accepted", cause)
