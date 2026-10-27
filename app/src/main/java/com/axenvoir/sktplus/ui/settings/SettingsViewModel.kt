package com.axenvoir.sktplus.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val languageManager: LanguageManager,
    private val notificationHelper: com.axenvoir.sktplus.notification.NotificationHelper,
) : ViewModel() {

    fun sendTestNotification() {
        notificationHelper.createChannel()
        notificationHelper.showExpiryReminder(count = 1, topNames = emptyList())
    }

    val current: StateFlow<AppLanguage> = languageManager.current.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppLanguage.SYSTEM,
    )

    fun setLanguage(language: AppLanguage) {
        languageManager.setLanguage(language)
    }
}
