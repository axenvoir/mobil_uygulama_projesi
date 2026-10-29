package com.axenvoir.expira.billing

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
        try {
            val viewModel = BillingViewModel(FakeBillingRepository(restoreResult = true))

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
        try {
            val viewModel = BillingViewModel(FakeBillingRepository(restoreResult = false))

            viewModel.restorePurchases()
            advanceUntilIdle()

            assertThat(viewModel.restoreResult.value).isFalse()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun ad_eligibility_requires_a_checked_free_entitlement_snapshot() {
        assertThat(BillingEntitlementState().canShowAds).isFalse()
        assertThat(
            BillingEntitlementState(
                userTier = UserTier.FREE,
                entitlementsChecked = true,
            ).canShowAds,
        ).isTrue()
        assertThat(
            BillingEntitlementState(
                userTier = UserTier.PREMIUM,
                entitlementsChecked = true,
            ).canShowAds,
        ).isFalse()
    }

    private class FakeBillingRepository(
        private val restoreResult: Boolean,
    ) : BillingRepository {
        override val entitlementState = MutableStateFlow(BillingEntitlementState())
        override val userTier = MutableStateFlow(UserTier.FREE)
        override val entitlementsChecked = MutableStateFlow(false)

        override fun refreshEntitlements() = Unit
        override suspend fun restorePurchases() = restoreResult
    }
}
