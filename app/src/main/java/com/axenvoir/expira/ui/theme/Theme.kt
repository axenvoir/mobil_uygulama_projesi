package com.axenvoir.expira.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.axenvoir.expira.ui.util.findActivity

@androidx.compose.runtime.Immutable
data class ExpiraStatusColors(
    val fresh: Color,
    val onFresh: Color,
    val freshContainer: Color,
    val onFreshContainer: Color,
    val expiring: Color,
    val onExpiring: Color,
    val expiringContainer: Color,
    val onExpiringContainer: Color,
    val expired: Color,
    val onExpired: Color,
    val expiredContainer: Color,
    val onExpiredContainer: Color,
)

val LocalExpiraStatusColors = staticCompositionLocalOf {
    ExpiraStatusColors(
        fresh = Color(0xFF21654C),
        onFresh = Color.White,
        freshContainer = Color(0xFFD6EEE1),
        onFreshContainer = Color(0xFF153B2A),
        expiring = Color(0xFF795100),
        onExpiring = Color.White,
        expiringContainer = Color(0xFFF6E6BD),
        onExpiringContainer = Color(0xFF4E390C),
        expired = Color(0xFFB3261E),
        onExpired = Color.White,
        expiredContainer = Color(0xFFF9DEDC),
        onExpiredContainer = Color(0xFF410E0B),
    )
}

private val LightColors = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceTint = PrimaryLight,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    inversePrimary = InversePrimaryLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    scrim = ScrimLight,
    surfaceDim = SurfaceDimLight,
    surfaceBright = SurfaceBrightLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceTint = PrimaryDark,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    inversePrimary = InversePrimaryDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    scrim = ScrimDark,
    surfaceDim = SurfaceDimDark,
    surfaceBright = SurfaceBrightDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
)

private data class PaletteTokens(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val outline: Color,
    val outlineVariant: Color,
)

private fun ColorScheme.apply(tokens: PaletteTokens): ColorScheme = copy(
    primary = tokens.primary,
    onPrimary = tokens.onPrimary,
    primaryContainer = tokens.primaryContainer,
    onPrimaryContainer = tokens.onPrimaryContainer,
    secondary = tokens.secondary,
    onSecondary = tokens.onSecondary,
    secondaryContainer = tokens.secondaryContainer,
    onSecondaryContainer = tokens.onSecondaryContainer,
    tertiary = tokens.tertiary,
    onTertiary = tokens.onTertiary,
    tertiaryContainer = tokens.tertiaryContainer,
    onTertiaryContainer = tokens.onTertiaryContainer,
    background = tokens.background,
    onBackground = tokens.onBackground,
    surface = tokens.surface,
    onSurface = tokens.onSurface,
    surfaceVariant = tokens.surfaceVariant,
    onSurfaceVariant = tokens.onSurfaceVariant,
    surfaceContainerLowest = tokens.surfaceContainerLowest,
    surfaceContainerLow = tokens.surfaceContainerLow,
    surfaceContainer = tokens.surfaceContainer,
    surfaceContainerHigh = tokens.surfaceContainerHigh,
    surfaceContainerHighest = tokens.surfaceContainerHighest,
    outline = tokens.outline,
    outlineVariant = tokens.outlineVariant,
    surfaceTint = tokens.primary,
)

private fun paletteTokens(paletteId: String, dark: Boolean): PaletteTokens? = when (paletteId) {
    ExpiraPalettes.QUIET_DUSK -> if (dark) {
        PaletteTokens(
            primary = Color(0xFFAAC8E0), onPrimary = Color(0xFF1B3549),
            primaryContainer = Color(0xFF304B62), onPrimaryContainer = Color(0xFFD8E7F2),
            secondary = Color(0xFFBDCCD5), onSecondary = Color(0xFF273640),
            secondaryContainer = Color(0xFF384A57), onSecondaryContainer = Color(0xFFDFE8EE),
            tertiary = Color(0xFFE6BAAE), onTertiary = Color(0xFF442821),
            tertiaryContainer = Color(0xFF5A3B33), onTertiaryContainer = Color(0xFFF4DED8),
            background = Color(0xFF11191F), onBackground = Color(0xFFE1E9EF),
            surface = Color(0xFF171F26), onSurface = Color(0xFFE1E9EF),
            surfaceVariant = Color(0xFF26333D), onSurfaceVariant = Color(0xFFC0CAD3),
            surfaceContainerLowest = Color(0xFF0C1217), surfaceContainerLow = Color(0xFF171F26),
            surfaceContainer = Color(0xFF1C2730), surfaceContainerHigh = Color(0xFF26343E),
            surfaceContainerHighest = Color(0xFF30404B), outline = Color(0xFF8A9BA8),
            outlineVariant = Color(0xFF43525D),
        )
    } else {
        PaletteTokens(
            primary = Color(0xFF3B5D76), onPrimary = Color.White,
            primaryContainer = Color(0xFFD8E7F2), onPrimaryContainer = Color(0xFF1A3449),
            secondary = Color(0xFF4E5E6B), onSecondary = Color.White,
            secondaryContainer = Color(0xFFDCE5EC), onSecondaryContainer = Color(0xFF1D2D39),
            tertiary = Color(0xFF765247), onTertiary = Color.White,
            tertiaryContainer = Color(0xFFF1DFD9), onTertiaryContainer = Color(0xFF442821),
            background = Color(0xFFF3F5F8), onBackground = Color(0xFF20272E),
            surface = Color(0xFFFCFDFE), onSurface = Color(0xFF20272E),
            surfaceVariant = Color(0xFFE8EDF2), onSurfaceVariant = Color(0xFF47525F),
            surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF1F4F7),
            surfaceContainer = Color(0xFFEAEFF3), surfaceContainerHigh = Color(0xFFE2E8EE),
            surfaceContainerHighest = Color(0xFFD9E1E8), outline = Color(0xFF687784),
            outlineVariant = Color(0xFFC4CED6),
        )
    }

    ExpiraPalettes.CITRUS_LEDGER -> if (dark) {
        PaletteTokens(
            primary = Color(0xFFC6D99B), onPrimary = Color(0xFF2B3618),
            primaryContainer = Color(0xFF424F26), onPrimaryContainer = Color(0xFFE4EBCB),
            secondary = Color(0xFFCBD0B3), onSecondary = Color(0xFF303426),
            secondaryContainer = Color(0xFF44483A), onSecondaryContainer = Color(0xFFE4E8D4),
            tertiary = Color(0xFFE7C979), onTertiary = Color(0xFF3D310C),
            tertiaryContainer = Color(0xFF584817), onTertiaryContainer = Color(0xFFF4E8BC),
            background = Color(0xFF191B14), onBackground = Color(0xFFE6E8DA),
            surface = Color(0xFF202219), onSurface = Color(0xFFE6E8DA),
            surfaceVariant = Color(0xFF303426), onSurfaceVariant = Color(0xFFC5C9B2),
            surfaceContainerLowest = Color(0xFF11120D), surfaceContainerLow = Color(0xFF202219),
            surfaceContainer = Color(0xFF25271E), surfaceContainerHigh = Color(0xFF303328),
            surfaceContainerHighest = Color(0xFF3A3E31), outline = Color(0xFF929782),
            outlineVariant = Color(0xFF484C3D),
        )
    } else {
        PaletteTokens(
            primary = Color(0xFF4D622B), onPrimary = Color.White,
            primaryContainer = Color(0xFFE4EBCB), onPrimaryContainer = Color(0xFF243319),
            secondary = Color(0xFF5B614D), onSecondary = Color.White,
            secondaryContainer = Color(0xFFE3E8D6), onSecondaryContainer = Color(0xFF262E1C),
            tertiary = Color(0xFF73580C), onTertiary = Color.White,
            tertiaryContainer = Color(0xFFF5E8B8), onTertiaryContainer = Color(0xFF443600),
            background = Color(0xFFF6F6EB), onBackground = Color(0xFF24281E),
            surface = Color(0xFFFFFDF6), onSurface = Color(0xFF24281E),
            surfaceVariant = Color(0xFFEBEEDC), onSurfaceVariant = Color(0xFF4D5542),
            surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF1F2E6),
            surfaceContainer = Color(0xFFECEEDD), surfaceContainerHigh = Color(0xFFE3E6D2),
            surfaceContainerHighest = Color(0xFFD9DEC5), outline = Color(0xFF6E765F),
            outlineVariant = Color(0xFFC9CEB7),
        )
    }

    ExpiraPalettes.BERRY_ARCHIVE -> if (dark) {
        PaletteTokens(
            primary = Color(0xFFE3B9D7), onPrimary = Color(0xFF452D41),
            primaryContainer = Color(0xFF5B3E54), onPrimaryContainer = Color(0xFFF0DDE9),
            secondary = Color(0xFFD0C4D5), onSecondary = Color(0xFF352E3B),
            secondaryContainer = Color(0xFF4B4252), onSecondaryContainer = Color(0xFFEAE1EC),
            tertiary = Color(0xFFE6BDA5), onTertiary = Color(0xFF482E22),
            tertiaryContainer = Color(0xFF5D4031), onTertiaryContainer = Color(0xFFF4E0D4),
            background = Color(0xFF1C171D), onBackground = Color(0xFFEDE3EA),
            surface = Color(0xFF241E25), onSurface = Color(0xFFEDE3EA),
            surfaceVariant = Color(0xFF352C37), onSurfaceVariant = Color(0xFFD0C4D2),
            surfaceContainerLowest = Color(0xFF130F14), surfaceContainerLow = Color(0xFF241E25),
            surfaceContainer = Color(0xFF2A232C), surfaceContainerHigh = Color(0xFF352C37),
            surfaceContainerHighest = Color(0xFF403641), outline = Color(0xFF9B8E9D),
            outlineVariant = Color(0xFF504653),
        )
    } else {
        PaletteTokens(
            primary = Color(0xFF744C68), onPrimary = Color.White,
            primaryContainer = Color(0xFFEEDDE9), onPrimaryContainer = Color(0xFF40273A),
            secondary = Color(0xFF5F596A), onSecondary = Color.White,
            secondaryContainer = Color(0xFFE8E1EC), onSecondaryContainer = Color(0xFF302B36),
            tertiary = Color(0xFF825440), onTertiary = Color.White,
            tertiaryContainer = Color(0xFFF1DFD3), onTertiaryContainer = Color(0xFF462A1F),
            background = Color(0xFFF7F2F6), onBackground = Color(0xFF28212A),
            surface = Color(0xFFFFFCFE), onSurface = Color(0xFF28212A),
            surfaceVariant = Color(0xFFEDE6EE), onSurfaceVariant = Color(0xFF534A56),
            surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF3EDF2),
            surfaceContainer = Color(0xFFEEE8EE), surfaceContainerHigh = Color(0xFFE6DDE6),
            surfaceContainerHighest = Color(0xFFDED4DE), outline = Color(0xFF756A77),
            outlineVariant = Color(0xFFCDC2CD),
        )
    }

    else -> null
}

fun expiraPalettePreviewColors(paletteId: String): List<Color> {
    val scheme = expiraStaticColorScheme(paletteId, dark = false)
    return listOf(scheme.primary, scheme.secondaryContainer, scheme.tertiary)
}

internal fun expiraStaticColorScheme(paletteId: String, dark: Boolean): ColorScheme {
    val base = if (dark) DarkColors else LightColors
    val tokens = paletteTokens(ExpiraPalettes.normalize(paletteId), dark) ?: return base
    return base.apply(tokens)
}

private val LightStatusColors = ExpiraStatusColors(
    fresh = Color(0xFF21654C),
    onFresh = Color.White,
    freshContainer = Color(0xFFD6EEE1),
    onFreshContainer = Color(0xFF153B2A),
    expiring = Color(0xFF795100),
    onExpiring = Color.White,
    expiringContainer = Color(0xFFF6E6BD),
    onExpiringContainer = Color(0xFF4E390C),
    expired = Color(0xFFB3261E),
    onExpired = Color.White,
    expiredContainer = Color(0xFFF9DEDC),
    onExpiredContainer = Color(0xFF410E0B),
)

private val DarkStatusColors = ExpiraStatusColors(
    fresh = Color(0xFFA7D6BB),
    onFresh = Color(0xFF12392A),
    freshContainer = Color(0xFF22543A),
    onFreshContainer = Color(0xFFD8F0E2),
    expiring = Color(0xFFF4C66F),
    onExpiring = Color(0xFF3E2E04),
    expiringContainer = Color(0xFF654B10),
    onExpiringContainer = Color(0xFFFAE9BF),
    expired = Color(0xFFFFB4A9),
    onExpired = Color(0xFF690B05),
    expiredContainer = Color(0xFF8C1D18),
    onExpiredContainer = Color(0xFFFFDAD4),
)

@Composable
fun ExpiraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    paletteId: String = ExpiraPalettes.GROVE,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = androidx.compose.runtime.remember(darkTheme, dynamicColor, paletteId, context) {
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            else -> expiraStaticColorScheme(paletteId, darkTheme)
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity() ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(activity.window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalExpiraStatusColors provides if (darkTheme) DarkStatusColors else LightStatusColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = ExpiraShapes,
            content = content,
        )
    }
}
