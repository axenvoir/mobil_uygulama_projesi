package com.axenvoir.expira

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.axenvoir.expira.ads.AdsManager
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

    @Inject
    lateinit var adsManager: AdsManager

    private val crashRecoveryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        adsManager.requestConsentAtLaunch(this)
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
            val dynamicColor by themePreferences.dynamicColor.collectAsState(initial = false)
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            ExpiraTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
                MainScreen()
            }
        }
        reminderScheduler.scheduleDailyReminder()
        if (CrashReporter.consumeCrashMarker(this)) {
            crashRecoveryLauncher.launch(Intent(this, CrashDisplayActivity::class.java))
        } else {
            adsManager.requestConsentAtLaunch(this)
        }
    }
}
