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
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axenvoir.expira.BuildConfig
import com.axenvoir.expira.R
import com.axenvoir.expira.billing.BillingOperation
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
) {
    val selected by viewModel.current.collectAsStateWithLifecycle()
    val restoreResult by billingViewModel.restoreResult.collectAsStateWithLifecycle()
    val billingState by billingViewModel.billingState.collectAsStateWithLifecycle()
    val offers by billingViewModel.offers.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val exportDoneMessage = stringResource(R.string.export_done)
    val exportFailedMessage = stringResource(R.string.export_failed)
    val testNotificationSentMessage = stringResource(R.string.test_notification_sent)
    val notificationsBlockedMessage = stringResource(R.string.notifications_blocked)
    val openSettingsLabel = stringResource(R.string.open_settings)
    val restoreSuccessMessage = stringResource(R.string.restore_success)
    val restoreUnavailableMessage = stringResource(R.string.restore_unavailable)
    val billingMessage = when (billingState.operation) {
        BillingOperation.LOADING -> stringResource(R.string.billing_loading)
        BillingOperation.SUCCESS -> stringResource(R.string.billing_success)
        BillingOperation.CANCELLED -> stringResource(R.string.billing_cancelled)
        BillingOperation.PENDING -> stringResource(R.string.billing_pending)
        BillingOperation.UNAVAILABLE -> stringResource(R.string.billing_unavailable)
        BillingOperation.ERROR -> stringResource(R.string.billing_error)
        BillingOperation.IDLE -> null
    }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    androidx.compose.runtime.LaunchedEffect(restoreResult) {
        restoreResult?.let { success ->
            snackbarHostState.showSnackbar(if (success) restoreSuccessMessage else restoreUnavailableMessage)
            billingViewModel.consumeRestoreResult()
        }
    }
    androidx.compose.runtime.LaunchedEffect(billingState.operation, billingState.productId) {
        if (billingMessage != null) snackbarHostState.showSnackbar(billingMessage)
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
                    FilledTonalButton(
                        onClick = { exportLauncher.launch("expira-verileri.csv") },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.export_csv)) }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.export_csv_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                SettingsCard(title = stringResource(R.string.support_section)) {
                    val tier by billingViewModel.userTier.collectAsStateWithLifecycle()
                    val purchaseInProgress = billingState.operation == BillingOperation.LOADING
                    val supporterPrice = offers.firstOrNull { it.productId == "supporter_monthly" }?.formattedPrice
                    val premiumPrice = offers.firstOrNull { it.productId == "premium_monthly" }?.formattedPrice
                    val lifetimePrice = offers.firstOrNull { it.productId == "premium_lifetime" }?.formattedPrice
                    Text(
                        text = stringResource(R.string.support_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (tier == UserTier.FREE) {
                        FilledTonalButton(
                            enabled = !purchaseInProgress,
                            onClick = {
                                val activity = context as? android.app.Activity
                                if (activity != null) billingViewModel.purchaseSupporter(activity)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("${stringResource(R.string.support_tier_supporter)} — ${supporterPrice ?: stringResource(R.string.billing_price_unavailable)}") }
                        Spacer(modifier = Modifier.height(8.dp))
                        FilledTonalButton(
                            enabled = !purchaseInProgress,
                            onClick = {
                                val activity = context as? android.app.Activity
                                if (activity != null) billingViewModel.purchasePremium(activity)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("${stringResource(R.string.support_tier_premium)} — ${premiumPrice ?: stringResource(R.string.billing_price_unavailable)}") }
                        Spacer(modifier = Modifier.height(8.dp))
                        FilledTonalButton(
                            enabled = !purchaseInProgress,
                            onClick = {
                                val activity = context as? android.app.Activity
                                if (activity != null) billingViewModel.purchaseLifetime(activity)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("${stringResource(R.string.support_tier_lifetime)} — ${lifetimePrice ?: stringResource(R.string.billing_price_unavailable)}") }
                    } else {
                        Text(
                            text = stringResource(R.string.support_thanks),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { billingViewModel.restorePurchases() },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.support_restore)) }
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
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 4.dp))
    }
}
