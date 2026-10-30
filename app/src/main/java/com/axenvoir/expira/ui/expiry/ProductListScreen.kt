package com.axenvoir.expira.ui.expiry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.semantics.Role
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axenvoir.expira.R
import com.axenvoir.expira.domain.model.ExpiryStatus
import com.axenvoir.expira.domain.model.Product
import com.axenvoir.expira.domain.util.daysUntil
import com.axenvoir.expira.domain.util.expiryStatus
import com.axenvoir.expira.ui.components.ExpiraEmptyState
import com.axenvoir.expira.ui.components.expiraTopAppBarColors
import com.axenvoir.expira.ui.components.CategoryAvatar
import com.axenvoir.expira.ui.main.SettingsIconButton
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private enum class ProductEmptyReason {
    REAL_EMPTY,
    SEARCH_EMPTY,
    FILTER_EMPTY,
}

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
    val listState = rememberLazyListState()
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val undoText = stringResource(R.string.undo)
    val errorText = stringResource(R.string.error_generic)
    var productPendingDelete by remember { mutableStateOf<Product?>(null) }
    var restoreSwipedProduct by remember { mutableStateOf<(() -> Unit)?>(null) }
    var productDetail by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(error) {
        if (error) {
            snackbarHostState.showSnackbar(errorText)
            viewModel.consumeError()
        }
    }

    val referenceDate = uiState.currentDate
    val visibleProducts = uiState.visibleProducts
    val emptyReason = when {
        uiState.isLoading -> null
        uiState.allProducts.isEmpty() -> ProductEmptyReason.REAL_EMPTY
        uiState.hasActiveSearch && uiState.products.isEmpty() -> ProductEmptyReason.SEARCH_EMPTY
        visibleProducts.isEmpty() -> ProductEmptyReason.FILTER_EMPTY
        else -> null
    }
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        topBar = {
            androidx.compose.material3.MediumTopAppBar(
                colors = expiraTopAppBarColors(),
                title = {
                    Text(
                        text = stringResource(R.string.expiry_list_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = { SettingsIconButton(onClick = onOpenSettings) },
                scrollBehavior = topBarScrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!uiState.isLoading && uiState.allProducts.isNotEmpty()) {
                FloatingActionButton(onClick = onAddProduct) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_product),
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 800.dp)
                    .align(Alignment.TopCenter),
            ) {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.search_products)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.search_products),
                        )
                    },
                    trailingIcon = if (uiState.query.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.onQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.clear_search),
                                )
                            }
                        }
                    } else {
                        null
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.extraLarge,
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    if (uiState.isLoading) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.products_loading),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else if (emptyReason != null) {
                        when (emptyReason) {
                            ProductEmptyReason.REAL_EMPTY -> ExpiraEmptyState(
                                icon = Icons.Filled.DateRange,
                                title = stringResource(R.string.empty_products_title),
                                subtitle = stringResource(R.string.empty_products),
                                actionLabel = stringResource(R.string.add_product),
                                onAction = onAddProduct,
                            )
                            ProductEmptyReason.SEARCH_EMPTY -> ExpiraEmptyState(
                                icon = Icons.Filled.Search,
                                title = stringResource(R.string.empty_search),
                                subtitle = stringResource(R.string.empty_search_subtitle),
                                actionLabel = stringResource(R.string.clear_search),
                                onAction = { viewModel.onQueryChange("") },
                            )
                            ProductEmptyReason.FILTER_EMPTY -> ExpiraEmptyState(
                                icon = Icons.Filled.DateRange,
                                title = stringResource(R.string.empty_filter_title),
                                subtitle = stringResource(R.string.empty_filter_subtitle),
                                actionLabel = stringResource(R.string.empty_filter_action),
                                onAction = { viewModel.onFilterChange(ProductFilter.ALL) },
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 96.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            item(key = "expiry-overview", contentType = "overview") {
                                ExpiryOverviewCard(
                                    freshCount = uiState.freshCount,
                                    expiringCount = uiState.expiringCount,
                                    expiredCount = uiState.expiredCount,
                                    totalCount = uiState.products.size,
                                    selected = uiState.filter,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    onFilterChange = viewModel::onFilterChange,
                                )
                            }
                            items(
                                items = visibleProducts,
                                key = { it.id },
                                contentType = { "product" },
                            ) { product ->
                                val dismissState = rememberSwipeToDismissBoxState()
                                LaunchedEffect(dismissState.currentValue) {
                                    if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                                        restoreSwipedProduct = {
                                            scope.launch { dismissState.reset() }
                                        }
                                        productPendingDelete = product
                                    }
                                }
                                Box(modifier = Modifier.animateItem()) {
                                    SwipeToDismissBox(
                                        state = dismissState,
                                        enableDismissFromStartToEnd = false,
                                        backgroundContent = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(MaterialTheme.shapes.extraLarge)
                                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                                contentAlignment = Alignment.CenterEnd,
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = stringResource(R.string.delete),
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.padding(end = 24.dp),
                                                )
                                            }
                                        },
                                    ) {
                                        ProductCard(
                                            product = product,
                                            referenceDate = referenceDate,
                                            onClick = { productDetail = product },
                                        )
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
        val status = expiryStatus(product.expiryDate, referenceDate)
        ModalBottomSheet(onDismissRequest = { productDetail = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
            ) {
                Surface(
                    color = statusColor(status).copy(alpha = 0.12f),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = statusLabel(status),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor(status),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = remainingDaysText(product.expiryDate, referenceDate),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                if (product.categoryName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.categoryName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = product.expiryDate.format(
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL),
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = remainingDaysText(product.expiryDate, referenceDate),
                    style = MaterialTheme.typography.bodyLarge,
                    color = statusColor(status),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    androidx.compose.material3.Button(
                        onClick = {
                            productDetail = null
                            onEditProduct(product.id)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.edit), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    OutlinedButton(
                        onClick = {
                            productDetail = null
                            productPendingDelete = product
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.delete), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }

    productPendingDelete?.let { product ->
        AlertDialog(
            onDismissRequest = {
                restoreSwipedProduct?.invoke()
                restoreSwipedProduct = null
                productPendingDelete = null
            },
            title = { Text(stringResource(R.string.delete_confirm)) },
            text = {
                Text(
                    text = product.name,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
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
                        restoreSwipedProduct?.invoke()
                        restoreSwipedProduct = null
                        productPendingDelete = null
                    },
                ) {
                    Text(stringResource(R.string.delete_action))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        restoreSwipedProduct?.invoke()
                        restoreSwipedProduct = null
                        productPendingDelete = null
                    },
                ) {
                    Text(stringResource(R.string.cancel_action))
                }
            },
        )
    }
}

@Composable
private fun ExpiryOverviewCard(
    freshCount: Int,
    expiringCount: Int,
    expiredCount: Int,
    totalCount: Int,
    selected: ProductFilter,
    modifier: Modifier = Modifier,
    onFilterChange: (ProductFilter) -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.product_summary_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Surface(
                    modifier = Modifier.selectable(
                        selected = selected == ProductFilter.ALL,
                        onClick = { onFilterChange(ProductFilter.ALL) },
                        role = Role.RadioButton,
                    ),
                    shape = MaterialTheme.shapes.small,
                    color = if (selected == ProductFilter.ALL) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    contentColor = if (selected == ProductFilter.ALL) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                ) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.product_summary_total,
                            totalCount,
                            totalCount,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ExpiryOverviewMetric(
                    count = freshCount,
                    label = stringResource(R.string.status_fresh),
                    color = statusColor(ExpiryStatus.FRESH),
                    selected = selected == ProductFilter.FRESH,
                    modifier = Modifier.weight(1f),
                    onClick = { onFilterChange(ProductFilter.FRESH) },
                )
                ExpiryOverviewMetric(
                    count = expiringCount,
                    label = stringResource(R.string.status_expiring_soon),
                    color = statusColor(ExpiryStatus.EXPIRING_SOON),
                    selected = selected == ProductFilter.EXPIRING_SOON,
                    modifier = Modifier.weight(1f),
                    onClick = { onFilterChange(ProductFilter.EXPIRING_SOON) },
                )
                ExpiryOverviewMetric(
                    count = expiredCount,
                    label = stringResource(R.string.status_expired),
                    color = statusColor(ExpiryStatus.EXPIRED),
                    selected = selected == ProductFilter.EXPIRED,
                    modifier = Modifier.weight(1f),
                    onClick = { onFilterChange(ProductFilter.EXPIRED) },
                )
            }
        }
    }
}

@Composable
private fun ExpiryOverviewMetric(
    count: Int,
    label: String,
    color: Color,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .heightIn(min = 72.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            ),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) color.copy(alpha = 0.14f) else Color.Transparent,
        contentColor = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun statusColor(status: ExpiryStatus): Color = when (status) {
    ExpiryStatus.FRESH -> MaterialTheme.colorScheme.primary
    ExpiryStatus.EXPIRING_SOON -> MaterialTheme.colorScheme.tertiary
    ExpiryStatus.EXPIRED -> MaterialTheme.colorScheme.error
}

@Composable
fun statusLabel(status: ExpiryStatus): String = when (status) {
    ExpiryStatus.FRESH -> stringResource(R.string.status_fresh)
    ExpiryStatus.EXPIRING_SOON -> stringResource(R.string.status_expiring_soon)
    ExpiryStatus.EXPIRED -> stringResource(R.string.status_expired)
}

@Composable
fun remainingDaysText(
    expiryDate: LocalDate,
    referenceDate: LocalDate,
): String {
    val days = daysUntil(expiryDate, referenceDate)
    return when {
        days == 0L -> stringResource(R.string.expires_today)
        days < 0 -> {
            val elapsed = -days
            androidx.compose.ui.res.pluralStringResource(
                R.plurals.days_past,
                elapsed.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                elapsed,
            )
        }
        else -> androidx.compose.ui.res.pluralStringResource(
            R.plurals.days_remaining,
            days.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            days,
        )
    }
}

@Composable
private fun ProductCard(
    product: Product,
    referenceDate: LocalDate,
    onClick: () -> Unit,
) {
    val status = expiryStatus(product.expiryDate, referenceDate)
    val quantityLabel = androidx.compose.ui.res.pluralStringResource(
        R.plurals.product_quantity,
        product.quantity.coerceAtLeast(0),
        product.quantity.coerceAtLeast(0),
    )
    val storageLocation = storageLocationLabel(product.storageLocation)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryAvatar(name = product.categoryName)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (product.categoryName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = product.categoryName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = statusColor(status).copy(alpha = 0.13f),
                    contentColor = statusColor(status),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.widthIn(max = 116.dp),
                ) {
                    Text(
                        text = remainingDaysText(product.expiryDate, referenceDate),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = product.expiryDate.format(
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buildString {
                        append(quantityLabel)
                        if (storageLocation.isNotBlank()) append(" · $storageLocation")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
private fun storageLocationLabel(storageLocation: String): String = when (storageLocation) {
    "fridge" -> stringResource(R.string.location_fridge)
    "pantry" -> stringResource(R.string.location_pantry)
    "freezer" -> stringResource(R.string.location_freezer)
    "other" -> stringResource(R.string.location_other)
    else -> storageLocation
}
