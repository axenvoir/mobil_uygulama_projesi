package com.axenvoir.expira.billing

import kotlinx.coroutines.flow.StateFlow

/** Legacy Play purchases are retained only to honor existing paid, ad-free entitlements. */
interface BillingRepository {
    val entitlementState: StateFlow<BillingEntitlementState>
    val userTier: StateFlow<UserTier>
    val entitlementsChecked: StateFlow<Boolean>
    fun refreshEntitlements()
    suspend fun refreshEntitlementsAwait(): Boolean
    suspend fun restorePurchases(): Boolean
}

data class BillingEntitlementState(
    val userTier: UserTier = UserTier.FREE,
    val entitlementsChecked: Boolean = false,
) {
    val canShowAds: Boolean
        get() = entitlementsChecked && userTier == UserTier.FREE
}
