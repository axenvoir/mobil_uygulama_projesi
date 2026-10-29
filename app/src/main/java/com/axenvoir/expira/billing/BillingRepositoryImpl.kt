package com.axenvoir.expira.billing

import android.app.Activity
import com.android.billingclient.api.Purchase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    private val billingManager: BillingManager,
    @ApplicationContext private val context: android.content.Context,
) : BillingRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _userTier = MutableStateFlow(UserTier.FREE)
    override val userTier: StateFlow<UserTier> = _userTier.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    override val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    init {
        billingManager.connect()
        scope.launch {
            billingManager.purchases.collect { purchases ->
                updateTierFromPurchases(purchases)
            }
        }
    }

    private fun updateTierFromPurchases(purchases: List<Purchase>) {
        val activePurchases = purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        val tier = when {
            activePurchases.any { it.products.contains("premium_lifetime") } -> UserTier.LIFETIME
            activePurchases.any { it.products.contains("premium_monthly") || it.products.contains("premium_yearly") } -> UserTier.PREMIUM
            activePurchases.any { it.products.contains("supporter_monthly") || it.products.contains("supporter_yearly") } -> UserTier.SUPPORTER
            else -> UserTier.FREE
        }
        _userTier.value = tier
        _isPremium.value = tier == UserTier.PREMIUM || tier == UserTier.LIFETIME
    }

    override fun refreshEntitlements() {
        // Re-query purchases
    }

    override suspend fun purchaseSupporter(activity: Activity): Boolean {
        val details = billingManager.getProductDetails("supporter_monthly") ?: return false
        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return false
        billingManager.launchPurchaseFlow(activity, details, offerToken)
        return true
    }

    override suspend fun purchasePremium(activity: Activity): Boolean {
        val details = billingManager.getProductDetails("premium_monthly") ?: return false
        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return false
        billingManager.launchPurchaseFlow(activity, details, offerToken)
        return true
    }

    override suspend fun purchaseLifetime(activity: Activity): Boolean {
        val details = billingManager.getProductDetails("premium_lifetime") ?: return false
        billingManager.launchPurchaseFlow(activity, details)
        return true
    }

    override suspend fun restorePurchases(): Boolean {
        billingManager.refreshPurchases()
        return true
    }
}
