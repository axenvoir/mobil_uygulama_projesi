package com.axenvoir.sktplus

import android.content.Context
import android.content.Intent
import java.io.PrintWriter
import java.io.StringWriter

object CrashReporter {

    fun install(context: Context) {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val stringWriter = StringWriter()
            throwable.printStackTrace(PrintWriter(stringWriter))
            val stackTrace = stringWriter.toString()
            runCatching {
                val crashFile = java.io.File(context.cacheDir, "crash_last.txt")
                crashFile.writeText(stackTrace.take(8000))
                val intent = Intent(context, CrashDisplayActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                context.startActivity(intent)
            }.onFailure { error ->
                android.util.Log.e("CrashReporter", "crash display failed", error)
            }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }
}
