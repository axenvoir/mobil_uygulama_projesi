package com.axenvoir.expira.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ReminderTimeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!shouldReschedule(intent.action)) return
        SystemTimeChangeEvents.publish()
        ReminderScheduler(context.applicationContext).scheduleDailyReminder()
    }

    companion object {
        internal fun shouldReschedule(action: String?): Boolean =
            action == Intent.ACTION_TIME_CHANGED || action == Intent.ACTION_TIMEZONE_CHANGED
    }
}

internal object SystemTimeChangeEvents {
    private val mutableChanges = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val changes: SharedFlow<Unit> = mutableChanges.asSharedFlow()

    fun publish() {
        mutableChanges.tryEmit(Unit)
    }
}
