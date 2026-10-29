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

    private val _entitlementState = MutableStateFlow(BillingEntitlementState())
    override val entitlementState: StateFlow<BillingEntitlementState> = _entitlementState.asStateFlow()

    private val _userTier = MutableStateFlow(UserTier.FREE)
    override val userTier: StateFlow<UserTier> = _userTier.asStateFlow()

    private val _entitlementsChecked = MutableStateFlow(false)
    override val entitlementsChecked: StateFlow<Boolean> = _entitlementsChecked.asStateFlow()

    init {
        scope.launch {
            billingManager.purchaseQueryState.collect { queryState ->
                val tier = tierFromPurchases(queryState.purchases)
                _entitlementState.value = BillingEntitlementState(
                    userTier = tier,
                    entitlementsChecked = queryState.entitlementsChecked,
                )
                _userTier.value = tier
                _entitlementsChecked.value = queryState.entitlementsChecked
            }
        }
        billingManager.refreshPurchases()
    }

    private fun tierFromPurchases(purchases: List<Purchase>): UserTier {
        val activeProductIds = purchases.asSequence()
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            .flatMap { it.products.asSequence() }
            .toSet()

        return when {
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
