package com.axenvoir.expira.billing

import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/** Read-only compatibility for purchases created by older releases. */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext context: Context,
) : PurchasesUpdatedListener {
    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enableAutoServiceReconnection()
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build(),
        )
        .build()

    private val ackScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectionLock = Any()
    private var connecting = false
    private val queryGeneration = AtomicLong(0)
    private val acknowledgementsInFlight = ConcurrentHashMap.newKeySet<String>()

    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

    private val _purchaseQueryState = MutableStateFlow(PurchaseQueryState())
    val purchaseQueryState: StateFlow<PurchaseQueryState> = _purchaseQueryState.asStateFlow()

    fun connect() {
        synchronized(connectionLock) {
            if (billingClient.isReady || connecting) return
            connecting = true
        }
        _connectionState.value = false
        markEntitlementsUnchecked()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                synchronized(connectionLock) { connecting = false }
                val ready = billingResult.responseCode == BillingClient.BillingResponseCode.OK
                _connectionState.value = ready
                if (ready) queryPurchases()
                else {
                    markEntitlementsUnchecked()
                    Log.w(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                synchronized(connectionLock) { connecting = false }
                _connectionState.value = false
                queryGeneration.incrementAndGet()
                markEntitlementsUnchecked()
            }
        })
    }

    fun refreshPurchases() {
        if (billingClient.isReady) queryPurchases() else connect()
    }

    suspend fun refreshPurchasesAwait(): Boolean {
        if (!awaitConnection()) return false
        return suspendCancellableCoroutine { continuation ->
            queryPurchases { success ->
                if (continuation.isActive) continuation.resume(success)
            }
        }
    }

    private suspend fun awaitConnection(): Boolean {
        if (billingClient.isReady) return true
        connect()
        return withTimeoutOrNull(CONNECTION_TIMEOUT_MS) {
            connectionState.first { it }
        } ?: false
    }

    private fun queryPurchases(onComplete: (Boolean) -> Unit = {}) {
        if (!billingClient.isReady) {
            _connectionState.value = false
            markEntitlementsUnchecked()
            onComplete(false)
            connect()
            return
        }

        markEntitlementsUnchecked()
        val generation = queryGeneration.incrementAndGet()
        val productTypes = listOf(BillingClient.ProductType.SUBS, BillingClient.ProductType.INAPP)
        val responses = mutableListOf<Purchase>()
        val lock = Any()
        var remaining = productTypes.size
        var queryFailed = false

        productTypes.forEach { productType ->
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(productType)
                .build()
            billingClient.queryPurchasesAsync(params) { result, purchases ->
                val completed = synchronized(lock) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        responses.addAll(purchases)
                    } else {
                        queryFailed = true
                        Log.w(TAG, "Purchase query failed ($productType): ${result.debugMessage}")
                    }
                    remaining -= 1
                    if (remaining == 0) QueryOutcome(!queryFailed, responses.toList()) else null
                }

                completed?.let { outcome ->
                    val isCurrentQuery = generation == queryGeneration.get() && billingClient.isReady
                    if (isCurrentQuery) {
                        if (outcome.success) {
                            val currentPurchases = choosePurchaseState(outcome.purchases)
                            _purchaseQueryState.value = PurchaseQueryState(
                                purchases = currentPurchases,
                                entitlementsChecked = true,
                            )
                            acknowledgePurchases(currentPurchases)
                        } else {
                            markEntitlementsUnchecked()
                        }
                    }
                    onComplete(outcome.success && isCurrentQuery)
                }
            }
        }
    }

    private fun acknowledgePurchases(purchases: List<Purchase>) {
        purchases.asSequence()
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
            .forEach(::acknowledgeWithRetry)
    }

    private fun acknowledgeWithRetry(purchase: Purchase) {
        if (!acknowledgementsInFlight.add(purchase.purchaseToken)) return
        ackScope.launch {
            try {
                repeat(ACK_ATTEMPTS) { attempt ->
                    if (!awaitConnection()) return@launch
                    val result = suspendCancellableCoroutine<BillingResult> { continuation ->
                        val params = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient.acknowledgePurchase(params) { billingResult ->
                            if (continuation.isActive) continuation.resume(billingResult)
                        }
                    }
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) return@launch
                    Log.w(TAG, "Purchase acknowledgement attempt ${attempt + 1} failed: ${result.debugMessage}")
                    if (attempt < ACK_ATTEMPTS - 1) delay(ACK_RETRY_BASE_MS * (1L shl attempt))
                }
            } finally {
                acknowledgementsInFlight.remove(purchase.purchaseToken)
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> if (!purchases.isNullOrEmpty()) refreshPurchases()
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            else -> Log.w(TAG, "Purchases update failed: ${result.debugMessage}")
        }
    }

    private fun choosePurchaseState(purchases: List<Purchase>): List<Purchase> =
        purchases.groupBy { it.purchaseToken }.values.mapNotNull { sameToken ->
            sameToken.maxByOrNull { purchase ->
                when (purchase.purchaseState) {
                    Purchase.PurchaseState.PURCHASED -> 2
                    Purchase.PurchaseState.PENDING -> 1
                    else -> 0
                }
            }
        }

    private fun markEntitlementsUnchecked() {
        val current = _purchaseQueryState.value
        if (current.entitlementsChecked) {
            _purchaseQueryState.value = current.copy(entitlementsChecked = false)
        }
    }

    private data class QueryOutcome(
        val success: Boolean,
        val purchases: List<Purchase>,
    )

    companion object {
        private const val TAG = "BillingManager"
        private const val CONNECTION_TIMEOUT_MS = 15_000L
        private const val ACK_ATTEMPTS = 3
        private const val ACK_RETRY_BASE_MS = 750L
    }
}

data class PurchaseQueryState(
    val purchases: List<Purchase> = emptyList(),
    val entitlementsChecked: Boolean = false,
)
