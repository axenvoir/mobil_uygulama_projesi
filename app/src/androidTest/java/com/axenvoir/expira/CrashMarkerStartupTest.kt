package com.axenvoir.expira

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.google.common.truth.Truth.assertThat
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CrashMarkerStartupTest {
    @Test
    fun crash_marker_opens_recovery_activity_before_normal_startup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val marker = File(context.noBackupFilesDir, "crash_last.txt")
        marker.writeText("simulated previous-process crash")
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        var scenario: ActivityScenario<MainActivity>? = null

        try {
            scenario = ActivityScenario.launch(MainActivity::class.java)
            instrumentation.waitForIdleSync()
            val recoveryActivityResumed = AtomicBoolean(false)
            instrumentation.runOnMainSync {
                recoveryActivityResumed.set(
                    ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(Stage.RESUMED)
                        .any { it is CrashDisplayActivity },
                )
            }

            assertThat(recoveryActivityResumed.get()).isTrue()
            assertThat(marker.exists()).isFalse()
        } finally {
            scenario?.close()
            instrumentation.runOnMainSync {
                ActivityLifecycleMonitorRegistry.getInstance()
                    .getActivitiesInStage(Stage.RESUMED)
                    .filterIsInstance<CrashDisplayActivity>()
                    .forEach { it.finish() }
            }
            marker.delete()
        }
    }
}
