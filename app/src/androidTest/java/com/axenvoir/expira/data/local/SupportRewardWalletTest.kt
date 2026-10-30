package com.axenvoir.expira.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.axenvoir.expira.data.repository.SupportRewardRepositoryImpl
import com.axenvoir.expira.domain.model.CosmeticPurchaseResult
import com.axenvoir.expira.domain.model.SupportCosmeticCatalog
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SupportRewardWalletTest {
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: SupportRewardRepositoryImpl

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences(LEGACY_PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = SupportRewardRepositoryImpl(database.supportRewardDao(), context)
    }

    @After
    fun tearDown() {
        database.close()
        context.getSharedPreferences(LEGACY_PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun imports_legacy_points_once_rewards_each_ad_once_and_keeps_badge_after_spend() = runBlocking {
        context.getSharedPreferences(LEGACY_PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putInt(LEGACY_POINTS_KEY, 4)
            .commit()

        repository.importLegacyBalanceIfNeeded()
        val imported = repository.wallet.first { it.isReady }
        assertThat(imported.spendablePoints).isEqualTo(4)
        assertThat(imported.lifetimeEarnedPoints).isEqualTo(4)
        assertThat(imported.supporterBadgeUnlocked).isFalse()

        assertThat(repository.grantCompletedAd("ad-session-unique")).isTrue()
        assertThat(repository.grantCompletedAd("ad-session-unique")).isFalse()
        val afterReward = repository.wallet.first { it.spendablePoints == 5 }
        assertThat(afterReward.lifetimeEarnedPoints).isEqualTo(5)
        assertThat(afterReward.supporterBadgeUnlocked).isTrue()

        assertThat(
            repository.redeemCosmetic(SupportCosmeticCatalog.QUIET_DUSK_ID),
        ).isEqualTo(CosmeticPurchaseResult.PURCHASED)
        val afterSpend = repository.wallet.first { SupportCosmeticCatalog.QUIET_DUSK_ID in it.ownedCosmeticIds }
        assertThat(afterSpend.spendablePoints).isEqualTo(0)
        assertThat(afterSpend.lifetimeEarnedPoints).isEqualTo(5)
        assertThat(afterSpend.supporterBadgeUnlocked).isTrue()
        assertThat(
            repository.redeemCosmetic(SupportCosmeticCatalog.QUIET_DUSK_ID),
        ).isEqualTo(CosmeticPurchaseResult.ALREADY_OWNED)
    }

    @Test
    fun insufficient_balance_does_not_unlock_or_spend_points() = runBlocking {
        context.getSharedPreferences(LEGACY_PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putInt(LEGACY_POINTS_KEY, 2)
            .commit()
        repository.importLegacyBalanceIfNeeded()

        assertThat(
            repository.redeemCosmetic(SupportCosmeticCatalog.QUIET_DUSK_ID),
        ).isEqualTo(CosmeticPurchaseResult.INSUFFICIENT_POINTS)
        val wallet = repository.wallet.first { it.isReady }
        assertThat(wallet.spendablePoints).isEqualTo(2)
        assertThat(wallet.ownedCosmeticIds).isEmpty()
    }

    @Test
    fun legacy_balance_is_not_imported_again_after_preferences_change() = runBlocking {
        context.getSharedPreferences(LEGACY_PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putInt(LEGACY_POINTS_KEY, 2)
            .commit()
        repository.importLegacyBalanceIfNeeded()

        context.getSharedPreferences(LEGACY_PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putInt(LEGACY_POINTS_KEY, 100)
            .commit()
        repository.importLegacyBalanceIfNeeded()

        val wallet = repository.wallet.first { it.isReady }
        assertThat(wallet.spendablePoints).isEqualTo(2)
        assertThat(wallet.lifetimeEarnedPoints).isEqualTo(2)
    }

    private companion object {
        const val LEGACY_PREFERENCES = "support_reward_preferences"
        const val LEGACY_POINTS_KEY = "support_points"
    }
}
