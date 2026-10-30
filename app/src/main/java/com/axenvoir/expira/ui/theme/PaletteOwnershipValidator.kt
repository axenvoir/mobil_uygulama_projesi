package com.axenvoir.expira.ui.theme

import android.util.Log
import com.axenvoir.expira.domain.model.SupportCosmeticCatalog
import com.axenvoir.expira.domain.repository.SupportRewardRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

@Singleton
class PaletteOwnershipValidator @Inject constructor(
    private val themePreferences: ThemePreferences,
    private val supportRewardRepository: SupportRewardRepository,
) {
    suspend fun validateSavedSelection() {
        try {
            val paletteId = themePreferences.paletteId.first()
            if (paletteId == ExpiraPalettes.GROVE) return

            val wallet = withTimeoutOrNull(WALLET_READY_TIMEOUT_MILLIS) {
                supportRewardRepository.wallet.first { it.isReady }
            }
            if (wallet == null || !isExpiraPaletteOwned(paletteId, wallet.ownedCosmeticIds)) {
                themePreferences.selectPalette(ExpiraPalettes.GROVE)
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            Log.w(TAG, "Could not verify saved palette ownership; using the free palette", exception)
            runCatching { themePreferences.selectPalette(ExpiraPalettes.GROVE) }
        }
    }

    private companion object {
        const val WALLET_READY_TIMEOUT_MILLIS = 8_000L
        const val TAG = "PaletteOwnership"
    }
}

internal fun isExpiraPaletteOwned(paletteId: String, ownedCosmeticIds: Set<String>): Boolean {
    if (paletteId == ExpiraPalettes.GROVE) return true
    val cosmetic = SupportCosmeticCatalog.items.firstOrNull { it.paletteId == paletteId }
        ?: return false
    return cosmetic.id in ownedCosmeticIds
}
