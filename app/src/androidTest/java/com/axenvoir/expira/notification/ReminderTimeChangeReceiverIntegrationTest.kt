package com.axenvoir.expira.notification

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import java.util.concurrent.TimeUnit
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderTimeChangeReceiverIntegrationTest {
    @Test
    fun timezone_change_receiver_enqueues_the_next_reminder() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val workManager = WorkManager.getInstance(context)
        cancelReminderWork(workManager)

        try {
            ReminderTimeChangeReceiver().onReceive(
                context,
                Intent(Intent.ACTION_TIMEZONE_CHANGED),
            )

            val activeStates = setOf(
                WorkInfo.State.ENQUEUED,
                WorkInfo.State.RUNNING,
                WorkInfo.State.BLOCKED,
            )
            val deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(WORK_QUERY_TIMEOUT_SECONDS)
            var hasActiveReminder = false
            while (!hasActiveReminder && System.nanoTime() < deadlineNanos) {
                val primary = workManager
                    .getWorkInfosForUniqueWork(ReminderWorker.UNIQUE_WORK_NAME)
                    .get(WORK_QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                val continuation = workManager
                    .getWorkInfosForUniqueWork(ReminderWorker.CONTINUATION_WORK_NAME)
                    .get(WORK_QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                hasActiveReminder = (primary + continuation).any { it.state in activeStates }
                if (!hasActiveReminder) Thread.sleep(POLL_INTERVAL_MILLIS)
            }
            assertThat(hasActiveReminder).isTrue()
        } finally {
            cancelReminderWork(workManager)
        }
    }

    private fun cancelReminderWork(workManager: WorkManager) {
        workManager.cancelUniqueWork(ReminderWorker.UNIQUE_WORK_NAME)
            .result.get(WORK_QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        workManager.cancelUniqueWork(ReminderWorker.CONTINUATION_WORK_NAME)
            .result.get(WORK_QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    }

    private companion object {
        const val WORK_QUERY_TIMEOUT_SECONDS = 10L
        const val POLL_INTERVAL_MILLIS = 50L
    }
}
