package com.axenvoir.expira.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderTimeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!shouldReschedule(intent.action)) return
        ReminderScheduler(context.applicationContext).scheduleDailyReminder()
    }

    companion object {
        internal fun shouldReschedule(action: String?): Boolean =
            action == Intent.ACTION_TIME_CHANGED || action == Intent.ACTION_TIMEZONE_CHANGED
    }
}
