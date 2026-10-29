package com.axenvoir.expira.ui.theme

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

object ExpiraMotion {
    // Durations
    const val durationFast = 100
    const val durationMedium = 300
    const val durationSlow = 500

    // Easing
    val easingStandard = FastOutSlowInEasing
    val easingDecelerate = LinearOutSlowInEasing

    // Tweens
    val tweenFast = tween<Float>(durationMillis = durationFast, easing = easingStandard)
    val tweenMedium = tween<Float>(durationMillis = durationMedium, easing = easingStandard)
    val tweenSlow = tween<Float>(durationMillis = durationSlow, easing = easingStandard)

    // Springs
    val springDefault = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )
    val springStiff = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh,
    )
    val springGentle = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow,
    )

    // Stagger cap for list animations
    const val staggerCap = 6
    const val staggerDelayMs = 50
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
