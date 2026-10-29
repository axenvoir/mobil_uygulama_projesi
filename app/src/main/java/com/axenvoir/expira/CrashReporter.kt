package com.axenvoir.expira

import android.content.Context
import android.os.Process
import android.util.Log
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

object CrashReporter {
    private const val TAG = "CrashReporter"
    private const val CRASH_FILE = "crash_last.txt"
    private const val MAX_TRACE_LENGTH = 8_000
    private val installed = AtomicBoolean(false)
    private val handlingCrash = AtomicBoolean(false)

    fun install(context: Context) {
        if (!installed.compareAndSet(false, true)) return

        val appContext = context.applicationContext
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        val handler = object : Thread.UncaughtExceptionHandler {
            override fun uncaughtException(thread: Thread, throwable: Throwable) {
                if (handlingCrash.compareAndSet(false, true)) {
                    writeLocalCrashMarker(appContext, throwable)
                }

                // Never forward to this handler again: a repeated install or a chained
                // handler must not recurse while the process is already failing.
                if (previousHandler != null && previousHandler !== this) {
                    runCatching {
                        previousHandler.uncaughtException(thread, throwable)
                    }.onFailure { forwardingError ->
                        Log.e(TAG, "Unable to forward uncaught exception", forwardingError)
                    }
                }
                Process.killProcess(Process.myPid())
                exitProcess(10)
            }
        }
        Thread.setDefaultUncaughtExceptionHandler(handler)
    }

    fun consumeCrashMarker(context: Context): Boolean = runCatching {
        val appContext = context.applicationContext
        val markers = listOf(
            File(appContext.noBackupFilesDir, CRASH_FILE),
            File(appContext.cacheDir, CRASH_FILE),
        )
        val found = markers.any(File::exists)
        markers.filter(File::exists).forEach { it.delete() }
        found
    }.getOrElse { error ->
        Log.e(TAG, "Unable to consume crash marker", error)
        false
    }

    private fun writeLocalCrashMarker(context: Context, throwable: Throwable) {
        val stackTrace = buildString {
            appendLine("type=${throwable::class.java.name}")
            throwable.stackTrace.take(40).forEach { frame ->
                appendLine("at ${frame.className}.${frame.methodName}(${frame.fileName}:${frame.lineNumber})")
            }
        }.take(MAX_TRACE_LENGTH)

        runCatching {
            File(context.noBackupFilesDir, CRASH_FILE).writeText(stackTrace)
        }.onFailure { error ->
            Log.e(TAG, "Unable to persist crash marker", error)
        }
    }
}
