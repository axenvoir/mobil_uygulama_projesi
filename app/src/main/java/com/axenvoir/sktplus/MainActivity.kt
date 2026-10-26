package com.axenvoir.sktplus

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.axenvoir.sktplus.notification.NotificationHelper
import com.axenvoir.sktplus.ui.settings.LanguageManager
import com.axenvoir.sktplus.notification.ReminderScheduler
import com.axenvoir.sktplus.ui.main.MainScreen
import com.axenvoir.sktplus.ui.theme.SktPlusTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    @Inject
    lateinit var languageManager: LanguageManager

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                notificationHelper.createChannel()
                reminderScheduler.scheduleDailyReminder()
            } else {
                val intent = android.content.Intent(
                    android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                ).apply {
                    putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName)
                }
                runCatching { startActivity(intent) }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationHelper.createChannel()
        languageManager.applyOnStartup()
        enableEdgeToEdge()
        setContent {
            SktPlusTheme {
                MainScreen()
            }
        }
        requestNotificationPermissionIfNeeded()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            reminderScheduler.scheduleDailyReminder()
        }
    }
}
