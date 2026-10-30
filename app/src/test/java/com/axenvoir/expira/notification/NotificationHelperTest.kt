package com.axenvoir.expira.notification

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NotificationHelperTest {
    @Test
    fun test_notification_uses_a_distinct_id_from_expiry_reminders() {
        assertThat(NotificationHelper.TEST_NOTIFICATION_ID)
            .isNotEqualTo(NotificationHelper.NOTIFICATION_ID)
    }
}
