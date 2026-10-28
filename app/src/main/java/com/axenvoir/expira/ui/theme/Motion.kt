package com.axenvoir.expira.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

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
