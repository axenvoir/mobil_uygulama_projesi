package com.axenvoir.expira.ui.expiry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import com.axenvoir.expira.ui.components.StatusDot
import com.axenvoir.expira.ui.components.expiraChipColors
import com.axenvoir.expira.ui.components.expiraTopAppBarColors
import com.axenvoir.expira.ui.main.SettingsIconButton
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private enum class ProductEmptyReason {
    REAL_EMPTY,
    SEARCH_EMPTY,
    FILTER_EMPTY,
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    var productDetail by remember { mutableStateOf<Product?>(null) }
    var fabVisible by remember { mutableStateOf(true) }

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
            AnimatedVisibility(
                visible = fabVisible && uiState.allProducts.isNotEmpty(),
                enter = fadeIn() + scaleIn(initialScale = 0.85f),
                exit = fadeOut() + scaleOut(targetScale = 0.85f),
            ) {
                FloatingActionButton(
                    onClick = onAddProduct,
                    modifier = Modifier.padding(16.dp),
                ) {
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

                if (!uiState.isLoading && uiState.allProducts.isNotEmpty()) {
                    if (uiState.products.isNotEmpty()) {
                        SummaryCard(
                            freshCount = uiState.freshCount,
                            expiringCount = uiState.expiringCount,
                            expiredCount = uiState.expiredCount,
                            totalCount = uiState.products.size,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            onStatusClick = { status ->
                                viewModel.onFilterChange(
                                    when (status) {
                                        ExpiryStatus.FRESH -> ProductFilter.FRESH
                                        ExpiryStatus.EXPIRING_SOON -> ProductFilter.EXPIRING_SOON
                                        ExpiryStatus.EXPIRED -> ProductFilter.EXPIRED
                                    },
                                )
                            },
                        )
                    }
                    // Counts live in SummaryCard; repeating them in filter chips adds noise.
                    FilterRow(
                        selected = uiState.filter,
                        onFilterChange = viewModel::onFilterChange,
                    )
                }

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
                        LaunchedEffect(listState) {
                            androidx.compose.runtime.snapshotFlow { listState.firstVisibleItemIndex }
                                .collect { index -> fabVisible = index == 0 }
                        }
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
                            items(
                                items = visibleProducts,
                                key = { it.id },
                                contentType = { "product" },
                            ) { product ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value ->
                                        if (value == SwipeToDismissBoxValue.EndToStart) {
                                            productPendingDelete = product
                                        }
                                        false
                                    },
                                )
                                Box(modifier = Modifier.animateItem()) {
                                    SwipeToDismissBox(
                                        state = dismissState,
                                        enableDismissFromStartToEnd = false,
                                        backgroundContent = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(MaterialTheme.colorScheme.errorContainer),
                                                contentAlignment = Alignment.CenterEnd,
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = stringResource(R.string.delete),
                                                    tint = MaterialTheme.colorScheme.onErrorContainer,
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp)
                        .background(statusColor(status), MaterialTheme.shapes.large),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = statusLabel(status),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
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
            onDismissRequest = { productPendingDelete = null },
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
                        productPendingDelete = null
                    },
                ) {
                    Text(stringResource(R.string.delete_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { productPendingDelete = null }) {
                    Text(stringResource(R.string.cancel_action))
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
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
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
                Text(
                    text = stringResource(R.string.product_summary_total, totalCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
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
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        androidx.compose.material3.LinearProgressIndicator(
            progress = { fraction },
            trackColor = MaterialTheme.colorScheme.surface,
            color = color,
            modifier = Modifier
                .width(48.dp)
                .height(6.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterRow(
    selected: ProductFilter,
    onFilterChange: (ProductFilter) -> Unit,
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
            shape = RoundedCornerShape(50),
            colors = expiraChipColors(),
        )
        FilterChip(
            selected = selected == ProductFilter.FRESH,
            onClick = { onFilterChange(ProductFilter.FRESH) },
            label = { Text(stringResource(R.string.status_fresh)) },
            leadingIcon = { StatusDot(FreshGreenDot) },
            shape = RoundedCornerShape(50),
            colors = expiraChipColors(),
        )
        FilterChip(
            selected = selected == ProductFilter.EXPIRING_SOON,
            onClick = { onFilterChange(ProductFilter.EXPIRING_SOON) },
            label = { Text(stringResource(R.string.status_expiring_soon)) },
            leadingIcon = { StatusDot(WarningOrangeDot) },
            shape = RoundedCornerShape(50),
            colors = expiraChipColors(),
        )
        FilterChip(
            selected = selected == ProductFilter.EXPIRED,
            onClick = { onFilterChange(ProductFilter.EXPIRED) },
            label = { Text(stringResource(R.string.status_expired)) },
            leadingIcon = { StatusDot(ExpiredRedDot) },
            shape = RoundedCornerShape(50),
            colors = expiraChipColors(),
        )
    }
}

private val FreshGreenDot = com.axenvoir.expira.ui.theme.FreshGreen
private val WarningOrangeDot = com.axenvoir.expira.ui.theme.WarningOrange
private val ExpiredRedDot = com.axenvoir.expira.ui.theme.ExpiredRed

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
fun remainingDaysText(
    expiryDate: LocalDate,
    referenceDate: LocalDate,
): String {
    val days = daysUntil(expiryDate, referenceDate)
    return when {
        days == 0L -> stringResource(R.string.expires_today)
        days < 0 -> stringResource(R.string.expired_days, -days)
        else -> stringResource(R.string.days_left, days)
    }
}

@Composable
private fun ProductCard(
    product: Product,
    referenceDate: LocalDate,
    onClick: () -> Unit,
) {
    val status = expiryStatus(product.expiryDate, referenceDate)
    val days = daysUntil(product.expiryDate, referenceDate)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 0.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .background(statusColor(status), RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp)),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    com.axenvoir.expira.ui.components.CategoryAvatar(name = product.categoryName)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = product.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = product.quantity.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
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
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusLabel(status),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor(status),
                        modifier = Modifier
                            .widthIn(max = 112.dp)
                            .background(
                                color = statusColor(status).copy(alpha = 0.12f),
                                shape = MaterialTheme.shapes.small,
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .widthIn(min = 48.dp, max = 72.dp),
            ) {
                Text(
                    text = kotlin.math.abs(days).toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor(status),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (days < 0) stringResource(R.string.days_passed_short)
                    else stringResource(R.string.days_short),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
