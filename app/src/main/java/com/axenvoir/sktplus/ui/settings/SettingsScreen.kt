package com.axenvoir.sktplus.ui.settings

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.axenvoir.sktplus.BuildConfig
import com.axenvoir.sktplus.R
import com.axenvoir.sktplus.billing.BillingViewModel
import com.axenvoir.sktplus.billing.UserTier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    billingViewModel: BillingViewModel = hiltViewModel(),
) {
    val selected by viewModel.current.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val exportDoneMessage = stringResource(R.string.export_done)
    val exportFailedMessage = stringResource(R.string.export_failed)
    val testNotificationSentMessage = stringResource(R.string.test_notification_sent)
    val notificationsBlockedMessage = stringResource(R.string.notifications_blocked)
    val openSettingsLabel = stringResource(R.string.open_settings)
    val scope = androidx.compose.runtime.rememberCoroutineScope()
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
                title = { Text(stringResource(R.string.settings_title)) },
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
                    OutlinedButton(
                        onClick = {
                            val sent = viewModel.sendTestNotification()
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = if (sent) testNotificationSentMessage
                                    else notificationsBlockedMessage,
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
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.test_notification)) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                SettingsCard(title = stringResource(R.string.data_section)) {
                    OutlinedButton(
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
                    Text(
                        text = stringResource(R.string.support_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (tier == UserTier.FREE) {
                        OutlinedButton(
                            onClick = {
                                val activity = context as? android.app.Activity
                                if (activity != null) billingViewModel.purchaseSupporter(activity)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(stringResource(R.string.support_tier_supporter)) }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val activity = context as? android.app.Activity
                                if (activity != null) billingViewModel.purchasePremium(activity)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(stringResource(R.string.support_tier_premium)) }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val activity = context as? android.app.Activity
                                if (activity != null) billingViewModel.purchaseLifetime(activity)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(stringResource(R.string.support_tier_lifetime)) }
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
