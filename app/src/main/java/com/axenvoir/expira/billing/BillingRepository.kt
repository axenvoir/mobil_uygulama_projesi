package com.axenvoir.expira.billing

import kotlinx.coroutines.flow.StateFlow

/** Legacy Play purchases are retained only to honor existing paid, ad-free entitlements. */
interface BillingRepository {
    val userTier: StateFlow<UserTier>
    val entitlementsChecked: StateFlow<Boolean>
    fun refreshEntitlements()
    suspend fun restorePurchases(): Boolean
}
