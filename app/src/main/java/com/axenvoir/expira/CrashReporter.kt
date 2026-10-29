package com.axenvoir.expira

import android.content.Context

object CrashReporter {

    fun install(context: Context) {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val stackTrace = buildString {
                appendLine("type=${throwable::class.java.name}")
                throwable.stackTrace.take(40).forEach { frame ->
                    appendLine("at ${frame.className}.${frame.methodName}(${frame.fileName}:${frame.lineNumber})")
                }
            }
            runCatching {
                val crashFile = java.io.File(context.cacheDir, "crash_last.txt")
                crashFile.writeText(stackTrace.take(8000))
            }.onFailure { error ->
                android.util.Log.e("CrashReporter", "crash display failed", error)
            }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }
}
