package com.axenvoir.expira.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ReminderTimeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!shouldReschedule(intent.action)) return
        SystemTimeChangeEvents.publish()
        val appContext = context.applicationContext
        val pendingResult = runCatching { goAsync() }.getOrNull()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val scheduler = ReminderScheduler(appContext)
            val healthStore = ReminderHealthStore(appContext)
            try {
                val generation = scheduler.scheduleDailyReminder()
                healthStore.clearFailuresBeforeGeneration(generation)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: ReminderScheduleException) {
                scheduler.withCurrentGeneration(failure.generation) {
                    healthStore.markTerminalFailure(failure.generation)
                }
                Log.e(TAG, "Reminder schedule could not be persisted after time change", failure)
            } catch (exception: Exception) {
                Log.e(TAG, "Reminder schedule failed after time change", exception)
            } finally {
                pendingResult?.finish()
            }
        }
    }

    companion object {
        internal fun shouldReschedule(action: String?): Boolean =
            action == Intent.ACTION_TIME_CHANGED || action == Intent.ACTION_TIMEZONE_CHANGED

        private const val TAG = "ReminderTimeChange"
    }
}

internal object SystemTimeChangeEvents {
    private val mutableChanges = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val changes: SharedFlow<Unit> = mutableChanges.asSharedFlow()

    fun publish() {
        mutableChanges.tryEmit(Unit)
    }
}
