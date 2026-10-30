package com.axenvoir.expira.billing

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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
            val viewModel = BillingViewModel(FakeBillingRepository(restoreAction = { true }))

            viewModel.restorePurchases()
            advanceUntilIdle()

            assertThat(viewModel.restoreResult.value).isTrue()
            assertThat(viewModel.isCheckingPurchases.value).isFalse()
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
            val viewModel = BillingViewModel(FakeBillingRepository(restoreAction = { false }))

            viewModel.restorePurchases()
            advanceUntilIdle()

            assertThat(viewModel.restoreResult.value).isFalse()
            assertThat(viewModel.isCheckingPurchases.value).isFalse()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun refresh_and_restore_queries_share_one_in_flight_gate() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val refreshGate = CompletableDeferred<Boolean>()
        val restoreGate = CompletableDeferred<Boolean>()
        val repository = FakeBillingRepository(
            restoreAction = { restoreGate.await() },
            refreshAction = { refreshGate.await() },
        )
        try {
            val viewModel = BillingViewModel(repository)

            viewModel.refreshEntitlements()
            runCurrent()
            assertThat(viewModel.isCheckingPurchases.value).isTrue()
            assertThat(repository.refreshCalls).isEqualTo(1)

            viewModel.refreshEntitlements()
            viewModel.restorePurchases()
            runCurrent()
            assertThat(repository.refreshCalls).isEqualTo(1)
            assertThat(repository.restoreCalls).isEqualTo(0)

            refreshGate.complete(true)
            advanceUntilIdle()
            assertThat(viewModel.isCheckingPurchases.value).isFalse()

            viewModel.restorePurchases()
            viewModel.restorePurchases()
            runCurrent()
            assertThat(viewModel.isCheckingPurchases.value).isTrue()
            assertThat(repository.restoreCalls).isEqualTo(1)
            viewModel.refreshEntitlements()
            runCurrent()
            assertThat(repository.refreshCalls).isEqualTo(1)

            restoreGate.complete(true)
            advanceUntilIdle()
            assertThat(viewModel.restoreResult.value).isTrue()
            assertThat(viewModel.isCheckingPurchases.value).isFalse()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun restore_exception_is_reported_as_unavailable_and_releases_the_gate() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val viewModel = BillingViewModel(
                FakeBillingRepository(
                    restoreAction = { throw IllegalStateException("Simulated Play query failure") },
                ),
            )

            viewModel.restorePurchases()
            advanceUntilIdle()

            assertThat(viewModel.restoreResult.value).isFalse()
            assertThat(viewModel.isCheckingPurchases.value).isFalse()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun restore_timeout_releases_the_gate_and_reports_unavailable() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val viewModel = BillingViewModel(
                FakeBillingRepository(restoreAction = { awaitCancellation() }),
            )

            viewModel.restorePurchases()
            advanceUntilIdle()

            assertThat(viewModel.restoreResult.value).isFalse()
            assertThat(viewModel.isCheckingPurchases.value).isFalse()
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
        private val restoreAction: suspend () -> Boolean,
        private val refreshAction: suspend () -> Boolean = { true },
    ) : BillingRepository {
        override val entitlementState = MutableStateFlow(BillingEntitlementState())
        override val userTier = MutableStateFlow(UserTier.FREE)
        override val entitlementsChecked = MutableStateFlow(false)
        var refreshCalls = 0
            private set
        var restoreCalls = 0
            private set

        override fun refreshEntitlements() = Unit

        override suspend fun refreshEntitlementsAwait(): Boolean {
            refreshCalls += 1
            return refreshAction()
        }

        override suspend fun restorePurchases(): Boolean {
            restoreCalls += 1
            return restoreAction()
        }
    }
}
