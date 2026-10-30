package com.axenvoir.expira.notification

import android.annotation.SuppressLint
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.axenvoir.expira.MainActivity
import com.axenvoir.expira.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notification_channel_desc)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun canPostNotifications(): Boolean =
        (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED) &&
            NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun showExpiryReminder(count: Int, topNames: List<String>): Boolean {
        if (count <= 0) return false
        val countText = context.resources.getQuantityString(
            R.plurals.notification_text,
            count,
            count,
        )
        val details = buildString {
            append(countText)
            topNames.filter(String::isNotBlank).forEach { name ->
                append('\n')
                append(name)
            }
        }
        return showNotification(
            notificationId = NOTIFICATION_ID,
            title = context.getString(R.string.notification_title),
            text = countText,
            details = details,
        )
    }

    fun showTestNotification(): Boolean = showNotification(
            notificationId = TEST_NOTIFICATION_ID,
            title = context.getString(R.string.notification_test_title),
            text = context.getString(R.string.notification_test_text),
        )

    @SuppressLint("MissingPermission")
    private fun showNotification(
        notificationId: Int,
        title: String,
        text: String,
        details: String = text,
    ): Boolean {
        return try {
            if (!canPostNotifications()) return false
            createChannel()
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(details))
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setPublicVersion(
                    NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(context.getString(R.string.notification_title))
                        .setContentText(context.getString(R.string.notification_private_content))
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .build(),
                )
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            true
        } catch (exception: SecurityException) {
            Log.w(TAG, "Notification permission was revoked before delivery", exception)
            false
        } catch (exception: Exception) {
            Log.e(TAG, "Notification could not be delivered", exception)
            false
        }
    }

    companion object {
        private const val TAG = "NotificationHelper"
        const val CHANNEL_ID = "expiry_reminders"
        const val NOTIFICATION_ID = 1001
        const val TEST_NOTIFICATION_ID = 1002
    }
}
