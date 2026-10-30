package com.axenvoir.expira.ads

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PendingRewardStoreTest {
    private lateinit var context: Context
    private lateinit var store: PendingRewardStore

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        store = PendingRewardStore(context)
    }

    @After
    fun tearDown() {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun confirmed_event_survives_store_recreation_until_cleared() {
        assertThat(store.savePendingEventId("confirmed-event")).isTrue()

        val recreated = PendingRewardStore(context)
        assertThat(recreated.readPendingEventId()).isEqualTo("confirmed-event")

        recreated.clearPendingEventId("confirmed-event")
        assertThat(store.readPendingEventId()).isNull()
    }

    @Test
    fun retry_cleanup_cannot_clear_a_different_pending_event() {
        store.savePendingEventId("new-event")
        store.clearPendingEventId("old-event")

        assertThat(store.readPendingEventId()).isEqualTo("new-event")
    }

    private companion object {
        const val PREFERENCES_NAME = "reward_retry_preferences"
    }
}
