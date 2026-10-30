package com.axenvoir.expira.ads

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

/** Persists only an opaque event ID until an SDK-confirmed point reward reaches Room. */
@Singleton
class PendingRewardStore @Inject constructor(
    @param:ApplicationContext context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun readPendingEventId(): String? = preferences.getString(KEY_PENDING_EVENT_ID, null)

    @android.annotation.SuppressLint("UseKtx")
    fun savePendingEventId(eventId: String): Boolean = runCatching {
        // The KTX helper discards commit()'s Boolean; this write-ahead result must be checked.
        preferences.edit().putString(KEY_PENDING_EVENT_ID, eventId).commit()
    }.getOrDefault(false)

    fun clearPendingEventId(eventId: String) {
        if (preferences.getString(KEY_PENDING_EVENT_ID, null) == eventId) {
            preferences.edit { remove(KEY_PENDING_EVENT_ID) }
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "reward_retry_preferences"
        const val KEY_PENDING_EVENT_ID = "pending_reward_event_id"
    }
}
