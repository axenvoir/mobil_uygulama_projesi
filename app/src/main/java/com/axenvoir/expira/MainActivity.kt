package com.axenvoir.expira

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.result.contract.ActivityResultContracts
import com.axenvoir.expira.notification.NotificationHelper
import com.axenvoir.expira.ui.settings.LanguageManager
import com.axenvoir.expira.notification.ReminderScheduler
import com.axenvoir.expira.ui.main.MainScreen
import com.axenvoir.expira.ui.theme.ExpiraTheme
import com.axenvoir.expira.ui.theme.ThemeMode
import com.axenvoir.expira.ui.theme.ThemePreferences
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

    @Inject
    lateinit var themePreferences: ThemePreferences

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
                provider.iconView?.let { iconView ->
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
                } ?: run {
                    provider.remove()
                }
            }
        }
        notificationHelper.createChannel()
        languageManager.applyOnStartup()
        enableEdgeToEdge()
        setContent {
            val themeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val dynamicColor by themePreferences.dynamicColor.collectAsState(initial = true)
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            ExpiraTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
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
