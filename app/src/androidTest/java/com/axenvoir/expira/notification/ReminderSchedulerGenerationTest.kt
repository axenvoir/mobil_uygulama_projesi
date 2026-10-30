package com.axenvoir.expira.notification

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderSchedulerGenerationTest {
    private lateinit var context: Context
    private lateinit var scheduler: ReminderScheduler

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        scheduler = ReminderScheduler(context)
    }

    @After
    fun tearDown() {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun advancing_generation_invalidates_previous_work_token() {
        assertThat(scheduler.isCurrentGeneration(0L)).isTrue()
        val first = scheduler.advanceGeneration()
        assertThat(scheduler.isCurrentGeneration(first)).isTrue()

        val second = scheduler.advanceGeneration()
        assertThat(second).isGreaterThan(first)
        assertThat(scheduler.isCurrentGeneration(first)).isFalse()
        assertThat(scheduler.isCurrentGeneration(second)).isTrue()
    }

    private companion object {
        const val PREFERENCES_NAME = "reminder_schedule_preferences"
    }
}
