package com.axenvoir.expira.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : PurchasesUpdatedListener {

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enableAutoServiceReconnection()
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()

    private val _productDetails = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetails: StateFlow<List<ProductDetails>> = _productDetails.asStateFlow()

    private val _purchaseResults = MutableSharedFlow<PurchaseResult>(extraBufferCapacity = 1)
    val purchaseResults: SharedFlow<PurchaseResult> = _purchaseResults.asSharedFlow()

    fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _connectionState.value = true
                    queryPurchases()
                    queryProductDetails()
                }
            }

            override fun onBillingServiceDisconnected() {
                _connectionState.value = false
            }
        })
    }

    fun disconnect() {
        billingClient.endConnection()
        _connectionState.value = false
    }

    private fun queryPurchases() {
        val productTypes = listOf(
            BillingClient.ProductType.SUBS,
            BillingClient.ProductType.INAPP,
        )
        val responses = mutableListOf<List<Purchase>>()
        var remaining = productTypes.size
        var queryFailed = false
        val lock = Any()

        fun onQueryFinished(billingResult: BillingResult, purchases: List<Purchase>) {
            val allPurchases = synchronized(lock) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    responses += purchases
                } else {
                    queryFailed = true
                }
                remaining -= 1
                if (remaining == 0 && !queryFailed) responses.flatten() else null
            }
            if (allPurchases != null) {
                val distinctPurchases = allPurchases.distinctBy { it.purchaseToken }
                _purchases.value = distinctPurchases
                acknowledgePurchases(distinctPurchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED })
            }
        }

        productTypes.forEach { productType ->
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(productType)
                .build()
            billingClient.queryPurchasesAsync(params) { result, purchases ->
                onQueryFinished(result, purchases)
            }
        }
    }

    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("supporter_monthly")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("supporter_yearly")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("premium_monthly")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("premium_yearly")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("premium_lifetime")
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _productDetails.value = productDetailsResult.productDetailsList
            }
        }
    }

    fun refreshPurchases() {
        if (_connectionState.value) {
            queryPurchases()
        } else {
            connect()
        }
    }

    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails, offerToken: String? = null): BillingResult {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .apply { if (offerToken != null) setOfferToken(offerToken) }
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        return billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when {
            result.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty() -> {
                _purchases.value = purchases
                acknowledgePurchases(purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED })
                val pending = purchases.any { it.purchaseState == Purchase.PurchaseState.PENDING }
                _purchaseResults.tryEmit(if (pending) PurchaseResult.Pending else PurchaseResult.Success)
            }
            result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseResults.tryEmit(PurchaseResult.Cancelled)
            }
            else -> {
                _purchaseResults.tryEmit(PurchaseResult.Error(result.responseCode))
            }
        }
    }

    private fun acknowledgePurchases(purchases: List<Purchase>) {
        purchases.forEach { purchase ->
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(params) { result ->
                    if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                        android.util.Log.w("BillingManager", "Acknowledge failed: ${result.debugMessage}")
                    }
                }
            }
        }
    }

    fun getProductDetails(productId: String): ProductDetails? {
        return _productDetails.value.find { it.productId == productId }
    }
}

sealed interface PurchaseResult {
    data object Success : PurchaseResult
    data object Pending : PurchaseResult
    data object Cancelled : PurchaseResult
    data class Error(val responseCode: Int) : PurchaseResult
}
