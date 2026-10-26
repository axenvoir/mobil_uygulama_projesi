package com.axenvoir.sktplus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BrandNavy,
    onPrimary = BrandSurface,
    primaryContainer = BrandBlueContainer,
    onPrimaryContainer = BrandNavy,
    secondary = BrandBlue,
    onSecondary = BrandSurface,
    secondaryContainer = BrandBlueContainer,
    onSecondaryContainer = BrandNavy,
    tertiary = FreshGreen,
    onTertiary = BrandSurface,
    tertiaryContainer = FreshGreenContainer,
    onTertiaryContainer = FreshGreen,
    background = BrandSurface,
    onBackground = BrandNavy,
    surface = BrandSurface,
    onSurface = BrandNavy,
    surfaceVariant = BrandBlueContainer,
    onSurfaceVariant = BrandNavyLight,
    error = ExpiredRed,
    errorContainer = ExpiredRedContainer,
    onError = BrandSurface,
    outline = BrandNavyLight,
    outlineVariant = BrandBlueContainer,
)

private val DarkColors = darkColorScheme(
    primary = BrandBlue,
    onPrimary = BrandSurfaceDark,
    primaryContainer = BrandNavy,
    onPrimaryContainer = BrandBlueContainer,
    secondary = BrandNavyLight,
    onSecondary = BrandSurfaceDark,
    secondaryContainer = BrandNavy,
    onSecondaryContainer = BrandBlueContainer,
    tertiary = FreshGreen,
    onTertiary = BrandSurfaceDark,
    tertiaryContainer = FreshGreen,
    onTertiaryContainer = FreshGreenContainer,
    background = BrandSurfaceDark,
    onBackground = BrandBlueContainer,
    surface = BrandSurfaceDark,
    onSurface = BrandBlueContainer,
    surfaceVariant = BrandNavy,
    onSurfaceVariant = BrandBlueContainer,
    error = ExpiredRed,
    errorContainer = ExpiredRed,
    onError = BrandSurfaceDark,
    outline = BrandNavyLight,
    outlineVariant = BrandNavy,
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun SktPlusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
