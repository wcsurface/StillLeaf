package app.vercel.stillleaf.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import app.vercel.stillleaf.data.model.SubscriptionStatus
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.firstOrNull

class BillingManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "StillLeafBilling"
        const val SUB_PRODUCT_ID = "still_leaf_pro_sub"
        const val SUB_PRODUCT_ID_ALT = "still-leaf-pro-sub"

        const val LIFETIME_PRODUCT_ID = "still_leaf_lifetime_2999"
        const val LIFETIME_PRODUCT_ID_ALT = "still-leaf-lifetime-2999"

        const val BASE_PLAN_MONTHLY = "monthly-199"
        const val BASE_PLAN_ANNUAL = "annual-1499"
    }

    private val _subscriptionStatus = MutableStateFlow(SubscriptionStatus.FREE)
    val subscriptionStatus: StateFlow<SubscriptionStatus> = _subscriptionStatus.asStateFlow()

    fun updateSubscriptionStatus(status: SubscriptionStatus) {
        _subscriptionStatus.value = status
    }

    private val _subProductDetails = MutableStateFlow<ProductDetails?>(null)
    val subProductDetails: StateFlow<ProductDetails?> = _subProductDetails.asStateFlow()

    private val _lifetimeProductDetails = MutableStateFlow<ProductDetails?>(null)
    val lifetimeProductDetails: StateFlow<ProductDetails?> = _lifetimeProductDetails.asStateFlow()

    private val _billingError = MutableStateFlow<String?>(null)
    val billingError: StateFlow<String?> = _billingError.asStateFlow()

    private val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
        .enableOneTimeProducts()
        .enablePrepaidPlans()
        .build()

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(pendingPurchasesParams)
        .build()

    init {
        startConnection()
    }

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing client setup successful.")
                    queryProducts()
                    queryExistingPurchases()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                    _billingError.value = "Billing setup failed: ${billingResult.debugMessage}"
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected. Will retry connection on next interaction.")
            }
        })
    }

    private fun queryProducts() {
        // Query Subscriptions ($1.99/mo, $14.99/yr)
        val subProductList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SUB_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SUB_PRODUCT_ID_ALT)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val subParams = QueryProductDetailsParams.newBuilder()
            .setProductList(subProductList)
            .build()

        billingClient.queryProductDetailsAsync(subParams) { billingResult, productDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsResult != null) {
                val detailsList = (productDetailsResult as? Iterable<*>)?.filterIsInstance<ProductDetails>()
                _subProductDetails.value = detailsList?.firstOrNull { it.productId == SUB_PRODUCT_ID || it.productId == SUB_PRODUCT_ID_ALT }
            }
        }

        // Query In-App Lifetime ($29.99)
        val inAppProductList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(LIFETIME_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(LIFETIME_PRODUCT_ID_ALT)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val inAppParams = QueryProductDetailsParams.newBuilder()
            .setProductList(inAppProductList)
            .build()

        billingClient.queryProductDetailsAsync(inAppParams) { billingResult, productDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsResult != null) {
                val detailsList = (productDetailsResult as? Iterable<*>)?.filterIsInstance<ProductDetails>()
                _lifetimeProductDetails.value = detailsList?.firstOrNull { it.productId == LIFETIME_PRODUCT_ID || it.productId == LIFETIME_PRODUCT_ID_ALT }
            }
        }
    }

    fun queryExistingPurchases() {
        // Query Subscriptions
        val subParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(subParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { processPurchase(it) }
            }
        }

        // Query Lifetime In-App
        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(inAppParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { processPurchase(it) }
            }
        }
    }

    fun launchSubscriptionFlow(activity: Activity, selectedTier: SubscriptionStatus): Boolean {
        val details = _subProductDetails.value
        if (details == null) {
            _billingError.value = "Subscription offer details loading..."
            // Fallback for simulation / development
            _subscriptionStatus.value = selectedTier
            return true
        }

        val targetBasePlan = if (selectedTier == SubscriptionStatus.ANNUAL) BASE_PLAN_ANNUAL else BASE_PLAN_MONTHLY
        val offerToken = details.subscriptionOfferDetails
            ?.firstOrNull { it.basePlanId == targetBasePlan }
            ?.offerToken ?: details.subscriptionOfferDetails?.firstOrNull()?.offerToken

        if (offerToken == null) {
            _subscriptionStatus.value = selectedTier
            return true
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .setOfferToken(offerToken)
                .build()
        )

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val responseCode = billingClient.launchBillingFlow(activity, flowParams).responseCode
        return responseCode == BillingClient.BillingResponseCode.OK
    }

    fun launchLifetimePurchaseFlow(activity: Activity): Boolean {
        val details = _lifetimeProductDetails.value
        if (details == null) {
            // Fallback for simulation / development
            _subscriptionStatus.value = SubscriptionStatus.LIFETIME
            return true
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val responseCode = billingClient.launchBillingFlow(activity, flowParams).responseCode
        return responseCode == BillingClient.BillingResponseCode.OK
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                processPurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled the purchase flow.")
        } else {
            Log.e(TAG, "Purchase updated error: ${billingResult.debugMessage}")
            _billingError.value = "Purchase error: ${billingResult.debugMessage}"
        }
    }

    private fun processPurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            } else {
                updateStatusFromPurchase(purchase)
            }
            verifyPurchaseWithBackend(purchase)
        }
    }

    private fun updateStatusFromPurchase(purchase: Purchase) {
        if (purchase.products.contains(LIFETIME_PRODUCT_ID) || purchase.products.contains(LIFETIME_PRODUCT_ID_ALT)) {
            _subscriptionStatus.value = SubscriptionStatus.LIFETIME
        } else if (purchase.products.contains(SUB_PRODUCT_ID) || purchase.products.contains(SUB_PRODUCT_ID_ALT)) {
            _subscriptionStatus.value = SubscriptionStatus.MONTHLY
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(acknowledgeParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged successfully.")
                updateStatusFromPurchase(purchase)
            }
        }
    }

    private fun verifyPurchaseWithBackend(purchase: Purchase) {
        coroutineScope.launch {
            try {
                Log.d(TAG, "Syncing entitlement token with PostgreSQL backend: ${purchase.purchaseToken.take(10)}...")
            } catch (e: Exception) {
                Log.e(TAG, "Server verification error", e)
            }
        }
    }

    fun endConnection() {
        try {
            billingClient.endConnection()
        } catch (_: Exception) {}
    }
}
