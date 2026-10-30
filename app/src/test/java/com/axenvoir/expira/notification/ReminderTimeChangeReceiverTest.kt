package com.axenvoir.expira.notification

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReminderTimeChangeReceiverTest {
    @Test
    fun time_set_and_timezone_changed_actions_reschedule_reminders() {
        assertThat(ReminderTimeChangeReceiver.shouldReschedule(Intent.ACTION_TIME_CHANGED)).isTrue()
        assertThat(ReminderTimeChangeReceiver.shouldReschedule(Intent.ACTION_TIMEZONE_CHANGED)).isTrue()
    }

    @Test
    fun unrelated_or_missing_actions_are_ignored() {
        assertThat(ReminderTimeChangeReceiver.shouldReschedule(Intent.ACTION_SCREEN_ON)).isFalse()
        assertThat(ReminderTimeChangeReceiver.shouldReschedule(null)).isFalse()
    }
}
