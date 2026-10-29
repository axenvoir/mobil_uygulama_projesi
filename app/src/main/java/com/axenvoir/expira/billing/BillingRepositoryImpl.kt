package com.axenvoir.expira.billing

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
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
) : BillingRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _userTier = MutableStateFlow(UserTier.FREE)
    override val userTier: StateFlow<UserTier> = _userTier.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    override val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _billingState = MutableStateFlow(BillingState())
    override val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private val _offers = MutableStateFlow<List<BillingOffer>>(emptyList())
    override val offers: StateFlow<List<BillingOffer>> = _offers.asStateFlow()

    init {
        billingManager.connect()
        scope.launch {
            billingManager.purchases.collect { purchases -> updateTierFromPurchases(purchases) }
        }
        scope.launch {
            billingManager.productDetails.collect { details ->
                _offers.value = details.mapNotNull { product ->
                    val subscriptionOffer = product.subscriptionOfferDetails?.firstOrNull()
                    val price = subscriptionOffer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                        ?: product.oneTimePurchaseOfferDetails?.formattedPrice
                    price?.let {
                        BillingOffer(
                            productId = product.productId,
                            title = product.title,
                            formattedPrice = it,
                            billingPeriod = subscriptionOffer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod,
                            isSubscription = subscriptionOffer != null,
                        )
                    }
                }
            }
        }
        scope.launch {
            billingManager.purchaseResults.collect { result ->
                _billingState.value = BillingState(
                    operation = when (result) {
                        PurchaseResult.Success -> BillingOperation.SUCCESS
                        PurchaseResult.Pending -> BillingOperation.PENDING
                        PurchaseResult.Cancelled -> BillingOperation.CANCELLED
                        is PurchaseResult.Error -> BillingOperation.ERROR
                    },
                    productId = _billingState.value.productId,
                )
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

    override fun refreshEntitlements() = billingManager.refreshPurchases()

    override suspend fun purchaseSupporter(activity: Activity): Boolean =
        launchSubscription(activity, "supporter_monthly")

    override suspend fun purchaseSupporterYearly(activity: Activity): Boolean =
        launchSubscription(activity, "supporter_yearly")

    override suspend fun purchasePremium(activity: Activity): Boolean =
        launchSubscription(activity, "premium_monthly")

    override suspend fun purchasePremiumYearly(activity: Activity): Boolean =
        launchSubscription(activity, "premium_yearly")

    override suspend fun purchaseLifetime(activity: Activity): Boolean {
        val productId = "premium_lifetime"
        val details = billingManager.getProductDetails(productId)
        if (details == null) {
            _billingState.value = BillingState(BillingOperation.UNAVAILABLE, productId)
            return false
        }
        _billingState.value = BillingState(BillingOperation.LOADING, productId)
        val result = billingManager.launchPurchaseFlow(activity, details)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _billingState.value = BillingState(BillingOperation.ERROR, productId)
            return false
        }
        return true
    }

    override suspend fun restorePurchases(): Boolean {
        val wasConnected = billingManager.connectionState.value
        billingManager.refreshPurchases()
        return wasConnected
    }

    private fun launchSubscription(activity: Activity, productId: String): Boolean {
        val details = billingManager.getProductDetails(productId)
        val offerToken = details?.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (details == null || offerToken == null) {
            _billingState.value = BillingState(BillingOperation.UNAVAILABLE, productId)
            return false
        }
        _billingState.value = BillingState(BillingOperation.LOADING, productId)
        val result = billingManager.launchPurchaseFlow(activity, details, offerToken)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _billingState.value = BillingState(BillingOperation.ERROR, productId)
            return false
        }
        return true
    }
}
