package com.axenvoir.expira.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axenvoir.expira.data.repository.CsvSnapshotLoader
import com.axenvoir.expira.domain.model.CosmeticPurchaseResult
import com.axenvoir.expira.domain.model.SupportCosmeticCatalog
import com.axenvoir.expira.domain.model.SupportWalletState
import com.axenvoir.expira.domain.repository.SupportRewardRepository
import com.axenvoir.expira.notification.ReminderHealthStore
import com.axenvoir.expira.notification.ReminderScheduleException
import com.axenvoir.expira.notification.ReminderScheduler
import com.axenvoir.expira.ui.theme.ExpiraPalettes
import com.axenvoir.expira.ui.theme.ThemeMode
import com.axenvoir.expira.ui.theme.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val supportRewardRepository: SupportRewardRepository,
    private val reminderScheduler: ReminderScheduler,
    private val reminderHealthStore: ReminderHealthStore,
) : ViewModel() {

    private val exportMutex = Mutex()
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val cosmeticPurchaseMutex = Mutex()
    private val _pendingCosmeticId = MutableStateFlow<String?>(null)
    val pendingCosmeticId: StateFlow<String?> = _pendingCosmeticId.asStateFlow()
    private val _cosmeticPurchaseEvents = MutableSharedFlow<CosmeticPurchaseResult>(extraBufferCapacity = 1)
    val cosmeticPurchaseEvents = _cosmeticPurchaseEvents.asSharedFlow()

    private val reminderRetryMutex = Mutex()
    private val _isRetryingReminder = MutableStateFlow(false)
    val isRetryingReminder: StateFlow<Boolean> = _isRetryingReminder.asStateFlow()
    val reminderHasTerminalFailure: StateFlow<Boolean> = reminderHealthStore.hasTerminalFailure

    val supportWallet: StateFlow<SupportWalletState> = supportRewardRepository.wallet.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SupportWalletState(),
    )

    val paletteId: StateFlow<String> = themePreferences.paletteId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpiraPalettes.GROVE,
    )

    init {
        viewModelScope.launch {
            try {
                supportRewardRepository.importLegacyBalanceIfNeeded()
            } catch (cancellation: kotlinx.coroutines.CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                // AdsManager and the next wallet action retry this idempotent migration.
            }
        }
    }

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

    fun retryReminderSchedule() {
        viewModelScope.launch {
            if (!reminderRetryMutex.tryLock()) return@launch
            _isRetryingReminder.value = true
            try {
                val generation = reminderScheduler.scheduleDailyReminder()
                reminderHealthStore.clearFailuresBeforeGeneration(generation)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: ReminderScheduleException) {
                reminderScheduler.withCurrentGeneration(failure.generation) {
                    reminderHealthStore.markTerminalFailure(failure.generation)
                }
            } finally {
                _isRetryingReminder.value = false
                reminderRetryMutex.unlock()
            }
        }
    }

    fun selectPalette(paletteId: String) {
        val normalizedId = ExpiraPalettes.normalize(paletteId)
        val ownedPaletteIds = supportWallet.value.ownedCosmeticIds
            .mapNotNull { SupportCosmeticCatalog.find(it) }
            .map { it.paletteId }
            .toSet()
        if (normalizedId != ExpiraPalettes.GROVE && normalizedId !in ownedPaletteIds) return

        viewModelScope.launch {
            themePreferences.selectPalette(normalizedId)
        }
    }

    fun purchaseCosmetic(cosmeticId: String) {
        viewModelScope.launch {
            if (!cosmeticPurchaseMutex.tryLock()) return@launch
            _pendingCosmeticId.value = cosmeticId
            try {
                val result = supportRewardRepository.redeemCosmetic(cosmeticId)
                if (result == CosmeticPurchaseResult.PURCHASED) {
                    SupportCosmeticCatalog.find(cosmeticId)?.let { cosmetic ->
                        try {
                            themePreferences.selectPalette(cosmetic.paletteId)
                        } catch (cancellation: CancellationException) {
                            throw cancellation
                        } catch (_: Exception) {
                            // The purchase remains valid; the owned palette can be applied later.
                        }
                    }
                }
                _cosmeticPurchaseEvents.emit(result)
            } catch (cancellation: kotlinx.coroutines.CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _cosmeticPurchaseEvents.emit(CosmeticPurchaseResult.UNKNOWN_ITEM)
            } finally {
                _pendingCosmeticId.value = null
                cosmeticPurchaseMutex.unlock()
            }
        }
    }
}
