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
    val rewardEvents = adsManager.rewardEvents
    val rewardSaveErrors = adsManager.rewardSaveErrors
    val rewardSavePending = adsManager.rewardSavePending

    fun prepareRewardedAd() {
        adsManager.prepareRewardedAd()
    }

    fun retryRewardedAd() {
        adsManager.retryRewardedAd()
    }

    fun retryRewardSave() {
        adsManager.retryRewardSave()
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
