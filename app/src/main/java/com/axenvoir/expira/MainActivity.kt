package com.axenvoir.expira

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.axenvoir.expira.ads.AdsManager
import com.axenvoir.expira.notification.NotificationHelper
import com.axenvoir.expira.notification.ReminderHealthStore
import com.axenvoir.expira.notification.ReminderScheduleException
import com.axenvoir.expira.ui.settings.LanguageManager
import com.axenvoir.expira.notification.ReminderScheduler
import com.axenvoir.expira.ui.main.MainScreen
import com.axenvoir.expira.ui.theme.ExpiraPalettes
import com.axenvoir.expira.ui.theme.ExpiraTheme
import com.axenvoir.expira.ui.theme.PaletteOwnershipValidator
import com.axenvoir.expira.ui.theme.ThemeMode
import com.axenvoir.expira.ui.theme.ThemePreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    @Inject
    lateinit var reminderHealthStore: ReminderHealthStore

    @Inject
    lateinit var languageManager: LanguageManager

    @Inject
    lateinit var themePreferences: ThemePreferences

    @Inject
    lateinit var paletteOwnershipValidator: PaletteOwnershipValidator

    @Inject
    lateinit var adsManager: AdsManager

    private val crashRecoveryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        startPostRecoveryStartupTasks()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        val crashedLastRun = CrashReporter.consumeCrashMarker(this)
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
        languageManager.applyOnStartup()
        enableEdgeToEdge()
        setContent {
            val themeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val dynamicColor by themePreferences.dynamicColor.collectAsState(initial = false)
            val paletteId by themePreferences.paletteId.collectAsState(initial = ExpiraPalettes.GROVE)
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            ExpiraTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, paletteId = paletteId) {
                MainScreen()
            }
        }
        if (crashedLastRun) {
            crashRecoveryLauncher.launch(Intent(this, CrashDisplayActivity::class.java))
        } else {
            lifecycleScope.launch { paletteOwnershipValidator.validateSavedSelection() }
            startPostRecoveryStartupTasks()
        }
    }

    private fun startPostRecoveryStartupTasks() {
        runCatching { notificationHelper.createChannel() }
            .onFailure { Log.w(TAG, "Notification channel could not be initialized", it) }
        lifecycleScope.launch {
            try {
                val generation = reminderScheduler.scheduleDailyReminder()
                reminderHealthStore.clearFailuresBeforeGeneration(generation)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: ReminderScheduleException) {
                reminderScheduler.withCurrentGeneration(failure.generation) {
                    reminderHealthStore.markTerminalFailure(failure.generation)
                }
                Log.e(TAG, "Reminder schedule could not be persisted", failure)
            }
        }
        adsManager.requestConsentAtLaunch(this)
    }

    private companion object {
        const val TAG = "ExpiraMain"
    }
}
