package com.axenvoir.expira.ui.theme

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

object ExpiraMotion {
    const val durationMedium = 300
    val easingDecelerate = LinearOutSlowInEasing
}

/** Reads the platform animation scale without introducing an app-specific motion setting. */
@Composable
fun rememberSystemReduceMotion(): Boolean {
    val context = LocalContext.current.applicationContext
    val resolver = context.contentResolver
    var reduceMotion by remember(resolver) {
        mutableStateOf(isSystemReduceMotionEnabled(context))
    }

    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                reduceMotion = isSystemReduceMotionEnabled(context)
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        onDispose { resolver.unregisterContentObserver(observer) }
    }

    return reduceMotion
}

private fun isSystemReduceMotionEnabled(context: Context): Boolean = runCatching {
    Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    ) <= 0f
}.getOrDefault(false)
