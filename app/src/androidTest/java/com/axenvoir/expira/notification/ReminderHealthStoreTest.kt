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
class ReminderHealthStoreTest {
    private lateinit var context: Context
    private lateinit var store: ReminderHealthStore

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        store = ReminderHealthStore(context)
    }

    @After
    fun tearDown() {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun terminal_failure_survives_store_instance_recreation_and_can_be_cleared_for_its_generation() {
        store.markTerminalFailure(7L)
        assertThat(store.hasTerminalFailure.value).isTrue()

        val recreated = ReminderHealthStore(context)
        assertThat(recreated.hasTerminalFailure.value).isTrue()

        recreated.clearTerminalFailureForGeneration(7L)
        assertThat(recreated.hasTerminalFailure.value).isFalse()
    }

    @Test
    fun stale_generation_cannot_clear_or_replace_a_newer_terminal_failure() {
        store.markTerminalFailure(9L)
        store.clearTerminalFailureForGeneration(8L)
        store.clearFailuresBeforeGeneration(9L)
        assertThat(store.hasTerminalFailure.value).isTrue()

        store.clearFailuresBeforeGeneration(10L)
        assertThat(store.hasTerminalFailure.value).isFalse()
    }

    private companion object {
        const val PREFERENCES_NAME = "reminder_health_preferences"
    }
}
