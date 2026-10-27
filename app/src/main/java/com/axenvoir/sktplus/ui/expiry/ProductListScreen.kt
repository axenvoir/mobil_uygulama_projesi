package com.axenvoir.sktplus.ui.expiry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
    var productDetail by remember { mutableStateOf<Product?>(null) }
    val errorText = stringResource(R.string.error_generic)

    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current
    var fabVisible by remember { mutableStateOf(true) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val undoText = stringResource(R.string.undo)

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

    val topBarScrollBehavior = androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        topBar = {
            androidx.compose.material3.LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.expiry_list_title),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                },
                actions = { SettingsIconButton(onClick = onOpenSettings) },
                scrollBehavior = topBarScrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            androidx.compose.animation.AnimatedVisibility(
                visible = fabVisible,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut(),
            ) {
                FloatingActionButton(onClick = onAddProduct) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_product))
                }
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
                            totalCount = uiState.products.size,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            onStatusClick = { status -> viewModel.onFilterChange(
                                when (status) {
                                    ExpiryStatus.FRESH -> ProductFilter.FRESH
                                    ExpiryStatus.EXPIRING_SOON -> ProductFilter.EXPIRING_SOON
                                    ExpiryStatus.EXPIRED -> ProductFilter.EXPIRED
                                }
                            ) },
                        )
                    }
                    FilterRow(
                        selected = uiState.filter,
                        onFilterChange = viewModel::onFilterChange,
                        freshBadge = freshCount,
                        soonBadge = expiringCount,
                        expiredBadge = expiredCount,
                    )
                    if (visibleProducts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (uiState.query.isNotEmpty()) {
                                EmptyState(
                                    icon = Icons.Filled.Search,
                                    title = stringResource(R.string.empty_search),
                                    subtitle = "",
                                )
                            } else {
                                EmptyState(
                                    icon = Icons.Filled.DateRange,
                                    title = stringResource(R.string.empty_products_title),
                                    subtitle = stringResource(R.string.empty_products),
                                    actionLabel = stringResource(R.string.add_product),
                                    onAction = onAddProduct,
                                )
                            }
                        }
                    } else {
                        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                        LaunchedEffect(listState) {
                            androidx.compose.runtime.snapshotFlow { listState.firstVisibleItemIndex }
                                .collect { index -> fabVisible = index == 0 }
                        }
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(visibleProducts, key = { it.id }, contentType = { "product" }) { product ->
                                val dismissState = androidx.compose.material3.rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value ->
                                        if (value == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart) {
                                            productPendingDelete = product
                                            true
                                        } else {
                                            false
                                        }
                                    },
                                )
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier.animateItem(),
                                ) {
                                    androidx.compose.material3.SwipeToDismissBox(
                                        state = dismissState,
                                        enableDismissFromStartToEnd = false,
                                        backgroundContent = {
                                            androidx.compose.foundation.layout.Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        MaterialTheme.colorScheme.errorContainer,
                                                        MaterialTheme.shapes.medium,
                                                    )
                                                    .padding(horizontal = 20.dp),
                                                contentAlignment = Alignment.CenterEnd,
                                            ) {
                                                Icon(
                                                    Icons.Filled.Delete,
                                                    contentDescription = stringResource(R.string.delete),
                                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                                )
                                            }
                                        },
                                    ) {
                                        ProductCard(
                                            product = product,
                                            onClick = { onEditProduct(product.id) },
                                            onLongClick = { productDetail = product },
                                            onDelete = { productPendingDelete = product },
                                        )
                                    }
                                }
                                androidx.compose.runtime.LaunchedEffect(productPendingDelete) {
                                    if (productPendingDelete == null &&
                                        dismissState.currentValue == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart
                                    ) {
                                        dismissState.snapTo(androidx.compose.material3.SwipeToDismissBoxValue.Settled)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    productDetail?.let { product ->
        val status = expiryStatus(product.expiryDate)
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { productDetail = null },
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .background(statusColor(status), MaterialTheme.shapes.large),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = statusLabel(status),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.categoryName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = product.expiryDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = remainingDaysText(product.expiryDate),
                    style = MaterialTheme.typography.bodyLarge,
                    color = statusColor(status),
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(20.dp))
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    androidx.compose.material3.Button(
                        onClick = {
                            productDetail = null
                            onEditProduct(product.id)
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.edit)) }
                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            productDetail = null
                            productPendingDelete = product
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.delete)) }
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
                        hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        viewModel.deleteProduct(product) {
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = product.name,
                                    actionLabel = undoText,
                                    duration = androidx.compose.material3.SnackbarDuration.Long,
                                )
                                if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                    viewModel.restoreProduct(product)
                                }
                            }
                        }
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
    totalCount: Int,
    modifier: Modifier = Modifier,
    onStatusClick: (ExpiryStatus) -> Unit = {},
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
                fraction = if (totalCount > 0) freshCount.toFloat() / totalCount else 0f,
                modifier = Modifier.weight(1f),
                onClick = { onStatusClick(ExpiryStatus.FRESH) },
            )
            SummaryItem(
                count = expiringCount,
                label = stringResource(R.string.status_expiring_soon),
                color = WarningOrangeDot,
                fraction = if (totalCount > 0) expiringCount.toFloat() / totalCount else 0f,
                modifier = Modifier.weight(1f),
                onClick = { onStatusClick(ExpiryStatus.EXPIRING_SOON) },
            )
            SummaryItem(
                count = expiredCount,
                label = stringResource(R.string.status_expired),
                color = ExpiredRedDot,
                fraction = if (totalCount > 0) expiredCount.toFloat() / totalCount else 0f,
                modifier = Modifier.weight(1f),
                onClick = { onStatusClick(ExpiryStatus.EXPIRED) },
            )
        }
    }
}

@Composable
private fun SummaryItem(
    count: Int,
    label: String,
    color: Color,
    fraction: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
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
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
        androidx.compose.material3.LinearProgressIndicator(
            progress = { fraction },
            trackColor = MaterialTheme.colorScheme.surface,
            color = color,
            modifier = Modifier
                .width(48.dp)
                .height(4.dp),
        )
    }
}

@Composable
private fun trailingBadge(count: Int): @Composable () -> Unit = {
    Text(
        text = count.toString(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
            .padding(horizontal = 6.dp, vertical = 1.dp),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterRow(
    selected: ProductFilter,
    onFilterChange: (ProductFilter) -> Unit,
    freshBadge: Int = 0,
    soonBadge: Int = 0,
    expiredBadge: Int = 0,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
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
            trailingIcon = if (freshBadge > 0) trailingBadge(freshBadge) else null,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            ),
        )
        FilterChip(
            selected = selected == ProductFilter.EXPIRING_SOON,
            onClick = { onFilterChange(ProductFilter.EXPIRING_SOON) },
            label = { Text(stringResource(R.string.status_expiring_soon)) },
            leadingIcon = { StatusDot(WarningOrangeDot) },
            trailingIcon = if (soonBadge > 0) trailingBadge(soonBadge) else null,
        )
        FilterChip(
            selected = selected == ProductFilter.EXPIRED,
            onClick = { onFilterChange(ProductFilter.EXPIRED) },
            label = { Text(stringResource(R.string.status_expired)) },
            leadingIcon = { StatusDot(ExpiredRedDot) },
            trailingIcon = if (expiredBadge > 0) trailingBadge(expiredBadge) else null,
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
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val status = expiryStatus(product.expiryDate)
    val statusContainer = when (status) {
        ExpiryStatus.FRESH -> MaterialTheme.colorScheme.surface
        ExpiryStatus.EXPIRING_SOON -> com.axenvoir.sktplus.ui.theme.WarningOrangeContainer
        ExpiryStatus.EXPIRED -> com.axenvoir.sktplus.ui.theme.ExpiredRedContainer
    }
    Card(
        onClick = onClick,
        onLongClick = onLongClick,
        colors = CardDefaults.cardColors(
            containerColor = statusContainer,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
                        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                            com.axenvoir.sktplus.ui.components.CategoryAvatar(name = product.categoryName)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = product.categoryName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "x${product.quantity}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.shapes.small,
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val days = daysUntil(product.expiryDate)
                        Text(
                            text = if (days < 0) days.toString() else days.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor(status),
                        )
                        Text(
                            text = if (days < 0) stringResource(R.string.status_expired)
                            else stringResource(R.string.days_short),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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
                        Icons.Filled.DateRange,
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
