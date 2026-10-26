package com.axenvoir.sktplus

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.axenvoir.sktplus.notification.NotificationHelper
import com.axenvoir.sktplus.notification.ReminderScheduler
import com.axenvoir.sktplus.ui.main.MainScreen
import com.axenvoir.sktplus.ui.theme.SktPlusTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                notificationHelper.createChannel()
                reminderScheduler.scheduleDailyReminder()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationHelper.createChannel()
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
