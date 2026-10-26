package com.expira.app.tr.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.expira.app.tr.MainActivity
import com.expira.app.tr.R
import com.expira.app.tr.data.local.AppDatabase
import com.expira.app.tr.data.model.ExpiryStatus
import java.time.LocalDate
import java.util.concurrent.TimeUnit

object NotificationHelper {
    const val CHANNEL_ID = "expira_expiry_channel"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showExpiryNotification(context: Context, approachingCount: Int, expiredCount: Int) {
        if (approachingCount == 0 && expiredCount == 0) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val title = "🌿 Expira SKT Hatırlatıcısı"
        val message = when {
            expiredCount > 0 && approachingCount > 0 ->
                "$expiredCount ürünün süresi doldu, $approachingCount ürünün süresi yaklaşıyor!"
            expiredCount > 0 ->
                "$expiredCount ürününüzün son kullanma tarihi geçti! Lütfen kontrol edin."
            else ->
                "$approachingCount ürününüzün son kullanma tarihi 3 gün içinde doluyor!"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun scheduleDailyCheck(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<DailyExpiryWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "expira_daily_expiry_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

class DailyExpiryWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(context)
            val products = database.productDao().getAllProductsSync()

            var approaching = 0
            var expired = 0

            products.forEach { entity ->
                val domain = entity.toDomain()
                when (domain.status) {
                    ExpiryStatus.EXPIRED -> expired++
                    ExpiryStatus.APPROACHING -> approaching++
                    ExpiryStatus.FRESH -> {}
                }
            }

            if (approaching > 0 || expired > 0) {
                NotificationHelper.showExpiryNotification(context, approaching, expired)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
