package com.axenvoir.expira

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CrashDisplayActivityTest {
    @get:Rule
    val rule = createAndroidComposeRule<CrashDisplayActivity>()

    @Test
    fun recovery_message_and_continue_action_are_displayed() {
        rule.onNodeWithText(rule.activity.getString(R.string.crash_recovery_title))
            .assertIsDisplayed()
        rule.onNodeWithText(rule.activity.getString(R.string.crash_recovery_continue))
            .assertIsDisplayed()
    }
}
