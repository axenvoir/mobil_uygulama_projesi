package com.axenvoir.expira.ui.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

fun Context.findActivity(): Activity? {
    var current: Context = this
    repeat(16) {
        when (val context = current) {
            is Activity -> return context
            is ContextWrapper -> current = context.baseContext
            else -> return null
        }
    }
    return null
}
