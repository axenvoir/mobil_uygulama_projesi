package com.axenvoir.expira.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
) : ViewModel() {
    private val _restoreResult = MutableStateFlow<Boolean?>(null)
    val restoreResult = _restoreResult.asStateFlow()

    private val _isCheckingPurchases = MutableStateFlow(false)
    val isCheckingPurchases: StateFlow<Boolean> = _isCheckingPurchases.asStateFlow()

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
        if (!beginPurchaseCheck()) return
        viewModelScope.launch {
            try {
                withTimeoutOrNull(PURCHASE_QUERY_TIMEOUT_MS) {
                    billingRepository.refreshEntitlementsAwait()
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                // Keep the unchecked state visible so the user can retry.
            } finally {
                _isCheckingPurchases.value = false
            }
        }
    }

    fun restorePurchases() {
        if (!beginPurchaseCheck()) return
        _restoreResult.value = null
        viewModelScope.launch {
            try {
                _restoreResult.value = try {
                    withTimeoutOrNull(PURCHASE_QUERY_TIMEOUT_MS) {
                        billingRepository.restorePurchases()
                    } ?: false
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    false
                }
            } finally {
                _isCheckingPurchases.value = false
            }
        }
    }

    fun consumeRestoreResult() {
        _restoreResult.value = null
    }

    private fun beginPurchaseCheck(): Boolean = _isCheckingPurchases.compareAndSet(false, true)

    private companion object {
        const val PURCHASE_QUERY_TIMEOUT_MS = 30_000L
    }
}
