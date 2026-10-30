package com.axenvoir.expira.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axenvoir.expira.BuildConfig
import com.axenvoir.expira.R
import com.axenvoir.expira.ads.AdSupportViewModel
import com.axenvoir.expira.ads.RewardedAdState
import com.axenvoir.expira.billing.BillingViewModel
import com.axenvoir.expira.billing.UserTier
import com.axenvoir.expira.domain.model.CosmeticPurchaseResult
import com.axenvoir.expira.domain.model.SupportCosmeticCatalog
import com.axenvoir.expira.domain.model.SupportWalletState
import com.axenvoir.expira.ui.components.expiraTopAppBarColors
import com.axenvoir.expira.ui.components.ExpiraSectionCard
import com.axenvoir.expira.ui.theme.ExpiraPalettes
import com.axenvoir.expira.ui.theme.ThemeMode
import com.axenvoir.expira.ui.theme.expiraPalettePreviewColors
import com.axenvoir.expira.ui.util.findActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    billingViewModel: BillingViewModel = hiltViewModel(),
    adSupportViewModel: AdSupportViewModel = hiltViewModel(),
) {
    val selected by viewModel.current.collectAsStateWithLifecycle()
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    val reminderHasTerminalFailure by viewModel.reminderHasTerminalFailure.collectAsStateWithLifecycle()
    val isRetryingReminder by viewModel.isRetryingReminder.collectAsStateWithLifecycle()
    val dynamicColor by viewModel.dynamicColor.collectAsStateWithLifecycle()
    val restoreResult by billingViewModel.restoreResult.collectAsStateWithLifecycle()
    val isCheckingPurchases by billingViewModel.isCheckingPurchases.collectAsStateWithLifecycle()
    val userTier by billingViewModel.userTier.collectAsStateWithLifecycle()
    val entitlementsChecked by billingViewModel.entitlementsChecked.collectAsStateWithLifecycle()
    val rewardedAdState by adSupportViewModel.rewardedAdState.collectAsStateWithLifecycle()
    val rewardSavePending by adSupportViewModel.rewardSavePending.collectAsStateWithLifecycle()
    val supportWallet by viewModel.supportWallet.collectAsStateWithLifecycle()
    val selectedPalette by viewModel.paletteId.collectAsStateWithLifecycle()
    val pendingCosmeticId by viewModel.pendingCosmeticId.collectAsStateWithLifecycle()
    val privacyOptionsRequired by adSupportViewModel.privacyOptionsRequired.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val resources = androidx.compose.ui.platform.LocalResources.current
    val adActivityUnavailableMessage = stringResource(R.string.support_ad_activity_unavailable)
    val exportDefaultFilename = stringResource(R.string.export_default_filename)
    val exportDoneMessage = stringResource(R.string.export_done)
    val exportFailedMessage = stringResource(R.string.export_failed)
    val testNotificationSentMessage = stringResource(R.string.test_notification_sent)
    val notificationsBlockedMessage = stringResource(R.string.notifications_blocked)
    val remindersPermissionEnabledMessage = stringResource(R.string.reminders_permission_enabled)
    val openSettingsLabel = stringResource(R.string.open_settings)
    val restoreSuccessMessage = stringResource(R.string.restore_success)
    val restoreUnavailableMessage = stringResource(R.string.restore_unavailable)
    val manageSubscriptionUnavailableMessage = stringResource(R.string.play_store_unavailable)
    val externalLinkUnavailableMessage = stringResource(R.string.external_link_unavailable)
    val rewardSaveFailedMessage = stringResource(R.string.support_reward_save_failed)
    val rewardRetryLabel = stringResource(R.string.support_reward_retry)
    val cosmeticPurchasedMessage = stringResource(R.string.palette_unlocked)
    val cosmeticInsufficientMessage = stringResource(R.string.palette_not_enough_points)
    val cosmeticOwnedMessage = stringResource(R.string.palette_already_owned)
    val cosmeticErrorMessage = stringResource(R.string.palette_unlock_failed)
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    fun withForegroundActivity(action: (android.app.Activity) -> Unit) {
        val activity = context.findActivity()
        if (activity == null) {
            scope.launch { snackbarHostState.showSnackbar(adActivityUnavailableMessage) }
        } else {
            action(activity)
        }
    }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        billingViewModel.refreshEntitlements()
    }
    androidx.compose.runtime.LaunchedEffect(resources) {
        adSupportViewModel.rewardEvents.collect { amount ->
            val message = resources.getQuantityString(
                R.plurals.support_reward_earned,
                amount,
                amount,
            )
            snackbarHostState.showSnackbar(message)
        }
    }
    androidx.compose.runtime.LaunchedEffect(resources, adSupportViewModel) {
        adSupportViewModel.rewardSaveErrors.collect {
            val result = snackbarHostState.showSnackbar(
                message = rewardSaveFailedMessage,
                actionLabel = rewardRetryLabel,
                withDismissAction = true,
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                adSupportViewModel.retryRewardSave()
            }
        }
    }
    androidx.compose.runtime.LaunchedEffect(viewModel, resources) {
        viewModel.cosmeticPurchaseEvents.collect { result ->
            val message = when (result) {
                CosmeticPurchaseResult.PURCHASED -> cosmeticPurchasedMessage
                CosmeticPurchaseResult.INSUFFICIENT_POINTS -> cosmeticInsufficientMessage
                CosmeticPurchaseResult.ALREADY_OWNED -> cosmeticOwnedMessage
                CosmeticPurchaseResult.UNKNOWN_ITEM -> cosmeticErrorMessage
            }
            snackbarHostState.showSnackbar(message)
        }
    }
    androidx.compose.runtime.LaunchedEffect(restoreResult) {
        restoreResult?.let { success ->
            snackbarHostState.showSnackbar(if (success) restoreSuccessMessage else restoreUnavailableMessage)
            billingViewModel.consumeRestoreResult()
        }
    }
    val showNotificationResult: (Boolean) -> Unit = { sent ->
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = if (sent) testNotificationSentMessage else notificationsBlockedMessage,
                actionLabel = if (sent) null else openSettingsLabel,
            )
            if (!sent && result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                val intent = android.content.Intent(
                    android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS,
                ).apply {
                    putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                runCatching { context.startActivity(intent) }
            }
        }
    }
    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted && viewModel.canPostNotifications()) {
            viewModel.retryReminderSchedule()
            scope.launch { snackbarHostState.showSnackbar(remindersPermissionEnabledMessage) }
        } else {
            showNotificationResult(false)
        }
    }
    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val result = viewModel.exportCsv(context, uri)
                snackbarHostState.showSnackbar(
                    if (result.isSuccess) exportDoneMessage else exportFailedMessage
                )
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = expiraTopAppBarColors(),
                title = { Text(stringResource(R.string.settings_title), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                ExpiraSectionCard(title = stringResource(R.string.language_section)) {
                    LanguageOption(
                        label = stringResource(R.string.language_system),
                        isSelected = selected == AppLanguage.SYSTEM,
                        onClick = { viewModel.setLanguage(AppLanguage.SYSTEM) },
                    )
                    LanguageOption(
                        label = stringResource(R.string.language_turkish),
                        isSelected = selected == AppLanguage.TURKISH,
                        onClick = { viewModel.setLanguage(AppLanguage.TURKISH) },
                    )
                    LanguageOption(
                        label = stringResource(R.string.language_english),
                        isSelected = selected == AppLanguage.ENGLISH,
                        onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) },
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                ExpiraSectionCard(title = stringResource(R.string.notifications_section)) {
                    Text(
                        text = stringResource(R.string.notifications_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FilledTonalButton(
                        onClick = {
                            if (
                                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                                androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.POST_NOTIFICATIONS,
                                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationPermissionLauncher.launch(
                                    android.Manifest.permission.POST_NOTIFICATIONS,
                                )
                            } else if (!viewModel.canPostNotifications()) {
                                showNotificationResult(false)
                            } else {
                                viewModel.retryReminderSchedule()
                                scope.launch { snackbarHostState.showSnackbar(remindersPermissionEnabledMessage) }
                            }
                        },
                        enabled = !isRetryingReminder,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        if (isRetryingReminder) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.reminder_rescheduling))
                        } else {
                            Text(stringResource(R.string.enable_reminders))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(
                        onClick = { showNotificationResult(viewModel.sendTestNotification()) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.test_notification)) }
                    if (reminderHasTerminalFailure) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.reminder_worker_failed),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FilledTonalButton(
                            onClick = viewModel::retryReminderSchedule,
                            enabled = !isRetryingReminder,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        ) {
                            if (isRetryingReminder) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                stringResource(
                                    if (isRetryingReminder) R.string.reminder_rescheduling
                                    else R.string.reminder_retry_schedule,
                                ),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                ExpiraSectionCard(title = stringResource(R.string.data_section)) {
                    Text(
                        text = stringResource(R.string.data_export_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FilledTonalButton(
                        onClick = { exportLauncher.launch(exportDefaultFilename) },
                        enabled = !isExporting,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.exporting_csv))
                        } else {
                            Text(stringResource(R.string.export_csv))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                ExpiraSectionCard(title = stringResource(R.string.support_section)) {
                    RewardedSupportSection(
                        userTier = userTier,
                        entitlementsChecked = entitlementsChecked,
                        isCheckingPurchases = isCheckingPurchases,
                        rewardedAdState = rewardedAdState,
                        rewardSavePending = rewardSavePending,
                        wallet = supportWallet,
                        privacyOptionsRequired = privacyOptionsRequired,
                        onPrepareAd = { adSupportViewModel.prepareRewardedAd() },
                        onRetryRewardSave = { adSupportViewModel.retryRewardSave() },
                        onRetryAd = { adSupportViewModel.retryRewardedAd() },
                        onShowAd = { withForegroundActivity(adSupportViewModel::showRewardedAd) },
                        onShowPrivacyOptions = {
                            withForegroundActivity(adSupportViewModel::showPrivacyOptions)
                        },
                        onRetryConsent = {
                            withForegroundActivity(adSupportViewModel::retryConsentUpdate)
                        },
                        onRestorePurchases = billingViewModel::restorePurchases,
                        onRefreshPurchases = billingViewModel::refreshEntitlements,
                        onManageSubscription = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                "https://play.google.com/store/account/subscriptions?package=${context.packageName}".toUri(),
                            )
                            runCatching { context.startActivity(intent) }.onFailure {
                                scope.launch { snackbarHostState.showSnackbar(manageSubscriptionUnavailableMessage) }
                            }
                        },
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                ExpiraSectionCard(title = stringResource(R.string.theme_section)) {
                    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
                    LanguageOption(
                        label = stringResource(R.string.theme_system),
                        isSelected = themeMode == ThemeMode.SYSTEM,
                        onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                    )
                    LanguageOption(
                        label = stringResource(R.string.theme_light),
                        isSelected = themeMode == ThemeMode.LIGHT,
                        onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                    )
                    LanguageOption(
                        label = stringResource(R.string.theme_dark),
                        isSelected = themeMode == ThemeMode.DARK,
                        onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                ExpiraSectionCard(title = stringResource(R.string.appearance_section)) {
                    val dynamicColorAvailable = android.os.Build.VERSION.SDK_INT >=
                        android.os.Build.VERSION_CODES.S
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = dynamicColor,
                                onValueChange = viewModel::setDynamicColor,
                                enabled = dynamicColorAvailable,
                                role = Role.Switch,
                            )
                            .semantics(mergeDescendants = true) {},
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.dynamic_color),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = stringResource(R.string.dynamic_color_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = dynamicColor,
                            onCheckedChange = null,
                            enabled = dynamicColorAvailable,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                ExpiraSectionCard(title = stringResource(R.string.palette_shop_section)) {
                    Text(
                        text = stringResource(R.string.palette_shop_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PaletteShopItem(
                        title = stringResource(R.string.palette_grove_name),
                        description = stringResource(R.string.palette_grove_description),
                        paletteId = ExpiraPalettes.GROVE,
                        pointCost = null,
                        owned = true,
                        walletReady = supportWallet.isReady,
                        spendablePoints = supportWallet.spendablePoints,
                        selected = selectedPalette == ExpiraPalettes.GROVE && !dynamicColor,
                        isPending = false,
                        onClick = { viewModel.selectPalette(ExpiraPalettes.GROVE) },
                    )
                    SupportCosmeticCatalog.items.forEach { cosmetic ->
                        val (titleRes, descriptionRes) = when (cosmetic.id) {
                            SupportCosmeticCatalog.QUIET_DUSK_ID ->
                                R.string.palette_quiet_dusk_name to R.string.palette_quiet_dusk_description
                            SupportCosmeticCatalog.CITRUS_LEDGER_ID ->
                                R.string.palette_citrus_ledger_name to R.string.palette_citrus_ledger_description
                            else ->
                                R.string.palette_berry_archive_name to R.string.palette_berry_archive_description
                        }
                        val owned = cosmetic.id in supportWallet.ownedCosmeticIds
                        PaletteShopItem(
                            title = stringResource(titleRes),
                            description = stringResource(descriptionRes),
                            paletteId = cosmetic.paletteId,
                            pointCost = cosmetic.pointCost,
                            owned = owned,
                            walletReady = supportWallet.isReady,
                            spendablePoints = supportWallet.spendablePoints,
                            selected = selectedPalette == cosmetic.paletteId && !dynamicColor,
                            isPending = pendingCosmeticId != null,
                            onClick = {
                                if (owned) viewModel.selectPalette(cosmetic.paletteId)
                                else viewModel.purchaseCosmetic(cosmetic.id)
                            },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                ExpiraSectionCard(title = stringResource(R.string.about_section)) {
                    Text(
                        text = "${stringResource(R.string.version_label)}: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    TextButton(
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        PRIVACY_POLICY_URL.toUri(),
                                    ),
                                )
                            }.onFailure {
                                scope.launch {
                                    snackbarHostState.showSnackbar(externalLinkUnavailableMessage)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.privacy_policy))
                    }
                    TextButton(
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    android.content.Intent(
                                        android.content.Intent.ACTION_SENDTO,
                                        "mailto:$SUPPORT_EMAIL".toUri(),
                                    ),
                                )
                            }.onFailure {
                                scope.launch {
                                    snackbarHostState.showSnackbar(externalLinkUnavailableMessage)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.contact_support))
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardedSupportSection(
    userTier: UserTier,
    entitlementsChecked: Boolean,
    isCheckingPurchases: Boolean,
    rewardedAdState: RewardedAdState,
    rewardSavePending: Boolean,
    wallet: SupportWalletState,
    privacyOptionsRequired: Boolean,
    onPrepareAd: () -> Unit,
    onRetryRewardSave: () -> Unit,
    onRetryAd: () -> Unit,
    onShowAd: () -> Unit,
    onShowPrivacyOptions: () -> Unit,
    onRetryConsent: () -> Unit,
    onRestorePurchases: () -> Unit,
    onRefreshPurchases: () -> Unit,
    onManageSubscription: () -> Unit,
) {
    if (wallet.isReady) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = MaterialTheme.shapes.large,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Star, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = androidx.compose.ui.res.pluralStringResource(
                        R.plurals.support_points_total,
                        wallet.spendablePoints,
                        wallet.spendablePoints,
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.palette_wallet_loading),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    if (wallet.supporterBadgeUnlocked) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            shape = MaterialTheme.shapes.large,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Star, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.support_badge_unlocked),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    } else {
        Spacer(modifier = Modifier.height(8.dp))
        Column {
            Text(
                text = stringResource(
                    R.string.support_badge_progress,
                    wallet.lifetimeEarnedPoints.coerceAtLeast(0),
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = {
                    wallet.lifetimeEarnedPoints.coerceIn(0, SUPPORTER_BADGE_POINTS).toFloat() /
                        SUPPORTER_BADGE_POINTS
                },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
            )
        }
    }
    if (rewardSavePending) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.support_reward_save_failed),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
        FilledTonalButton(
            onClick = onRetryRewardSave,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text(stringResource(R.string.support_reward_retry))
        }
    }
    Spacer(modifier = Modifier.height(12.dp))

    when {
        rewardSavePending -> Unit
        userTier != UserTier.FREE -> {
            Text(
                text = stringResource(R.string.support_thanks),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.support_legacy_ad_free),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (userTier == UserTier.SUPPORTER || userTier == UserTier.PREMIUM) {
                TextButton(onClick = onManageSubscription) {
                    Text(stringResource(R.string.support_manage_subscription))
                }
            }
        }
        !entitlementsChecked -> {
            Text(
                text = stringResource(R.string.support_entitlement_checking),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilledTonalButton(
                onClick = onRefreshPurchases,
                enabled = !isCheckingPurchases,
            ) {
                Text(
                    stringResource(
                        if (isCheckingPurchases) R.string.support_purchases_checking
                        else R.string.support_check_purchases,
                    ),
                )
            }
        }
        else -> {
            Text(
                text = stringResource(R.string.support_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.support_ad_disclosure),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            when (rewardedAdState) {
                RewardedAdState.NOT_REQUESTED -> FilledTonalButton(
                    onClick = onPrepareAd,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.support_ad_prepare)) }
                RewardedAdState.LOADING -> AdProgressRow(R.string.support_ad_loading)
                RewardedAdState.READY -> Button(
                    onClick = onShowAd,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.support_ad_watch)) }
                RewardedAdState.SHOWING -> AdProgressRow(R.string.support_ad_showing)
                RewardedAdState.CONSENT_REQUIRED -> {
                    Text(
                        text = stringResource(R.string.support_ad_consent_required),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(
                        onClick = onRetryConsent,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.support_ad_retry_consent)) }
                }
                RewardedAdState.CONFIGURATION_MISSING -> Text(
                    text = stringResource(R.string.support_ad_configuration_missing),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                RewardedAdState.UNAVAILABLE -> {
                    Text(
                        text = stringResource(R.string.support_ad_unavailable),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(
                        onClick = onRetryAd,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.support_ad_retry)) }
                }
                RewardedAdState.LEGACY_PURCHASE -> Text(
                    text = stringResource(R.string.support_legacy_ad_free),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }

    if (privacyOptionsRequired && userTier == UserTier.FREE) {
        TextButton(onClick = onShowPrivacyOptions) {
            Text(stringResource(R.string.support_privacy_options))
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    TextButton(
        onClick = onRestorePurchases,
        enabled = !isCheckingPurchases,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            stringResource(
                if (isCheckingPurchases) R.string.support_purchases_checking
                else R.string.support_restore,
            ),
        )
    }
}

@Composable
private fun PaletteShopItem(
    title: String,
    description: String,
    paletteId: String,
    pointCost: Int?,
    owned: Boolean,
    walletReady: Boolean,
    spendablePoints: Int,
    selected: Boolean,
    isPending: Boolean,
    onClick: () -> Unit,
) {
    val affordable = pointCost == null || spendablePoints >= pointCost
    val actionLabel = when {
        selected -> stringResource(R.string.palette_current)
        isPending -> stringResource(R.string.palette_processing)
        !owned && !walletReady -> stringResource(R.string.palette_wallet_loading)
        owned -> stringResource(R.string.palette_apply)
        else -> stringResource(R.string.palette_unlock)
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier.size(width = 56.dp, height = 40.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        expiraPalettePreviewColors(paletteId).forEach { swatch ->
                            Surface(
                                modifier = Modifier.size(14.dp),
                                shape = CircleShape,
                                color = swatch,
                            ) { }
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val compactCard = maxWidth < 420.dp
                if (compactCard) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (pointCost == null) stringResource(R.string.palette_free)
                            else stringResource(R.string.palette_cost, pointCost),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        FilledTonalButton(
                            onClick = onClick,
                            enabled = !selected && !isPending && (owned || (walletReady && affordable)),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        ) {
                            Text(actionLabel)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = if (pointCost == null) stringResource(R.string.palette_free)
                            else stringResource(R.string.palette_cost, pointCost),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        FilledTonalButton(
                            onClick = onClick,
                            enabled = !selected && !isPending && (owned || (walletReady && affordable)),
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Text(actionLabel)
                        }
                    }
                }
            }
            if (!owned && walletReady && pointCost != null && !affordable) {
                Text(
                    text = stringResource(R.string.palette_need_more_points),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private const val SUPPORTER_BADGE_POINTS = 5
private const val PRIVACY_POLICY_URL = "https://axenvoir.github.io/"
private const val SUPPORT_EMAIL = "axenvoir@yahoo.com"

@Composable
private fun AdProgressRow(messageRes: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.padding(end = 12.dp).size(20.dp),
            strokeWidth = 2.dp,
        )
        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun LanguageOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface,
            )
        },
        trailingContent = {
            RadioButton(selected = isSelected, onClick = null)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton,
            ),
        colors = ListItemDefaults.colors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
            } else {
                Color.Transparent
            },
        ),
    )
}
