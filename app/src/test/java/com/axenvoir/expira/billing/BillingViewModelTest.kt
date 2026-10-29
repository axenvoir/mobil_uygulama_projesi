package com.axenvoir.expira.billing

import android.app.Activity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BillingViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun restore_result_is_exposed_when_billing_is_connected() = runTest {
        val repository = FakeBillingRepository(restoreResult = true)
        val viewModel = BillingViewModel(repository)

        viewModel.restorePurchases()
        advanceUntilIdle()

        assertThat(viewModel.restoreResult.value).isTrue()
        viewModel.consumeRestoreResult()
        assertThat(viewModel.restoreResult.value).isNull()
    }

    @Test
    fun restore_result_is_false_when_billing_is_unavailable() = runTest {
        val repository = FakeBillingRepository(restoreResult = false)
        val viewModel = BillingViewModel(repository)

        viewModel.restorePurchases()
        advanceUntilIdle()

        assertThat(viewModel.restoreResult.value).isFalse()
    }

    private class FakeBillingRepository(
        restoreResult: Boolean,
    ) : BillingRepository {
        override val userTier = MutableStateFlow(UserTier.FREE)
        override val isPremium = MutableStateFlow(false)
        private val result = restoreResult

        override fun refreshEntitlements() = Unit
        override suspend fun purchaseSupporter(activity: Activity) = false
        override suspend fun purchasePremium(activity: Activity) = false
        override suspend fun purchaseLifetime(activity: Activity) = false
        override suspend fun restorePurchases() = result
    }
}
