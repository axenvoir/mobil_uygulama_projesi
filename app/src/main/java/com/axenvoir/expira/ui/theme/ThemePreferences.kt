package com.axenvoir.expira.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val dynamicColorKey = booleanPreferencesKey("dynamic_color")

    val themeMode: Flow<ThemeMode> = context.themeDataStore.data.map { prefs ->
        when (prefs[themeModeKey]) {
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    val dynamicColor: Flow<Boolean> = context.themeDataStore.data.map { prefs ->
        prefs[dynamicColorKey] ?: false
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { prefs ->
            prefs[themeModeKey] = when (mode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.SYSTEM -> "system"
            }
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[dynamicColorKey] = enabled
        }
    }
}
