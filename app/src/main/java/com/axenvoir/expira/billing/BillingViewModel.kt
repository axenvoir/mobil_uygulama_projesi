package com.axenvoir.expira.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
) : ViewModel() {
    private val _restoreResult = MutableStateFlow<Boolean?>(null)
    val restoreResult = _restoreResult.asStateFlow()

    val userTier: StateFlow<UserTier> = billingRepository.userTier.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        UserTier.FREE,
    )

    val entitlementsChecked: StateFlow<Boolean> = billingRepository.entitlementsChecked.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        false,
    )

    fun refreshEntitlements() {
        billingRepository.refreshEntitlements()
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
