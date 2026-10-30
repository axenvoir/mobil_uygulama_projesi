package com.axenvoir.expira.notification

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Persists only a coarse local reminder-health flag; no product names or error details are stored. */
@Singleton
class ReminderHealthStore @Inject constructor(
    @param:ApplicationContext context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val _hasTerminalFailure = MutableStateFlow(
        preferences.getBoolean(KEY_TERMINAL_FAILURE, false),
    )
    val hasTerminalFailure: StateFlow<Boolean> = _hasTerminalFailure.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        if (key == KEY_TERMINAL_FAILURE || key == KEY_FAILURE_GENERATION) {
            _hasTerminalFailure.value = prefs.getBoolean(KEY_TERMINAL_FAILURE, false)
        }
    }

    init {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    /** Call only while the scheduler confirms this generation is current. */
    fun markTerminalFailure(generation: Long) = synchronized(HEALTH_LOCK) {
        preferences.edit {
            putLong(KEY_FAILURE_GENERATION, generation)
            putBoolean(KEY_TERMINAL_FAILURE, true)
        }
        _hasTerminalFailure.value = true
    }

    /** A successful new schedule clears only older failures, never its own fast worker failure. */
    fun clearFailuresBeforeGeneration(generation: Long) = synchronized(HEALTH_LOCK) {
        if (
            preferences.getBoolean(KEY_TERMINAL_FAILURE, false) &&
            preferences.getLong(KEY_FAILURE_GENERATION, LEGACY_FAILURE_GENERATION) < generation
        ) {
            clearLocked()
        }
    }

    /** Clear only the terminal marker produced by this same still-current work generation. */
    fun clearTerminalFailureForGeneration(generation: Long) = synchronized(HEALTH_LOCK) {
        if (
            preferences.getBoolean(KEY_TERMINAL_FAILURE, false) &&
            preferences.getLong(KEY_FAILURE_GENERATION, LEGACY_FAILURE_GENERATION) == generation
        ) {
            clearLocked()
        }
    }

    private fun clearLocked() {
        preferences.edit {
            putBoolean(KEY_TERMINAL_FAILURE, false)
            remove(KEY_FAILURE_GENERATION)
        }
        _hasTerminalFailure.value = false
    }

    private companion object {
        const val PREFERENCES_NAME = "reminder_health_preferences"
        const val KEY_TERMINAL_FAILURE = "terminal_failure"
        const val KEY_FAILURE_GENERATION = "failure_generation"
        const val LEGACY_FAILURE_GENERATION = -1L
        val HEALTH_LOCK = Any()
    }
}
