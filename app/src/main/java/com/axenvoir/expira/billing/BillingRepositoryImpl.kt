package com.axenvoir.expira.billing

import com.android.billingclient.api.Purchase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    private val billingManager: BillingManager,
) : BillingRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _userTier = MutableStateFlow(UserTier.FREE)
    override val userTier: StateFlow<UserTier> = _userTier.asStateFlow()

    override val entitlementsChecked: StateFlow<Boolean> = billingManager.entitlementsChecked

    init {
        scope.launch {
            billingManager.purchases.collect(::updateTierFromPurchases)
        }
        billingManager.refreshPurchases()
    }

    private fun updateTierFromPurchases(purchases: List<Purchase>) {
        val activeProductIds = purchases.asSequence()
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            .flatMap { it.products.asSequence() }
            .toSet()

        _userTier.value = when {
            PRODUCT_PREMIUM_LIFETIME in activeProductIds -> UserTier.LIFETIME
            PRODUCT_PREMIUM_MONTHLY in activeProductIds || PRODUCT_PREMIUM_YEARLY in activeProductIds -> UserTier.PREMIUM
            PRODUCT_SUPPORTER_MONTHLY in activeProductIds || PRODUCT_SUPPORTER_YEARLY in activeProductIds -> UserTier.SUPPORTER
            else -> UserTier.FREE
        }
    }

    override fun refreshEntitlements() {
        billingManager.refreshPurchases()
    }

    override suspend fun restorePurchases(): Boolean = billingManager.refreshPurchasesAwait()

    private companion object {
        const val PRODUCT_SUPPORTER_MONTHLY = "supporter_monthly"
        const val PRODUCT_SUPPORTER_YEARLY = "supporter_yearly"
        const val PRODUCT_PREMIUM_MONTHLY = "premium_monthly"
        const val PRODUCT_PREMIUM_YEARLY = "premium_yearly"
        const val PRODUCT_PREMIUM_LIFETIME = "premium_lifetime"
    }
}
