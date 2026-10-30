package com.axenvoir.expira.data.repository

import android.content.Context
import com.axenvoir.expira.data.local.SupportRewardDao
import com.axenvoir.expira.domain.model.CosmeticPurchaseResult
import com.axenvoir.expira.domain.model.SupportCosmeticCatalog
import com.axenvoir.expira.domain.model.SupportWalletState
import com.axenvoir.expira.domain.repository.SupportRewardRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

@Singleton
class SupportRewardRepositoryImpl @Inject constructor(
    private val supportRewardDao: SupportRewardDao,
    @param:ApplicationContext private val context: Context,
) : SupportRewardRepository {
    override val wallet: Flow<SupportWalletState> = combine(
        supportRewardDao.observeWallet(),
        supportRewardDao.observeOwnedCosmeticIds(),
    ) { entity, ownedIds ->
        SupportWalletState(
            isReady = entity?.legacyPointsImported == true,
            spendablePoints = entity?.balance ?: 0,
            lifetimeEarnedPoints = entity?.lifetimeEarnedPoints ?: 0,
            supporterBadgeUnlocked = entity?.supporterBadgeUnlocked == true,
            ownedCosmeticIds = ownedIds.toSet(),
        )
    }

    override suspend fun importLegacyBalanceIfNeeded() {
        val legacyBalance = withContext(Dispatchers.IO) {
            context.getSharedPreferences(LEGACY_PREFERENCES, Context.MODE_PRIVATE)
                .getInt(LEGACY_POINTS_KEY, 0)
                .coerceAtLeast(0)
        }
        supportRewardDao.importLegacyBalanceIfNeeded(legacyBalance)
    }

    override suspend fun grantCompletedAd(eventId: String): Boolean {
        importLegacyBalanceIfNeeded()
        return supportRewardDao.grantCompletedAd(
            eventId = eventId,
            now = System.currentTimeMillis(),
        )
    }

    override suspend fun redeemCosmetic(cosmeticId: String): CosmeticPurchaseResult {
        val cosmetic = SupportCosmeticCatalog.find(cosmeticId)
            ?: return CosmeticPurchaseResult.UNKNOWN_ITEM
        importLegacyBalanceIfNeeded()
        return supportRewardDao.redeemCosmetic(
            cosmeticId = cosmetic.id,
            pointCost = cosmetic.pointCost,
            now = System.currentTimeMillis(),
        )
    }

    private companion object {
        const val LEGACY_PREFERENCES = "support_reward_preferences"
        const val LEGACY_POINTS_KEY = "support_points"
    }
}
