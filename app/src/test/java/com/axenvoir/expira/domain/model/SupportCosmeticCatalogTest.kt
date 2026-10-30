package com.axenvoir.expira.domain.model

import com.axenvoir.expira.ui.theme.ExpiraPalettes
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SupportCosmeticCatalogTest {
    @Test
    fun every_cosmetic_has_unique_id_palette_and_price_with_a_supported_palette() {
        val items = SupportCosmeticCatalog.items

        assertThat(items).isNotEmpty()
        assertThat(items.map { it.id }.toSet()).hasSize(items.size)
        assertThat(items.map { it.paletteId }.toSet()).hasSize(items.size)
        assertThat(items.map { it.pointCost }.toSet()).hasSize(items.size)
        items.forEach { cosmetic ->
            assertThat(cosmetic.pointCost).isGreaterThan(0)
            assertThat(ExpiraPalettes.all).contains(cosmetic.paletteId)
            assertThat(cosmetic.paletteId).isNotEqualTo(ExpiraPalettes.GROVE)
            assertThat(SupportCosmeticCatalog.find(cosmetic.id)).isEqualTo(cosmetic)
        }
        assertThat(SupportCosmeticCatalog.find("unknown_cosmetic")).isNull()
    }
}
