package com.axenvoir.sktplus

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.result.contract.ActivityResultContracts
import com.axenvoir.sktplus.notification.NotificationHelper
import com.axenvoir.sktplus.ui.settings.LanguageManager
import com.axenvoir.sktplus.notification.ReminderScheduler
import com.axenvoir.sktplus.ui.main.MainScreen
import com.axenvoir.sktplus.ui.theme.ExpiraTheme
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
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            notificationHelper.createChannel()
            reminderScheduler.scheduleDailyReminder()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splash.setOnExitAnimationListener { provider ->
                val iconView = provider.iconView
                iconView.animate()
                    .scaleX(0.6f)
                    .scaleY(0.6f)
                    .alpha(0f)
                    .setDuration(220L)
                    .setInterpolator(android.view.animation.DecelerateInterpolator())
                    .withEndAction {
                        provider.remove()
                    }
                    .start()
            }
        }
        notificationHelper.createChannel()
        languageManager.applyOnStartup()
        enableEdgeToEdge()
        setContent {
            ExpiraTheme {
                MainScreen()
            }
        }
        requestNotificationPermissionIfNeeded()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationHelper.canPostNotifications()) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        reminderScheduler.scheduleDailyReminder()
    }
}
