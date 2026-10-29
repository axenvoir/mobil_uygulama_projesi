package com.axenvoir.expira.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axenvoir.expira.BuildConfig
import com.axenvoir.expira.R
import com.axenvoir.expira.ads.AdSupportViewModel
import com.axenvoir.expira.ads.RewardedAdState
import com.axenvoir.expira.billing.BillingViewModel
import com.axenvoir.expira.billing.UserTier
import com.axenvoir.expira.ui.components.expiraTopAppBarColors
import com.axenvoir.expira.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    billingViewModel: BillingViewModel = hiltViewModel(),
    adSupportViewModel: AdSupportViewModel = hiltViewModel(),
) {
    val selected by viewModel.current.collectAsStateWithLifecycle()
    val restoreResult by billingViewModel.restoreResult.collectAsStateWithLifecycle()
    val userTier by billingViewModel.userTier.collectAsStateWithLifecycle()
    val entitlementsChecked by billingViewModel.entitlementsChecked.collectAsStateWithLifecycle()
    val rewardedAdState by adSupportViewModel.rewardedAdState.collectAsStateWithLifecycle()
    val supportPoints by adSupportViewModel.supportPoints.collectAsStateWithLifecycle()
    val privacyOptionsRequired by adSupportViewModel.privacyOptionsRequired.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val resources = androidx.compose.ui.platform.LocalResources.current
    val exportDoneMessage = stringResource(R.string.export_done)
    val exportFailedMessage = stringResource(R.string.export_failed)
    val testNotificationSentMessage = stringResource(R.string.test_notification_sent)
    val notificationsBlockedMessage = stringResource(R.string.notifications_blocked)
    val openSettingsLabel = stringResource(R.string.open_settings)
    val restoreSuccessMessage = stringResource(R.string.restore_success)
    val restoreUnavailableMessage = stringResource(R.string.restore_unavailable)
    val manageSubscriptionUnavailableMessage = stringResource(R.string.play_store_unavailable)
    val scope = androidx.compose.runtime.rememberCoroutineScope()
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
        showNotificationResult(granted && viewModel.sendTestNotification())
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
        topBar = {
            TopAppBar(
                colors = expiraTopAppBarColors(),
                title = { Text(stringResource(R.string.settings_title), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cancel),
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
                    .fillMaxWidth()
                    .widthIn(max = 800.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                SettingsCard(title = stringResource(R.string.language_section)) {
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
                SettingsCard(title = stringResource(R.string.notifications_section)) {
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
                            } else {
                                showNotificationResult(viewModel.sendTestNotification())
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.test_notification)) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                SettingsCard(title = stringResource(R.string.data_section)) {
                    Text(
                        text = stringResource(R.string.data_export_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FilledTonalButton(
                        onClick = { exportLauncher.launch("expira-verileri.csv") },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.export_csv)) }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                SettingsCard(title = stringResource(R.string.support_section)) {
                    RewardedSupportSection(
                        userTier = userTier,
                        entitlementsChecked = entitlementsChecked,
                        rewardedAdState = rewardedAdState,
                        supportPoints = supportPoints,
                        privacyOptionsRequired = privacyOptionsRequired,
                        onPrepareAd = { adSupportViewModel.prepareRewardedAd(isEligible = true) },
                        onRetryAd = { adSupportViewModel.retryRewardedAd(isEligible = true) },
                        onShowAd = {
                            (context as? android.app.Activity)?.let(adSupportViewModel::showRewardedAd)
                        },
                        onShowPrivacyOptions = {
                            (context as? android.app.Activity)?.let(adSupportViewModel::showPrivacyOptions)
                        },
                        onRetryConsent = {
                            (context as? android.app.Activity)?.let(adSupportViewModel::retryConsentUpdate)
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
                SettingsCard(title = stringResource(R.string.theme_section)) {
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
                SettingsCard(title = stringResource(R.string.appearance_section)) {
                    val dynamicColor by viewModel.dynamicColor.collectAsStateWithLifecycle()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                            onCheckedChange = viewModel::setDynamicColor,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                SettingsCard(title = stringResource(R.string.about_section)) {
                    Text(
                        text = "${stringResource(R.string.version_label)}: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardedSupportSection(
    userTier: UserTier,
    entitlementsChecked: Boolean,
    rewardedAdState: RewardedAdState,
    supportPoints: Int,
    privacyOptionsRequired: Boolean,
    onPrepareAd: () -> Unit,
    onRetryAd: () -> Unit,
    onShowAd: () -> Unit,
    onShowPrivacyOptions: () -> Unit,
    onRetryConsent: () -> Unit,
    onRestorePurchases: () -> Unit,
    onRefreshPurchases: () -> Unit,
    onManageSubscription: () -> Unit,
) {
    Text(
        text = stringResource(R.string.support_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (supportPoints > 0) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = androidx.compose.ui.res.pluralStringResource(
                R.plurals.support_points_total,
                supportPoints,
                supportPoints,
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    Spacer(modifier = Modifier.height(12.dp))

    when {
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
            FilledTonalButton(onClick = onRefreshPurchases) {
                Text(stringResource(R.string.support_check_purchases))
            }
        }
        else -> {
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
                RewardedAdState.LOADING -> Text(
                    text = stringResource(R.string.support_ad_loading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                RewardedAdState.READY -> FilledTonalButton(
                    onClick = onShowAd,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.support_ad_watch)) }
                RewardedAdState.SHOWING -> Text(
                    text = stringResource(R.string.support_ad_showing),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
        modifier = Modifier.fillMaxWidth(),
    ) { Text(stringResource(R.string.support_restore)) }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun LanguageOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                else MaterialTheme.colorScheme.surface,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = isSelected, onClick = null)
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 4.dp))
    }
}
