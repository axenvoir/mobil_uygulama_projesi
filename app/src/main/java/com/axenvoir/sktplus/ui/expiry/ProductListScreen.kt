package com.axenvoir.sktplus.ui.expiry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import com.axenvoir.sktplus.ui.theme.ExpiredRed
import com.axenvoir.sktplus.ui.theme.FreshGreen
import com.axenvoir.sktplus.ui.theme.WarningOrange
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onAddProduct: () -> Unit,
    onEditProduct: (Long) -> Unit,
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

    val visibleProducts = when (uiState.filter) {
        ProductFilter.ALL -> uiState.products
        ProductFilter.FRESH -> uiState.products.filter { expiryStatus(it.expiryDate) == ExpiryStatus.FRESH }
        ProductFilter.EXPIRING_SOON -> uiState.products.filter { expiryStatus(it.expiryDate) == ExpiryStatus.EXPIRING_SOON }
        ProductFilter.EXPIRED -> uiState.products.filter { expiryStatus(it.expiryDate) == ExpiryStatus.EXPIRED }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.expiry_list_title)) }) },
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
            FilterRow(uiState.filter, viewModel::onFilterChange)
            if (visibleProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.empty_products))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(visibleProducts, key = { it.id }) { product ->
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

    productPendingDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productPendingDelete = null },
            title = { Text(stringResource(R.string.delete_confirm)) },
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
        )
        FilterChip(
            selected = selected == ProductFilter.EXPIRING_SOON,
            onClick = { onFilterChange(ProductFilter.EXPIRING_SOON) },
            label = { Text(stringResource(R.string.status_expiring_soon)) },
        )
        FilterChip(
            selected = selected == ProductFilter.EXPIRED,
            onClick = { onFilterChange(ProductFilter.EXPIRED) },
            label = { Text(stringResource(R.string.status_expired)) },
        )
    }
}

@Composable
fun statusColor(status: ExpiryStatus): Color = when (status) {
    ExpiryStatus.FRESH -> FreshGreen
    ExpiryStatus.EXPIRING_SOON -> WarningOrange
    ExpiryStatus.EXPIRED -> ExpiredRed
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
    Card(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .background(statusColor(status), CircleShape)
                    .height(12.dp)
                    .fillMaxWidth(0.03f),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = product.categoryName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.expiryDate.format(
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "${statusLabel(status)} · ${remainingDaysText(product.expiryDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor(status),
                )
            }
            Text(
                text = "x${product.quantity}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.delete),
                )
            }
        }
    }
}
