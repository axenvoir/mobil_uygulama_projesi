package com.axenvoir.expira.ads

import android.app.Activity
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdSupportViewModel @Inject constructor(
    private val adsManager: AdsManager,
) : ViewModel() {
    val rewardedAdState = adsManager.state
    val privacyOptionsRequired = adsManager.privacyOptionsRequired
    val supportPoints = adsManager.supportPoints
    val rewardEvents = adsManager.rewardEvents

    fun prepareRewardedAd(isEligible: Boolean) {
        adsManager.prepareRewardedAd(isEligible)
    }

    fun retryRewardedAd(isEligible: Boolean) {
        adsManager.retryRewardedAd(isEligible)
    }

    fun showRewardedAd(activity: Activity) {
        adsManager.showRewardedAd(activity)
    }

    fun showPrivacyOptions(activity: Activity) {
        adsManager.showPrivacyOptions(activity)
    }

    fun retryConsentUpdate(activity: Activity) {
        adsManager.requestConsentAtLaunch(activity)
    }
}
