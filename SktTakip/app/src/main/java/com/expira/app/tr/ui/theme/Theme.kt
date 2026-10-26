package com.expira.app.tr.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val PrimaryGreen = Color(0xFF267968)
val PrimaryDarkGreen = Color(0xFF153F38)
val AccentLime = Color(0xFFC4D639)
val SurfaceLight = Color(0xFFFFFFFF)
val BackgroundLight = Color(0xFFF5F7F4)
val TextDark = Color(0xFF19332D)
val TextMuted = Color(0xFF5A6B63)
val BorderLight = Color(0xFFE1E8E3)

val FreshGreen = Color(0xFF10B981)
val FreshGreenBg = Color(0xFFEAF5EF)
val WarningAmber = Color(0xFFD97706)
val WarningAmberBg = Color(0xFFFEF3C7)
val DangerRed = Color(0xFFDC2626)
val DangerRedBg = Color(0xFFFEE2E2)

val SurfaceDark = Color(0xFF1A2B26)
val BackgroundDark = Color(0xFF101C19)
val TextLight = Color(0xFFF0F4EC)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = PrimaryDarkGreen,
    onPrimaryContainer = Color.White,
    secondary = AccentLime,
    onSecondary = PrimaryDarkGreen,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextDark,
    onSurface = TextDark,
    outline = BorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF70CDB0),
    onPrimary = Color(0xFF102B26),
    primaryContainer = PrimaryDarkGreen,
    onPrimaryContainer = Color.White,
    secondary = AccentLime,
    onSecondary = PrimaryDarkGreen,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextLight,
    onSurface = TextLight,
    outline = Color(0xFF344A40)
)

val Typography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)

@Composable
fun ExpiraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
