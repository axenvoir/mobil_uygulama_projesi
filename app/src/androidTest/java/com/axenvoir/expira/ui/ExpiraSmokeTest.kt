package com.axenvoir.expira.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.axenvoir.expira.MainActivity
import com.axenvoir.expira.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExpiraSmokeTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun first_launch_can_dismiss_onboarding_and_show_product_screen() {
        rule.activity.getSharedPreferences("app_settings", 0)
            .edit()
            .putBoolean("onboarding_done", false)
            .commit()
        rule.activity.runOnUiThread { rule.activity.recreate() }
        rule.waitForIdle()

        rule.onNodeWithText(rule.activity.getString(R.string.onboarding_skip))
            .performClick()
        rule.onNodeWithText(rule.activity.getString(R.string.expiry_list_title))
            .assertIsDisplayed()
    }

    @Test
    fun product_and_note_tabs_are_visible_after_onboarding() {
        rule.activity.getSharedPreferences("app_settings", 0)
            .edit()
            .putBoolean("onboarding_done", true)
            .commit()
        rule.activity.runOnUiThread { rule.activity.recreate() }
        rule.waitForIdle()

        rule.onNodeWithText(rule.activity.getString(R.string.tab_expiry))
            .assertIsDisplayed()
        rule.onNodeWithText(rule.activity.getString(R.string.tab_notes))
            .assertIsDisplayed()
            .performClick()
        rule.onNodeWithText(rule.activity.getString(R.string.notes_list_title))
            .assertIsDisplayed()
    }

    @Test
    fun settings_exposes_the_cosmetic_palette_shop() {
        rule.activity.getSharedPreferences("app_settings", 0)
            .edit()
            .putBoolean("onboarding_done", true)
            .commit()
        rule.activity.runOnUiThread { rule.activity.recreate() }
        rule.waitForIdle()

        rule.onNodeWithContentDescription(rule.activity.getString(R.string.settings_title))
            .performClick()
        rule.onNodeWithText(rule.activity.getString(R.string.palette_shop_section))
            .performScrollTo()
            .assertIsDisplayed()
        rule.onNodeWithText(rule.activity.getString(R.string.palette_grove_name))
            .assertIsDisplayed()
    }
}
