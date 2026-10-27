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
            val intent = Intent(context, CrashDisplayActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra(com.axenvoir.sktplus.CrashDisplayActivity.EXTRA_STACK_TRACE, stackTrace)
            }
            runCatching { context.startActivity(intent) }
                .onFailure { android.util.Log.e("CrashReporter", "crash display failed", throwable) }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }
}
