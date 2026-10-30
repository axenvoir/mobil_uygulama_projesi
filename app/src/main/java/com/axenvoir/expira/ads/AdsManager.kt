package com.axenvoir.expira.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.axenvoir.expira.BuildConfig
import com.axenvoir.expira.billing.BillingRepository
import com.axenvoir.expira.billing.UserTier
import com.axenvoir.expira.domain.repository.SupportRewardRepository
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.ref.WeakReference
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.suspendCancellableCoroutine

/** AdMob consent and the one explicitly requested rewarded-ad placement. */
@Singleton
class AdsManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val billingRepository: BillingRepository,
    private val supportRewardRepository: SupportRewardRepository,
    private val pendingRewardStore: PendingRewardStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val initializationMutex = Mutex()
    private var initialized = false
    private var loadedAd: RewardedAd? = null
    private var adRequestGeneration = 0L
    private var observedEligible = false
    private var userRequestedAd = false
    private var pendingRewardEventId: String? = pendingRewardStore.readPendingEventId()
    private var rewardSaveInFlight = false

    private var consentRequestInFlight = false
    private var consentFormInFlight = false
    private var consentRequestGeneration = 0L
    private var consentActivity: WeakReference<Activity>? = null
    private var consentOperationActivity: WeakReference<Activity>? = null
    private var privacyFormInFlight = false
    private var privacyActivity: WeakReference<Activity>? = null

    private val consentInformation = UserMessagingPlatform.getConsentInformation(context)

    private val _canRequestAds = MutableStateFlow(false)
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    private val _privacyOptionsRequired = MutableStateFlow(false)
    val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired.asStateFlow()

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<RewardedAdState> = _state.asStateFlow()

    private val _rewardEvents = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val rewardEvents: SharedFlow<Int> = _rewardEvents.asSharedFlow()

    private val _rewardSaveErrors = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val rewardSaveErrors: SharedFlow<Unit> = _rewardSaveErrors.asSharedFlow()

    private val _rewardSavePending = MutableStateFlow(false)
    val rewardSavePending: StateFlow<Boolean> = _rewardSavePending.asStateFlow()

    init {
        scope.launch {
            try {
                supportRewardRepository.importLegacyBalanceIfNeeded()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.e(TAG, "Legacy support balance could not be migrated", exception)
            }
        }
        scope.launch {
            billingRepository.entitlementState
                .collect { entitlement -> onEligibilityChanged(entitlement.canShowAds) }
        }
        pendingRewardEventId?.let(::persistEarnedReward)
    }

    /** Refreshes UMP consent on app launch without retaining or using a stale Activity. */
    fun requestConsentAtLaunch(activity: Activity) {
        if (!BuildConfig.ADS_ENABLED || !isActivityAlive(activity)) return

        abandonStaleConsentOperationIfNeeded()
        consentActivity = WeakReference(activity)

        // A call from a recreated Activity may arrive while the old request/form is still
        // completing. Keep only the newest Activity and let the existing operation finish; this
        // prevents two consent forms from being presented at once.
        if (consentRequestInFlight || consentFormInFlight) return

        consentRequestInFlight = true
        val requestGeneration = ++consentRequestGeneration
        consentOperationActivity = WeakReference(activity)
        requestConsentWhenResumed(activity, requestGeneration)
    }

    fun showPrivacyOptions(activity: Activity) {
        if (!BuildConfig.ADS_ENABLED || !privacyOptionsRequired.value) return

        if (privacyFormInFlight && !isActivityAlive(privacyActivity?.get())) {
            privacyFormInFlight = false
            privacyActivity = null
        }
        if (privacyFormInFlight || !isActivityUsable(activity)) return

        privacyFormInFlight = true
        privacyActivity = WeakReference(activity)
        runCatching {
            UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
                if (!privacyFormInFlight) return@showPrivacyOptionsForm
                privacyFormInFlight = false
                privacyActivity = null
                if (formError != null) {
                    Log.w(TAG, "Privacy options form failed: ${formError.message}")
                }
                updateConsentState()
            }
        }.onFailure { error ->
            privacyFormInFlight = false
            privacyActivity = null
            Log.w(TAG, "Privacy options form could not be opened", error)
        }
    }

    /** Requests an ad only after Play purchase state and consent permit it. */
    fun prepareRewardedAd() {
        if (pendingRewardEventId != null || rewardSaveInFlight) return
        val eligible = currentEligibility()
        applyEligibilitySnapshot(eligible)

        if (!BuildConfig.ADS_ENABLED) {
            clearLoadedAd()
            userRequestedAd = false
            _state.value = RewardedAdState.CONFIGURATION_MISSING
            return
        }
        if (!eligible) {
            clearLoadedAd()
            userRequestedAd = false
            _state.value = stateForCurrentEligibility()
            return
        }
        if (_state.value == RewardedAdState.SHOWING) return
        if (!_canRequestAds.value) {
            clearLoadedAd()
            userRequestedAd = true
            _state.value = RewardedAdState.CONSENT_REQUIRED
            return
        }
        if (_state.value == RewardedAdState.LOADING || _state.value == RewardedAdState.READY) return

        userRequestedAd = true
        startRewardedAdLoad()
    }

    /** Explicit retry entry point; Play purchase state remains the source of truth. */
    fun retryRewardedAd() {
        if (_state.value == RewardedAdState.SHOWING || pendingRewardEventId != null) return
        clearLoadedAd()
        userRequestedAd = false
        _state.value = if (BuildConfig.ADS_ENABLED) {
            RewardedAdState.NOT_REQUESTED
        } else {
            RewardedAdState.CONFIGURATION_MISSING
        }
        prepareRewardedAd()
    }

    fun showRewardedAd(activity: Activity) {
        if (pendingRewardEventId != null || rewardSaveInFlight) return
        val eligible = currentEligibility()
        applyEligibilitySnapshot(eligible)
        val ad = loadedAd
        if (
            ad == null ||
            !eligible ||
            !_canRequestAds.value ||
            !isActivityUsable(activity)
        ) {
            if (!eligible) {
                _state.value = stateForCurrentEligibility()
            } else if (_state.value != RewardedAdState.SHOWING) {
                _state.value = if (_canRequestAds.value) {
                    RewardedAdState.UNAVAILABLE
                } else {
                    RewardedAdState.CONSENT_REQUIRED
                }
            }
            return
        }

        // A loaded ad is single-use. Invalidate any late load callback before handing it to the
        // SDK, and require a fresh explicit prepare/retry action for another ad.
        loadedAd = null
        adRequestGeneration += 1
        userRequestedAd = false
        val rewardDelivered = AtomicBoolean(false)
        val rewardEventId = UUID.randomUUID().toString()
        _state.value = RewardedAdState.SHOWING
        ad.adEventCallback = object : RewardedAdEventCallback {
            override fun onAdShowedFullScreenContent() = Unit

            override fun onAdDismissedFullScreenContent() {
                if (_state.value == RewardedAdState.SHOWING) {
                    _state.value = RewardedAdState.UNAVAILABLE
                }
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                Log.w(TAG, "Rewarded ad failed to show: ${fullScreenContentError.message}")
                if (_state.value == RewardedAdState.SHOWING) {
                    _state.value = RewardedAdState.UNAVAILABLE
                }
            }

            override fun onAdImpression() = Unit

            override fun onAdClicked() = Unit
        }
        runCatching {
            ad.show(activity) { rewardItem ->
                // The SDK callback proves this one-shot session was completed. A later change to
                // consent or Play entitlement must not erase a reward already earned by the user.
                if (rewardDelivered.compareAndSet(false, true)) {
                    if (rewardItem.amount != REWARD_POINTS_PER_AD) {
                        Log.w(TAG, "Rewarded-ad amount differs from the fixed in-app reward")
                    }
                    persistEarnedReward(rewardEventId)
                }
            }
        }.onFailure { error ->
            Log.w(TAG, "Rewarded ad could not be opened", error)
            if (_state.value == RewardedAdState.SHOWING) {
                _state.value = RewardedAdState.UNAVAILABLE
            }
        }
    }

    /** Retries only the exact SDK-confirmed reward event; the Room ledger prevents double credit. */
    fun retryRewardSave() {
        pendingRewardEventId?.let(::persistEarnedReward)
    }

    private fun persistEarnedReward(eventId: String) {
        if (rewardSaveInFlight) return
        pendingRewardEventId = eventId
        _rewardSavePending.value = true
        rewardSaveInFlight = true
        scope.launch {
            try {
                val wasQueuedDurably = withContext(Dispatchers.IO) {
                    pendingRewardStore.savePendingEventId(eventId)
                }
                if (!wasQueuedDurably) {
                    Log.e(TAG, "Completed rewarded-ad event could not be durably queued")
                    _rewardSaveErrors.tryEmit(Unit)
                    return@launch
                }
                supportRewardRepository.grantCompletedAd(eventId)
                if (pendingRewardEventId == eventId) {
                    pendingRewardEventId = null
                    pendingRewardStore.clearPendingEventId(eventId)
                    _rewardSavePending.value = false
                    _rewardEvents.tryEmit(REWARD_POINTS_PER_AD)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.e(TAG, "Completed rewarded-ad points could not be saved", exception)
                _rewardSaveErrors.tryEmit(Unit)
            } finally {
                rewardSaveInFlight = false
            }
        }
    }

    private fun requestConsentWhenResumed(activity: Activity, requestGeneration: Long) {
        val lifecycleOwner = activity as? LifecycleOwner
        if (lifecycleOwner == null) {
            if (isActivityUsable(activity) && isCurrentConsentRequest(requestGeneration)) {
                requestConsentInfoUpdate(requestGeneration)
            } else {
                finishConsentRequest(requestGeneration, activity)
            }
            return
        }

        val lifecycle = lifecycleOwner.lifecycle
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
            finishConsentRequest(requestGeneration, activity)
            return
        }
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            requestConsentInfoUpdate(requestGeneration)
            return
        }

        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                owner.lifecycle.removeObserver(this)
                if (isCurrentConsentRequest(requestGeneration) && isActivityAlive(activity)) {
                    requestConsentInfoUpdate(requestGeneration)
                } else {
                    finishConsentRequest(requestGeneration, activity)
                }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                owner.lifecycle.removeObserver(this)
                finishConsentRequest(requestGeneration, activity)
            }
        }
        lifecycle.addObserver(observer)
    }

    private fun requestConsentInfoUpdate(requestGeneration: Long) {
        if (!isCurrentConsentRequest(requestGeneration)) return
        val activity = consentActivity?.get()
        if (activity == null || !isActivityUsable(activity)) {
            finishConsentRequest(requestGeneration, consentOperationActivity?.get())
            return
        }

        consentOperationActivity = WeakReference(activity)
        val parameters = ConsentRequestParameters.Builder().build()
        runCatching {
            consentInformation.requestConsentInfoUpdate(
                activity,
                parameters,
                {
                    onConsentInfoUpdateSucceeded(requestGeneration)
                },
                { error ->
                    if (!isCurrentConsentRequest(requestGeneration)) return@requestConsentInfoUpdate
                    Log.w(TAG, "Consent update failed: ${error.message}")
                    finishConsentRequest(requestGeneration, activity)
                    // UMP may still permit ads based on a valid decision from the previous
                    // session, so use its current gate even after an update failure.
                    updateConsentState()
                },
            )
        }.onFailure { error ->
            if (isCurrentConsentRequest(requestGeneration)) {
                Log.w(TAG, "Consent update could not be requested", error)
                finishConsentRequest(requestGeneration, activity)
                updateConsentState()
            }
        }
    }

    private fun onConsentInfoUpdateSucceeded(requestGeneration: Long) {
        if (!isCurrentConsentRequest(requestGeneration)) return
        consentRequestInFlight = false

        val activity = consentActivity?.get()
        if (activity == null || !isActivityUsable(activity)) {
            finishConsentForm(requestGeneration, consentOperationActivity?.get())
            updateConsentState()
            return
        }

        // The request callback may race a new Activity instance. Always use the latest usable
        // reference, never the Activity captured by an old callback.
        consentFormInFlight = true
        consentOperationActivity = WeakReference(activity)
        runCatching {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                if (
                    !isCurrentConsentGeneration(requestGeneration) ||
                    !consentFormInFlight
                ) return@loadAndShowConsentFormIfRequired
                finishConsentForm(requestGeneration, activity)
                if (formError != null) {
                    Log.i(TAG, "Consent form not shown: ${formError.message}")
                }
                updateConsentState()
            }
        }.onFailure { error ->
            if (isCurrentConsentGeneration(requestGeneration)) {
                finishConsentForm(requestGeneration, activity)
                Log.w(TAG, "Consent form could not be opened", error)
                updateConsentState()
            }
        }
    }

    private fun finishConsentRequest(requestGeneration: Long, operationActivity: Activity?) {
        if (!isCurrentConsentGeneration(requestGeneration)) return
        consentRequestInFlight = false
        if (deferConsentUntilResumed()) return
        retryConsentOnReplacementActivity(requestGeneration, operationActivity)
    }

    private fun finishConsentForm(requestGeneration: Long, operationActivity: Activity?) {
        if (!isCurrentConsentGeneration(requestGeneration)) return
        consentFormInFlight = false
        consentRequestInFlight = false
        if (deferConsentUntilResumed()) return
        retryConsentOnReplacementActivity(requestGeneration, operationActivity)
    }

    private fun deferConsentUntilResumed(): Boolean {
        val latestActivity = consentActivity?.get()
        val operationActivity = consentOperationActivity?.get()
        val activity = when {
            isActivityAlive(latestActivity) -> latestActivity
            isActivityAlive(operationActivity) -> operationActivity
            else -> null
        } ?: return false
        val lifecycle = (activity as? LifecycleOwner)?.lifecycle ?: return false
        if (lifecycle.currentState == Lifecycle.State.DESTROYED ||
            lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        ) return false

        val nextGeneration = ++consentRequestGeneration
        consentActivity = WeakReference(activity)
        consentOperationActivity = WeakReference(activity)
        consentRequestInFlight = true
        requestConsentWhenResumed(activity, nextGeneration)
        return true
    }

    private fun retryConsentOnReplacementActivity(requestGeneration: Long, operationActivity: Activity?) {
        if (requestGeneration != consentRequestGeneration) return
        val replacement = consentActivity?.get()
        if (
            replacement != null &&
            replacement !== operationActivity &&
            isActivityAlive(replacement)
        ) {
            // The replacement Activity called requestConsentAtLaunch while the previous
            // operation was in flight. It is therefore an explicit launch request, not an
            // unsolicited ad/consent retry.
            requestConsentAtLaunch(replacement)
        }
    }

    private fun abandonStaleConsentOperationIfNeeded() {
        val operationActivity = consentOperationActivity?.get()
        if (
            (consentRequestInFlight || consentFormInFlight) &&
            !isActivityAlive(operationActivity)
        ) {
            consentRequestInFlight = false
            consentFormInFlight = false
            consentRequestGeneration += 1
        }
    }

    private fun startRewardedAdLoad() {
        if (
            !BuildConfig.ADS_ENABLED ||
            !currentEligibility() ||
            !_canRequestAds.value ||
            !userRequestedAd
        ) {
            _state.value = stateForCurrentEligibilityOrConsent()
            return
        }
        if (_state.value == RewardedAdState.LOADING ||
            _state.value == RewardedAdState.READY ||
            _state.value == RewardedAdState.SHOWING
        ) return

        val requestGeneration = ++adRequestGeneration
        _state.value = RewardedAdState.LOADING
        scope.launch {
            if (!initializeSdk()) {
                if (isCurrentLoadRequest(requestGeneration)) {
                    _state.value = RewardedAdState.UNAVAILABLE
                }
                return@launch
            }
            loadRewardedAd(requestGeneration)
        }
    }

    private suspend fun initializeSdk(): Boolean = initializationMutex.withLock {
        if (initialized) return@withLock true
        if (!BuildConfig.ADMOB_APP_ID.isNotBlank()) return@withLock false

        val success = runCatching {
            withTimeoutOrNull(SDK_INITIALIZATION_TIMEOUT_MS) {
                suspendCancellableCoroutine { continuation ->
                    MobileAds.initialize(
                        context,
                        InitializationConfig.Builder(BuildConfig.ADMOB_APP_ID).build(),
                    ) {
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                }
                true
            } ?: false
        }.onFailure { Log.w(TAG, "Mobile Ads initialization failed", it) }
            .getOrDefault(false)
        initialized = success
        success
    }

    private fun loadRewardedAd(requestGeneration: Long) {
        if (!isCurrentLoadRequest(requestGeneration) || !BuildConfig.REWARDED_AD_UNIT_ID.isNotBlank()) {
            if (isCurrentLoadRequest(requestGeneration)) {
                _state.value = RewardedAdState.UNAVAILABLE
            }
            return
        }

        runCatching {
            RewardedAd.load(
                AdRequest.Builder(BuildConfig.REWARDED_AD_UNIT_ID).build(),
                object : AdLoadCallback<RewardedAd> {
                    override fun onAdLoaded(ad: RewardedAd) {
                        if (isCurrentLoadRequest(requestGeneration)) {
                            loadedAd = ad
                            _state.value = RewardedAdState.READY
                        }
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        if (requestGeneration != adRequestGeneration) return
                        Log.i(TAG, "Rewarded ad unavailable: ${adError.message}")
                        loadedAd = null
                        _state.value = if (currentEligibility() && _canRequestAds.value) {
                            RewardedAdState.UNAVAILABLE
                        } else {
                            stateForCurrentEligibilityOrConsent()
                        }
                    }
                },
            )
        }.onFailure { error ->
            if (requestGeneration == adRequestGeneration) {
                loadedAd = null
                _state.value = RewardedAdState.UNAVAILABLE
                Log.w(TAG, "Rewarded ad could not be requested", error)
            }
        }
    }

    private fun updateConsentState() {
        val canRequestAds = consentInformation.canRequestAds()
        _canRequestAds.value = canRequestAds
        _privacyOptionsRequired.value = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

        val eligible = currentEligibility()
        applyEligibilitySnapshot(eligible)
        if (!canRequestAds) {
            clearLoadedAd()
            if (_state.value != RewardedAdState.SHOWING) {
                _state.value = if (eligible) {
                    RewardedAdState.CONSENT_REQUIRED
                } else {
                    stateForCurrentEligibility()
                }
            }
        } else if (
            eligible &&
            userRequestedAd &&
            _state.value == RewardedAdState.CONSENT_REQUIRED
        ) {
            // This path is reachable only after an earlier explicit prepare/retry action.
            startRewardedAdLoad()
        }
    }

    private fun onEligibilityChanged(eligible: Boolean) {
        applyEligibilitySnapshot(eligible)
    }

    private fun applyEligibilitySnapshot(eligible: Boolean) {
        if (eligible == observedEligible) return
        observedEligible = eligible
        clearLoadedAd()
        if (!eligible) userRequestedAd = false
        if (!BuildConfig.ADS_ENABLED) return
        _state.value = stateForCurrentEligibility()
    }

    private fun currentEligibility(): Boolean =
        billingRepository.entitlementState.value.canShowAds

    private fun stateForCurrentEligibilityOrConsent(): RewardedAdState = when {
        !currentEligibility() -> stateForCurrentEligibility()
        !_canRequestAds.value -> RewardedAdState.CONSENT_REQUIRED
        else -> RewardedAdState.UNAVAILABLE
    }

    private fun stateForCurrentEligibility(): RewardedAdState = when {
        !billingRepository.entitlementState.value.entitlementsChecked -> RewardedAdState.NOT_REQUESTED
        billingRepository.entitlementState.value.userTier != UserTier.FREE -> RewardedAdState.LEGACY_PURCHASE
        else -> RewardedAdState.NOT_REQUESTED
    }

    private fun clearLoadedAd() {
        loadedAd = null
        adRequestGeneration += 1
    }

    private fun isCurrentLoadRequest(requestGeneration: Long): Boolean =
        requestGeneration == adRequestGeneration &&
            _state.value == RewardedAdState.LOADING &&
            userRequestedAd &&
            currentEligibility() &&
            _canRequestAds.value

    private fun isCurrentConsentRequest(requestGeneration: Long): Boolean =
        requestGeneration == consentRequestGeneration && consentRequestInFlight

    private fun isCurrentConsentGeneration(requestGeneration: Long): Boolean =
        requestGeneration == consentRequestGeneration

    private fun isActivityAlive(activity: Activity?): Boolean {
        if (activity == null || activity.isFinishing || activity.isDestroyed) return false
        val lifecycle = (activity as? LifecycleOwner)?.lifecycle
        return lifecycle?.currentState != Lifecycle.State.DESTROYED
    }

    private fun isActivityUsable(activity: Activity?): Boolean {
        if (!isActivityAlive(activity)) return false
        val lifecycle = (activity as? LifecycleOwner)?.lifecycle
        return lifecycle == null || lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
    }

    private fun initialState(): RewardedAdState =
        if (BuildConfig.ADS_ENABLED) RewardedAdState.NOT_REQUESTED
        else RewardedAdState.CONFIGURATION_MISSING

    companion object {
        private const val TAG = "ExpiraAds"
        private const val REWARD_POINTS_PER_AD = 1
        private const val SDK_INITIALIZATION_TIMEOUT_MS = 15_000L
    }
}

enum class RewardedAdState {
    NOT_REQUESTED,
    CONFIGURATION_MISSING,
    CONSENT_REQUIRED,
    LEGACY_PURCHASE,
    LOADING,
    READY,
    SHOWING,
    UNAVAILABLE,
}
