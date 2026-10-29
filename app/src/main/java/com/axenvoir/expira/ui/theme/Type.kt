package com.axenvoir.expira.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.axenvoir.expira.R

private val Inter = FontFamily(
    Font(
        R.font.inter_variable,
        weight = FontWeight.Normal,
    ),
    Font(
        R.font.inter_variable,
        weight = FontWeight.Medium,
    ),
    Font(
        R.font.inter_variable,
        weight = FontWeight.SemiBold,
    ),
    Font(
        R.font.inter_variable,
        weight = FontWeight.Bold,
    ),
)

private fun textStyle(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
    tracking: Float = 0f,
) = TextStyle(
    fontFamily = Inter,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.sp,
)

val AppTypography = Typography(
    displayLarge = textStyle(57, 64, FontWeight.Normal, -0.25f),
    displayMedium = textStyle(45, 52, FontWeight.Normal),
    displaySmall = textStyle(36, 44, FontWeight.Normal),
    headlineLarge = textStyle(32, 40, FontWeight.SemiBold),
    headlineMedium = textStyle(28, 36, FontWeight.SemiBold),
    headlineSmall = textStyle(24, 32, FontWeight.SemiBold),
    titleLarge = textStyle(22, 28, FontWeight.SemiBold),
    titleMedium = textStyle(16, 24, FontWeight.SemiBold, 0.1f),
    titleSmall = textStyle(14, 20, FontWeight.Medium, 0.1f),
    bodyLarge = textStyle(16, 24, FontWeight.Normal, 0.1f),
    bodyMedium = textStyle(14, 20, FontWeight.Normal, 0.15f),
    bodySmall = textStyle(12, 16, FontWeight.Normal, 0.2f),
    labelLarge = textStyle(14, 20, FontWeight.Medium, 0.1f),
    labelMedium = textStyle(12, 16, FontWeight.Medium, 0.2f),
    labelSmall = textStyle(11, 16, FontWeight.Medium, 0.25f),
)
