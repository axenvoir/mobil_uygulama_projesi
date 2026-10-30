package com.axenvoir.expira.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "support_wallet")
data class SupportWalletEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val balance: Int,
    val lifetimeEarnedPoints: Int,
    val supporterBadgeUnlocked: Boolean,
    val legacyPointsImported: Boolean,
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}

@Entity(tableName = "support_reward_transactions")
data class SupportRewardTransactionEntity(
    @PrimaryKey val eventId: String,
    val pointsDelta: Int,
    val kind: String,
    val createdAt: Long,
)

@Entity(tableName = "support_cosmetic_unlocks")
data class SupportCosmeticUnlockEntity(
    @PrimaryKey val cosmeticId: String,
    val unlockedAt: Long,
)
