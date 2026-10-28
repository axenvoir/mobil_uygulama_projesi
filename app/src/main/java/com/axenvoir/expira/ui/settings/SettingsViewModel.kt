package com.axenvoir.expira.ui.settings

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
    private val notificationHelper: com.axenvoir.expira.notification.NotificationHelper,
    private val productRepository: com.axenvoir.expira.domain.repository.ProductRepository,
    private val noteRepository: com.axenvoir.expira.domain.repository.NoteRepository,
    private val csvExporter: com.axenvoir.expira.data.repository.CsvExporter,
) : ViewModel() {

    fun sendTestNotification(): Boolean {
        notificationHelper.createChannel()
        if (!notificationHelper.canPostNotifications()) return false
        notificationHelper.showTestNotification()
        return true
    }

    suspend fun exportCsv(context: android.content.Context, uri: android.net.Uri): Result<Unit> {
        val products = productRepository.observeAll().first()
        val notes = noteRepository.observeAll().first()
        return csvExporter.export(context, uri, products, notes)
    }

    val current: StateFlow<AppLanguage> = languageManager.current.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppLanguage.SYSTEM,
    )

    fun setLanguage(language: AppLanguage) {
        languageManager.setLanguage(language)
    }
}
