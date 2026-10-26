package com.axenvoir.sktplus.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.axenvoir.sktplus.data.local.ProductDao
import com.axenvoir.sktplus.domain.util.EXPIRY_THRESHOLD_DAYS
import com.axenvoir.sktplus.domain.util.today
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val productDao: ProductDao,
    private val notificationHelper: NotificationHelper,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val todayEpochDay = today().toEpochDay()
            val count = productDao.countExpiringSoon(
                todayEpochDay = todayEpochDay,
                thresholdDays = EXPIRY_THRESHOLD_DAYS,
            )
            if (count > 0) {
                val items = productDao.topExpiringNames(
                    todayEpochDay = todayEpochDay,
                    thresholdDays = EXPIRY_THRESHOLD_DAYS,
                )
                val summary = items.joinToString { item ->
                    "${item.name} (${item.daysLeft}d)"
                }
                notificationHelper.showExpiryReminder(count = count, topNames = listOf(summary))
            }
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "daily_expiry_reminder"
        private const val MAX_RETRIES = 3
    }
}
