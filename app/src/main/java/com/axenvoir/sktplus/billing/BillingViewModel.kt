package com.axenvoir.sktplus.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
) : ViewModel() {

    val userTier: StateFlow<UserTier> = billingRepository.userTier
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserTier.FREE)

    val isPremium: StateFlow<Boolean> = billingRepository.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun purchaseSupporter(activity: android.app.Activity) {
        viewModelScope.launch {
            billingRepository.purchaseSupporter(activity)
        }
    }

    fun purchasePremium(activity: android.app.Activity) {
        viewModelScope.launch {
            billingRepository.purchasePremium(activity)
        }
    }

    fun purchaseLifetime(activity: android.app.Activity) {
        viewModelScope.launch {
            billingRepository.purchaseLifetime(activity)
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            billingRepository.restorePurchases()
        }
    }
}
