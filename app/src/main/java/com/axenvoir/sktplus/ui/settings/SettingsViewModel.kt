package com.axenvoir.sktplus.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val languageManager: LanguageManager,
    private val notificationHelper: com.axenvoir.sktplus.notification.NotificationHelper,
    private val productRepository: com.axenvoir.sktplus.domain.repository.ProductRepository,
    private val noteRepository: com.axenvoir.sktplus.domain.repository.NoteRepository,
    private val csvExporter: com.axenvoir.sktplus.data.repository.CsvExporter,
) : ViewModel() {

    fun sendTestNotification(): Boolean {
        notificationHelper.createChannel()
        if (!notificationHelper.canPostNotifications()) return false
        notificationHelper.showExpiryReminder(count = 1, topNames = emptyList())
        return true
    }

    suspend fun exportCsv(context: android.content.Context, uri: android.net.Uri): Result<Unit> {
        val products = productRepository.observeAll().first()
        val notes = noteRepository.observeAll().first()
        return csvExporter.export(context, uri, products, notes)
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
