package com.axenvoir.expira.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.axenvoir.expira.domain.model.CosmeticPurchaseResult
import kotlinx.coroutines.flow.Flow

@Dao
interface SupportRewardDao {
    @Query("SELECT * FROM support_wallet WHERE id = 1")
    fun observeWallet(): Flow<SupportWalletEntity?>

    @Query("SELECT * FROM support_wallet WHERE id = 1")
    suspend fun getWallet(): SupportWalletEntity?

    @Query("SELECT cosmeticId FROM support_cosmetic_unlocks ORDER BY unlockedAt, cosmeticId")
    fun observeOwnedCosmeticIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM support_cosmetic_unlocks WHERE cosmeticId = :cosmeticId)")
    suspend fun isCosmeticUnlocked(cosmeticId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWallet(wallet: SupportWalletEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(transaction: SupportRewardTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnlock(unlock: SupportCosmeticUnlockEntity): Long

    @Query("DELETE FROM support_cosmetic_unlocks WHERE cosmeticId = :cosmeticId")
    suspend fun deleteUnlock(cosmeticId: String)

    @Query(
        """
        UPDATE support_wallet
        SET balance = :balance,
            lifetimeEarnedPoints = :lifetimeEarnedPoints,
            supporterBadgeUnlocked = :supporterBadgeUnlocked,
            legacyPointsImported = 1
        WHERE id = 1
        """
    )
    suspend fun writeLegacyImport(
        balance: Int,
        lifetimeEarnedPoints: Int,
        supporterBadgeUnlocked: Boolean,
    ): Int

    @Query(
        """
        UPDATE support_wallet
        SET balance = balance + :points,
            lifetimeEarnedPoints = lifetimeEarnedPoints + :points,
            supporterBadgeUnlocked = CASE
                WHEN lifetimeEarnedPoints + :points >= :badgeThreshold THEN 1
                ELSE supporterBadgeUnlocked
            END
        WHERE id = 1
          AND balance <= :maximumPoints - :points
          AND lifetimeEarnedPoints <= :maximumPoints - :points
        """
    )
    suspend fun addPoints(points: Int, badgeThreshold: Int, maximumPoints: Int): Int

    @Query(
        "UPDATE support_wallet SET balance = balance - :points WHERE id = 1 AND balance >= :points"
    )
    suspend fun spendPoints(points: Int): Int

    @Transaction
    suspend fun importLegacyBalanceIfNeeded(legacyBalance: Int) {
        val points = legacyBalance.coerceAtLeast(0)
        val current = getWallet()
        if (current == null) {
            insertWallet(
                SupportWalletEntity(
                    balance = points,
                    lifetimeEarnedPoints = points,
                    supporterBadgeUnlocked = points >= SUPPORTER_BADGE_THRESHOLD,
                    legacyPointsImported = true,
                ),
            )
        } else if (!current.legacyPointsImported) {
            val balance = (current.balance.toLong() + points)
                .coerceAtMost(MAXIMUM_POINTS.toLong())
                .toInt()
            val lifetimeEarned = (current.lifetimeEarnedPoints.toLong() + points)
                .coerceAtMost(MAXIMUM_POINTS.toLong())
                .toInt()
            writeLegacyImport(
                balance = balance,
                lifetimeEarnedPoints = lifetimeEarned,
                supporterBadgeUnlocked = current.supporterBadgeUnlocked ||
                    lifetimeEarned >= SUPPORTER_BADGE_THRESHOLD,
            )
        }
    }

    @Transaction
    suspend fun grantCompletedAd(eventId: String, now: Long): Boolean {
        check(getWallet()?.legacyPointsImported == true) {
            "Legacy support points must be imported before granting rewards"
        }
        val inserted = insertTransaction(
            SupportRewardTransactionEntity(
                eventId = eventId,
                pointsDelta = POINTS_PER_COMPLETED_AD,
                kind = KIND_AD_REWARD,
                createdAt = now,
            ),
        )
        if (inserted == -1L) return false
        check(
            addPoints(
                points = POINTS_PER_COMPLETED_AD,
                badgeThreshold = SUPPORTER_BADGE_THRESHOLD,
                maximumPoints = MAXIMUM_POINTS,
            ) == 1,
        ) { "The support wallet reached its maximum balance" }
        return true
    }

    @Transaction
    suspend fun redeemCosmetic(
        cosmeticId: String,
        pointCost: Int,
        now: Long,
    ): CosmeticPurchaseResult {
        check(getWallet()?.legacyPointsImported == true) {
            "Legacy support points must be imported before a purchase"
        }
        if (isCosmeticUnlocked(cosmeticId)) return CosmeticPurchaseResult.ALREADY_OWNED

        val inserted = insertUnlock(
            SupportCosmeticUnlockEntity(cosmeticId = cosmeticId, unlockedAt = now),
        )
        if (inserted == -1L) return CosmeticPurchaseResult.ALREADY_OWNED

        if (spendPoints(pointCost) != 1) {
            deleteUnlock(cosmeticId)
            return CosmeticPurchaseResult.INSUFFICIENT_POINTS
        }

        check(
            insertTransaction(
                SupportRewardTransactionEntity(
                    eventId = "cosmetic:$cosmeticId",
                    pointsDelta = -pointCost,
                    kind = KIND_COSMETIC_REDEMPTION,
                    createdAt = now,
                ),
            ) != -1L,
        ) { "The cosmetic transaction already exists without its unlock" }
        return CosmeticPurchaseResult.PURCHASED
    }

    companion object {
        private const val POINTS_PER_COMPLETED_AD = 1
        private const val SUPPORTER_BADGE_THRESHOLD = 5
        private const val MAXIMUM_POINTS = Int.MAX_VALUE
        private const val KIND_AD_REWARD = "ad_reward"
        private const val KIND_COSMETIC_REDEMPTION = "cosmetic_redemption"
    }
}
