package com.axenvoir.expira.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
) : ViewModel() {

    private val _restoreResult = MutableStateFlow<Boolean?>(null)
    val restoreResult = _restoreResult.asStateFlow()

    val userTier: StateFlow<UserTier> = billingRepository.userTier
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserTier.FREE)

    val isPremium: StateFlow<Boolean> = billingRepository.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val billingState: StateFlow<BillingState> = billingRepository.billingState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BillingState())

    val offers: StateFlow<List<BillingOffer>> = billingRepository.offers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
            _restoreResult.value = billingRepository.restorePurchases()
        }
    }

    fun consumeRestoreResult() {
        _restoreResult.value = null
    }
}
