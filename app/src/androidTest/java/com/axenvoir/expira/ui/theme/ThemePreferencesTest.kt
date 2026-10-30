package com.axenvoir.expira.ui.theme

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemePreferencesTest {
    @Test
    fun selecting_named_palette_also_disables_dynamic_color() = runBlocking {
        val context: Context = ApplicationProvider.getApplicationContext()
        val preferences = ThemePreferences(context)
        val originalPalette = preferences.paletteId.first()
        val originalDynamicColor = preferences.dynamicColor.first()

        try {
            preferences.setDynamicColor(true)
            preferences.selectPalette(ExpiraPalettes.QUIET_DUSK)

            assertThat(preferences.paletteId.first()).isEqualTo(ExpiraPalettes.QUIET_DUSK)
            assertThat(preferences.dynamicColor.first()).isFalse()
        } finally {
            preferences.selectPalette(originalPalette)
            preferences.setDynamicColor(originalDynamicColor)
        }
    }
}
