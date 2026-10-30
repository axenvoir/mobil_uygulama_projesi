package com.axenvoir.expira.domain.model

/** Non-transferable, no-cash-value progress earned through optional rewarded ads. */
data class SupportWalletState(
    val isReady: Boolean = false,
    val spendablePoints: Int = 0,
    val lifetimeEarnedPoints: Int = 0,
    val supporterBadgeUnlocked: Boolean = false,
    val ownedCosmeticIds: Set<String> = emptySet(),
)

data class SupportCosmetic(
    val id: String,
    val paletteId: String,
    val pointCost: Int,
)

enum class CosmeticPurchaseResult {
    PURCHASED,
    ALREADY_OWNED,
    INSUFFICIENT_POINTS,
    UNKNOWN_ITEM,
}

object SupportCosmeticCatalog {
    const val QUIET_DUSK_ID = "palette_quiet_dusk"
    const val CITRUS_LEDGER_ID = "palette_citrus_ledger"
    const val BERRY_ARCHIVE_ID = "palette_berry_archive"

    val items: List<SupportCosmetic> = listOf(
        SupportCosmetic(
            id = QUIET_DUSK_ID,
            paletteId = "quiet_dusk",
            pointCost = 5,
        ),
        SupportCosmetic(
            id = CITRUS_LEDGER_ID,
            paletteId = "citrus_ledger",
            pointCost = 10,
        ),
        SupportCosmetic(
            id = BERRY_ARCHIVE_ID,
            paletteId = "berry_archive",
            pointCost = 15,
        ),
    )

    fun find(id: String): SupportCosmetic? = items.firstOrNull { it.id == id }
}
