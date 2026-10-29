package com.axenvoir.expira.billing

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

enum class BillingOperation {
    IDLE,
    LOADING,
    SUCCESS,
    CANCELLED,
    PENDING,
    UNAVAILABLE,
    ERROR,
}

data class BillingState(
    val operation: BillingOperation = BillingOperation.IDLE,
    val productId: String? = null,
)

data class BillingOffer(
    val productId: String,
    val title: String,
    val formattedPrice: String,
    val billingPeriod: String? = null,
    val isSubscription: Boolean,
)

interface BillingRepository {
    val userTier: StateFlow<UserTier>
    val isPremium: StateFlow<Boolean>
    val billingState: StateFlow<BillingState>
    val offers: StateFlow<List<BillingOffer>>
    fun refreshEntitlements()
    suspend fun purchaseSupporter(activity: Activity): Boolean
    suspend fun purchasePremium(activity: Activity): Boolean
    suspend fun purchaseLifetime(activity: Activity): Boolean
    suspend fun restorePurchases(): Boolean
}
