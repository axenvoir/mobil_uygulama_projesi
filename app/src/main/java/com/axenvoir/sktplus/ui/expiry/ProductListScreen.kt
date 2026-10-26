package com.axenvoir.sktplus.ui.expiry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axenvoir.sktplus.R
import com.axenvoir.sktplus.domain.model.ExpiryStatus
import com.axenvoir.sktplus.domain.model.Product
import com.axenvoir.sktplus.domain.util.daysUntil
import com.axenvoir.sktplus.domain.util.expiryStatus
import com.axenvoir.sktplus.ui.components.EmptyState
import com.axenvoir.sktplus.ui.main.SettingsIconButton
import com.axenvoir.sktplus.ui.components.StatusDot
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onAddProduct: () -> Unit,
    onEditProduct: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ProductListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var productPendingDelete by remember { mutableStateOf<Product?>(null) }
    val errorText = stringResource(R.string.error_generic)

    LaunchedEffect(error) {
        if (error) {
            snackbarHostState.showSnackbar(errorText)
            viewModel.consumeError()
        }
    }

    val freshCount = uiState.products.count { expiryStatus(it.expiryDate) == ExpiryStatus.FRESH }
    val expiringCount = uiState.products.count { expiryStatus(it.expiryDate) == ExpiryStatus.EXPIRING_SOON }
    val expiredCount = uiState.products.count { expiryStatus(it.expiryDate) == ExpiryStatus.EXPIRED }

    val visibleProducts = when (uiState.filter) {
        ProductFilter.ALL -> uiState.products
        ProductFilter.FRESH -> uiState.products.filter { expiryStatus(it.expiryDate) == ExpiryStatus.FRESH }
        ProductFilter.EXPIRING_SOON -> uiState.products.filter { expiryStatus(it.expiryDate) == ExpiryStatus.EXPIRING_SOON }
        ProductFilter.EXPIRED -> uiState.products.filter { expiryStatus(it.expiryDate) == ExpiryStatus.EXPIRED }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.expiry_list_title)) },
                actions = { SettingsIconButton(onClick = onOpenSettings) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduct) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_product))
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 800.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (uiState.products.isNotEmpty()) {
                        SummaryCard(
                            freshCount = freshCount,
                            expiringCount = expiringCount,
                            expiredCount = expiredCount,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    FilterRow(uiState.filter, viewModel::onFilterChange)
                    if (visibleProducts.isEmpty()) {
                        EmptyState(
                            icon = Icons.Filled.Inventory2,
                            title = stringResource(R.string.empty_products_title),
                            subtitle = stringResource(R.string.empty_products),
                            actionLabel = stringResource(R.string.add_product),
                            onAction = onAddProduct,
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(visibleProducts, key = { it.id }) { product ->
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier.animateItem(),
                                ) {
                                    ProductCard(
                                        product = product,
                                        onClick = { onEditProduct(product.id) },
                                        onDelete = { productPendingDelete = product },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    productPendingDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productPendingDelete = null },
            title = { Text(stringResource(R.string.delete_confirm)) },
            text = { Text(product.name) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProduct(product)
                        productPendingDelete = null
                    },
                ) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = {
                TextButton(onClick = { productPendingDelete = null }) {
                    Text(stringResource(R.string.no))
                }
            },
        )
    }
}

@Composable
private fun SummaryCard(
    freshCount: Int,
    expiringCount: Int,
    expiredCount: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
        ) {
            SummaryItem(
                count = freshCount,
                label = stringResource(R.string.status_fresh),
                color = FreshGreenDot,
                modifier = Modifier.weight(1f),
            )
            SummaryItem(
                count = expiringCount,
                label = stringResource(R.string.status_expiring_soon),
                color = WarningOrangeDot,
                modifier = Modifier.weight(1f),
            )
            SummaryItem(
                count = expiredCount,
                label = stringResource(R.string.status_expired),
                color = ExpiredRedDot,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryItem(count: Int, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FilterRow(selected: ProductFilter, onFilterChange: (ProductFilter) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selected == ProductFilter.ALL,
            onClick = { onFilterChange(ProductFilter.ALL) },
            label = { Text(stringResource(R.string.all)) },
        )
        FilterChip(
            selected = selected == ProductFilter.FRESH,
            onClick = { onFilterChange(ProductFilter.FRESH) },
            label = { Text(stringResource(R.string.status_fresh)) },
            leadingIcon = { StatusDot(FreshGreenDot) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            ),
        )
        FilterChip(
            selected = selected == ProductFilter.EXPIRING_SOON,
            onClick = { onFilterChange(ProductFilter.EXPIRING_SOON) },
            label = { Text(stringResource(R.string.status_expiring_soon)) },
            leadingIcon = { StatusDot(WarningOrangeDot) },
        )
        FilterChip(
            selected = selected == ProductFilter.EXPIRED,
            onClick = { onFilterChange(ProductFilter.EXPIRED) },
            label = { Text(stringResource(R.string.status_expired)) },
            leadingIcon = { StatusDot(ExpiredRedDot) },
        )
    }
}

private val FreshGreenDot = com.axenvoir.sktplus.ui.theme.FreshGreen
private val WarningOrangeDot = com.axenvoir.sktplus.ui.theme.WarningOrange
private val ExpiredRedDot = com.axenvoir.sktplus.ui.theme.ExpiredRed

@Composable
fun statusColor(status: ExpiryStatus) = when (status) {
    ExpiryStatus.FRESH -> FreshGreenDot
    ExpiryStatus.EXPIRING_SOON -> WarningOrangeDot
    ExpiryStatus.EXPIRED -> ExpiredRedDot
}

@Composable
fun statusLabel(status: ExpiryStatus): String = when (status) {
    ExpiryStatus.FRESH -> stringResource(R.string.status_fresh)
    ExpiryStatus.EXPIRING_SOON -> stringResource(R.string.status_expiring_soon)
    ExpiryStatus.EXPIRED -> stringResource(R.string.status_expired)
}

@Composable
fun remainingDaysText(expiryDate: java.time.LocalDate): String {
    val days = daysUntil(expiryDate)
    return when {
        days == 0L -> stringResource(R.string.expires_today)
        days < 0 -> stringResource(R.string.expired_days, -days)
        else -> stringResource(R.string.days_left, days)
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val status = expiryStatus(product.expiryDate)
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp)
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxSize()
                    .padding(vertical = 10.dp)
                    .background(
                        color = statusColor(status),
                        shape = MaterialTheme.shapes.small,
                    ),
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = product.categoryName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    AssistChip(
                        onClick = {},
                        label = { Text("x${product.quantity}") },
                    )
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = statusColor(status),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = product.expiryDate.format(
                            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusDot(statusColor(status))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${statusLabel(status)} · ${remainingDaysText(product.expiryDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor(status),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
