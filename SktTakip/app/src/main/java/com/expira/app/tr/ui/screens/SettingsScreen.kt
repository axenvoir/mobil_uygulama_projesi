package com.expira.app.tr.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expira.app.tr.ui.theme.DangerRed
import com.expira.app.tr.worker.NotificationHelper

@Composable
fun SettingsScreen(
    isTurkish: Boolean,
    onLanguageChange: (Boolean) -> Unit,
    onResetToSampleData: () -> Unit,
    onClearAllData: () -> Unit
) {
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showTestNotificationToast by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Info Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "🌿 Expira Android",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isTurkish)
                        "Saf Yerel Android (Kotlin & Jetpack Compose & Room)"
                    else
                        "Pure Native Android (Kotlin, Jetpack Compose, Room)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isTurkish) "Sürüm: 2.0.0 (API 36 Uyumlu)" else "Version: 2.0.0 (Target API 36)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        // Section: Language
        Text(
            text = if (isTurkish) "Dil Seçenekleri" else "Language",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isTurkish) "Uygulama Dili" else "App Language",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isTurkish) "Şu anda: Türkçe" else "Current: English",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = { onLanguageChange(true) },
                        colors = if (isTurkish) ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) else ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Text("🇹🇷 TR")
                    }
                    FilledTonalButton(
                        onClick = { onLanguageChange(false) },
                        colors = if (!isTurkish) ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) else ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Text("🇬🇧 EN")
                    }
                }
            }
        }

        // Section: Notifications & Alarms
        Text(
            text = if (isTurkish) "Bildirimler & Alarmlar" else "Notifications & Alarms",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTurkish) "Günlük SKT Kontrolü" else "Daily Expiry Check",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isTurkish)
                                "Arka planda (WorkManager) her gün otomatik çalışır ve yaklaşanları bildirir."
                            else
                                "Runs daily in background via WorkManager to alert you of items expiring soon.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Active",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                OutlinedButton(
                    onClick = {
                        NotificationHelper.showExpiryNotification(context, approachingCount = 2, expiredCount = 1)
                        showTestNotificationToast = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isTurkish) "Test Bildirimi Gönder" else "Send Test Notification")
                }

                if (showTestNotificationToast) {
                    Text(
                        text = if (isTurkish) "✓ Test bildirimi cihazınıza iletildi!" else "✓ Test notification sent to device!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Section: Data Management
        Text(
            text = if (isTurkish) "Veri Yönetimi" else "Data Management",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isTurkish) "Örnek Verileri Yükle" else "Load Sample Data")
                }

                Button(
                    onClick = { showClearDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isTurkish) "Tüm Veritabanını Sıfırla" else "Clear All Data")
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(if (isTurkish) "Örnek Verileri Yükle" else "Load Sample Data") },
            text = {
                Text(
                    if (isTurkish)
                        "Veritabanına başlangıç örnek ürünleri ve kiler notları eklensin mi?"
                    else
                        "Would you like to populate initial sample products and pantry notes?"
                )
            },
            confirmButton = {
                Button(onClick = {
                    onResetToSampleData()
                    showResetDialog = false
                }) {
                    Text(if (isTurkish) "Yükle" else "Load")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(if (isTurkish) "İptal" else "Cancel")
                }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(if (isTurkish) "Tüm Verileri Sil" else "Clear All Data") },
            text = {
                Text(
                    if (isTurkish)
                        "Tüm kayıtlı ürünleriniz ve notlarınız kalıcı olarak silinecek. Emin misiniz?"
                    else
                        "All saved items and notes will be permanently deleted. Are you sure?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text(if (isTurkish) "Hepsini Sil" else "Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(if (isTurkish) "İptal" else "Cancel")
                }
            }
        )
    }
}
