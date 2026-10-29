package com.axenvoir.expira.billing

import android.app.Activity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BillingViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun restore_result_is_exposed_when_billing_is_connected() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeBillingRepository(restoreResult = true)
        try {
            val viewModel = BillingViewModel(repository)

            viewModel.restorePurchases()
            advanceUntilIdle()

            assertThat(viewModel.restoreResult.value).isTrue()
            viewModel.consumeRestoreResult()
            assertThat(viewModel.restoreResult.value).isNull()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun restore_result_is_false_when_billing_is_unavailable() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeBillingRepository(restoreResult = false)
        try {
            val viewModel = BillingViewModel(repository)

            viewModel.restorePurchases()
            advanceUntilIdle()

            assertThat(viewModel.restoreResult.value).isFalse()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class FakeBillingRepository(
        restoreResult: Boolean,
    ) : BillingRepository {
        override val userTier = MutableStateFlow(UserTier.FREE)
        override val isPremium = MutableStateFlow(false)
        override val billingState = MutableStateFlow(BillingState())
        override val offers = MutableStateFlow(emptyList<BillingOffer>())
        private val result = restoreResult

        override fun refreshEntitlements() = Unit
        override suspend fun purchaseSupporter(activity: Activity) = false
        override suspend fun purchasePremium(activity: Activity) = false
        override suspend fun purchaseLifetime(activity: Activity) = false
        override suspend fun restorePurchases() = result
    }
}
