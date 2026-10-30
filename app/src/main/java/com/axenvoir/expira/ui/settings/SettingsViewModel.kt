package com.axenvoir.expira.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axenvoir.expira.data.repository.CsvSnapshotLoader
import com.axenvoir.expira.ui.theme.ThemeMode
import com.axenvoir.expira.ui.theme.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val languageManager: LanguageManager,
    private val notificationHelper: com.axenvoir.expira.notification.NotificationHelper,
    private val csvSnapshotLoader: CsvSnapshotLoader,
    private val csvExporter: com.axenvoir.expira.data.repository.CsvExporter,
    private val themePreferences: ThemePreferences,
) : ViewModel() {

    private val exportMutex = Mutex()
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    fun sendTestNotification(): Boolean = notificationHelper.showTestNotification()

    suspend fun exportCsv(context: android.content.Context, uri: android.net.Uri): Result<Unit> {
        if (!exportMutex.tryLock()) {
            return Result.failure(IllegalStateException("A CSV export is already in progress"))
        }
        _isExporting.value = true
        return try {
            val snapshotResult = csvSnapshotLoader.loadSnapshot()
            val snapshot = when {
                snapshotResult.isSuccess -> snapshotResult.getOrThrow()
                else -> return Result.failure(
                    snapshotResult.exceptionOrNull()
                        ?: IllegalStateException("CSV data could not be loaded"),
                )
            }
            csvExporter.export(context, uri, snapshot.products, snapshot.notes)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Result.failure(error)
        } finally {
            _isExporting.value = false
            exportMutex.unlock()
        }
    }

    val current: StateFlow<AppLanguage> = languageManager.current.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppLanguage.SYSTEM,
    )

    val dynamicColor: StateFlow<Boolean> = themePreferences.dynamicColor.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    val themeMode: StateFlow<ThemeMode> = themePreferences.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThemeMode.SYSTEM,
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { themePreferences.setThemeMode(mode) }
    }

    fun setLanguage(language: AppLanguage) {
        languageManager.setLanguage(language)
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { themePreferences.setDynamicColor(enabled) }
    }
}
