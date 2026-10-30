package com.axenvoir.expira.domain.repository

import com.axenvoir.expira.domain.model.CosmeticPurchaseResult
import com.axenvoir.expira.domain.model.SupportWalletState
import kotlinx.coroutines.flow.Flow

interface SupportRewardRepository {
    val wallet: Flow<SupportWalletState>

    suspend fun importLegacyBalanceIfNeeded()

    /** Credits exactly one point for one unique, SDK-earned ad session. */
    suspend fun grantCompletedAd(eventId: String): Boolean

    /** Redeems only a catalogued cosmetic; its cost is never supplied by the UI. */
    suspend fun redeemCosmetic(cosmeticId: String): CosmeticPurchaseResult
}
