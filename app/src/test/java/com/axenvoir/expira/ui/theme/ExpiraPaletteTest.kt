package com.axenvoir.expira.ui.theme

import com.axenvoir.expira.domain.model.SupportCosmeticCatalog
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import kotlin.math.pow
import org.junit.Test

class ExpiraPaletteTest {
    @Test
    fun every_named_palette_has_distinct_light_and_dark_brand_roles() {
        val lightSchemes = ExpiraPalettes.all.map { expiraStaticColorScheme(it, dark = false) }
        val darkSchemes = ExpiraPalettes.all.map { expiraStaticColorScheme(it, dark = true) }

        assertThat(lightSchemes.map { it.primary }.toSet()).hasSize(ExpiraPalettes.all.size)
        assertThat(darkSchemes.map { it.primary }.toSet()).hasSize(ExpiraPalettes.all.size)
    }

    @Test
    fun palette_shop_preview_uses_real_colors_from_the_selected_palette() {
        ExpiraPalettes.all.forEach { paletteId ->
            val scheme = expiraStaticColorScheme(paletteId, dark = false)
            assertThat(expiraPalettePreviewColors(paletteId)).containsExactly(
                scheme.primary,
                scheme.secondaryContainer,
                scheme.tertiary,
            ).inOrder()
        }
    }

    @Test
    fun all_standard_text_color_pairs_meet_wcag_aa_contrast_in_both_modes() {
        ExpiraPalettes.all.forEach { paletteId ->
            listOf(false, true).forEach { dark ->
                val scheme = expiraStaticColorScheme(paletteId, dark)
                val foregroundBackgroundPairs = listOf(
                    "primary" to (scheme.onPrimary to scheme.primary),
                    "primaryContainer" to (scheme.onPrimaryContainer to scheme.primaryContainer),
                    "secondary" to (scheme.onSecondary to scheme.secondary),
                    "secondaryContainer" to (scheme.onSecondaryContainer to scheme.secondaryContainer),
                    "tertiary" to (scheme.onTertiary to scheme.tertiary),
                    "tertiaryContainer" to (scheme.onTertiaryContainer to scheme.tertiaryContainer),
                    "surface" to (scheme.onSurface to scheme.surface),
                    "background" to (scheme.onBackground to scheme.background),
                    "surfaceVariant" to (scheme.onSurfaceVariant to scheme.surfaceVariant),
                )
                foregroundBackgroundPairs.forEach { (role, pair) ->
                    assertWithMessage("$paletteId/$dark/$role")
                        .that(contrastRatio(pair.first, pair.second))
                        .isAtLeast(4.5)
                }
            }
            }
    }

    @Test
    fun unknown_or_null_palette_ids_fall_back_to_free_grove() {
        assertThat(ExpiraPalettes.normalize(null)).isEqualTo(ExpiraPalettes.GROVE)
        assertThat(ExpiraPalettes.normalize("removed_palette"))
            .isEqualTo(ExpiraPalettes.GROVE)
        assertThat(
            expiraStaticColorScheme("removed_palette", dark = false).primary,
        ).isEqualTo(expiraStaticColorScheme(ExpiraPalettes.GROVE, dark = false).primary)
    }

    @Test
    fun restored_cosmetic_palette_requires_its_matching_owned_wallet_item() {
        assertThat(isExpiraPaletteOwned(ExpiraPalettes.GROVE, emptySet())).isTrue()
        assertThat(isExpiraPaletteOwned(ExpiraPalettes.QUIET_DUSK, emptySet())).isFalse()
        assertThat(
            isExpiraPaletteOwned(
                ExpiraPalettes.QUIET_DUSK,
                setOf(SupportCosmeticCatalog.QUIET_DUSK_ID),
            ),
        ).isTrue()
        assertThat(isExpiraPaletteOwned("unknown", emptySet())).isFalse()
    }

    private fun contrastRatio(foreground: androidx.compose.ui.graphics.Color, background: androidx.compose.ui.graphics.Color): Double {
        val first = relativeLuminance(foreground)
        val second = relativeLuminance(background)
        val lighter = maxOf(first, second)
        val darker = minOf(first, second)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(color: androidx.compose.ui.graphics.Color): Double {
        fun linear(component: Float): Double {
            val value = component.toDouble()
            return if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * linear(color.red) + 0.7152 * linear(color.green) + 0.0722 * linear(color.blue)
    }

}
