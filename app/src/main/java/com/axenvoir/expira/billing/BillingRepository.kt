package com.axenvoir.expira.billing

import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    val userTier: StateFlow<UserTier>
    val isPremium: StateFlow<Boolean>
    fun refreshEntitlements()
    suspend fun purchaseSupporter(activity: android.app.Activity): Boolean
    suspend fun purchasePremium(activity: android.app.Activity): Boolean
    suspend fun purchaseLifetime(activity: android.app.Activity): Boolean
    suspend fun restorePurchases(): Boolean
}
