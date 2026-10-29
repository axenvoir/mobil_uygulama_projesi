package com.axenvoir.expira.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.axenvoir.expira.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/** AdMob consent and the one explicitly requested rewarded-ad placement. */
@Singleton
class AdsManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val initializationMutex = Mutex()
    private var initialized = false
    private var loadedAd: RewardedAd? = null
    private var lastEligible = false
    private var userRequestedAd = false

    private val consentInformation = UserMessagingPlatform.getConsentInformation(context)
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val _canRequestAds = MutableStateFlow(false)
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    private val _privacyOptionsRequired = MutableStateFlow(false)
    val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired.asStateFlow()

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<RewardedAdState> = _state.asStateFlow()

    private val _supportPoints = MutableStateFlow(preferences.getInt(KEY_SUPPORT_POINTS, 0))
    val supportPoints: StateFlow<Int> = _supportPoints.asStateFlow()

    private val _rewardEvents = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val rewardEvents: SharedFlow<Int> = _rewardEvents.asSharedFlow()

    /** Refreshes UMP consent on each cold app launch, before any ad request is allowed. */
    fun requestConsentAtLaunch(activity: Activity) {
        if (!BuildConfig.ADS_ENABLED || activity.isFinishing || activity.isDestroyed) return

        val parameters = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            parameters,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.i(TAG, "Consent form not shown: ${formError.message}")
                    }
                    updateConsentState()
                }
            },
            { error ->
                Log.w(TAG, "Consent update failed: ${error.message}")
                // UMP may still permit ads based on a valid decision from the previous session.
                updateConsentState()
            },
        )
    }

    fun showPrivacyOptions(activity: Activity) {
        if (!BuildConfig.ADS_ENABLED || !privacyOptionsRequired.value) return
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.w(TAG, "Privacy options form failed: ${formError.message}")
            }
            updateConsentState()
        }
    }

    /** Loads an ad only for a verified free user, after UMP has allowed ad requests. */
    fun prepareRewardedAd(isEligible: Boolean) {
        lastEligible = isEligible
        userRequestedAd = true
        if (!BuildConfig.ADS_ENABLED) {
            loadedAd = null
            _state.value = RewardedAdState.CONFIGURATION_MISSING
            return
        }
        if (!isEligible) {
            loadedAd = null
            _state.value = RewardedAdState.LEGACY_PURCHASE
            return
        }
        if (!_canRequestAds.value) {
            loadedAd = null
            _state.value = RewardedAdState.CONSENT_REQUIRED
            return
        }
        if (_state.value == RewardedAdState.LOADING ||
            _state.value == RewardedAdState.READY ||
            _state.value == RewardedAdState.SHOWING
        ) return

        _state.value = RewardedAdState.LOADING
        scope.launch {
            if (!initializeSdk()) {
                _state.value = RewardedAdState.UNAVAILABLE
                return@launch
            }
            loadRewardedAd()
        }
    }

    fun retryRewardedAd(isEligible: Boolean) {
        loadedAd = null
        _state.value = RewardedAdState.NOT_REQUESTED
        prepareRewardedAd(isEligible)
    }

    fun showRewardedAd(activity: Activity) {
        val ad = loadedAd
        if (ad == null || !lastEligible || !_canRequestAds.value || activity.isFinishing || activity.isDestroyed) {
            _state.value = RewardedAdState.UNAVAILABLE
            return
        }

        loadedAd = null
        val rewardDelivered = AtomicBoolean(false)
        _state.value = RewardedAdState.SHOWING
        ad.adEventCallback = object : RewardedAdEventCallback {
            override fun onAdShowedFullScreenContent() = Unit

            override fun onAdDismissedFullScreenContent() {
                _state.value = RewardedAdState.UNAVAILABLE
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                Log.w(TAG, "Rewarded ad failed to show: ${fullScreenContentError.message}")
                _state.value = RewardedAdState.UNAVAILABLE
            }

            override fun onAdImpression() = Unit

            override fun onAdClicked() = Unit
        }
        runCatching {
            ad.show(activity) { rewardItem ->
                if (rewardDelivered.compareAndSet(false, true)) {
                    val amount = rewardItem.amount.coerceAtLeast(0)
                    if (amount > 0) {
                        val newTotal = (_supportPoints.value.toLong() + amount)
                            .coerceAtMost(Int.MAX_VALUE.toLong())
                            .toInt()
                        preferences.edit { putInt(KEY_SUPPORT_POINTS, newTotal) }
                        _supportPoints.value = newTotal
                        _rewardEvents.tryEmit(amount)
                    }
                }
            }
        }.onFailure { error ->
            Log.w(TAG, "Rewarded ad could not be opened", error)
            _state.value = RewardedAdState.UNAVAILABLE
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

    private fun loadRewardedAd() {
        if (!lastEligible || !_canRequestAds.value || !BuildConfig.REWARDED_AD_UNIT_ID.isNotBlank()) {
            _state.value = RewardedAdState.UNAVAILABLE
            return
        }

        RewardedAd.load(
            AdRequest.Builder(BuildConfig.REWARDED_AD_UNIT_ID).build(),
            object : AdLoadCallback<RewardedAd> {
                override fun onAdLoaded(ad: RewardedAd) {
                    if (lastEligible && _canRequestAds.value) {
                        loadedAd = ad
                        _state.value = RewardedAdState.READY
                    } else {
                        loadedAd = null
                        _state.value = RewardedAdState.CONSENT_REQUIRED
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.i(TAG, "Rewarded ad unavailable: ${adError.message}")
                    loadedAd = null
                    _state.value = RewardedAdState.UNAVAILABLE
                }
            },
        )
    }

    private fun updateConsentState() {
        _canRequestAds.value = consentInformation.canRequestAds()
        _privacyOptionsRequired.value = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        if (!_canRequestAds.value && _state.value != RewardedAdState.SHOWING) {
            loadedAd = null
            _state.value = RewardedAdState.CONSENT_REQUIRED
        } else if (_canRequestAds.value && userRequestedAd && lastEligible &&
            _state.value == RewardedAdState.CONSENT_REQUIRED
        ) {
            prepareRewardedAd(isEligible = true)
        }
    }

    private fun initialState(): RewardedAdState =
        if (BuildConfig.ADS_ENABLED) RewardedAdState.NOT_REQUESTED
        else RewardedAdState.CONFIGURATION_MISSING

    companion object {
        private const val TAG = "ExpiraAds"
        private const val PREFERENCES_NAME = "support_reward_preferences"
        private const val KEY_SUPPORT_POINTS = "support_points"
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
