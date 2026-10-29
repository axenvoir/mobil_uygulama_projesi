package com.axenvoir.expira.ui.expiry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axenvoir.expira.R
import com.axenvoir.expira.domain.model.ExpiryStatus
import com.axenvoir.expira.domain.model.Product
import com.axenvoir.expira.domain.util.daysUntil
import com.axenvoir.expira.domain.util.expiryStatus
import com.axenvoir.expira.ui.components.ExpiraEmptyState
import com.axenvoir.expira.ui.main.SettingsIconButton
import com.axenvoir.expira.ui.components.ExpiraStatusDot
import com.axenvoir.expira.ui.components.StatusDot
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
    val listState = rememberLazyListState()

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

    val referenceDate = uiState.currentDate
    val freshCount = uiState.freshCount
    val expiringCount = uiState.expiringCount
    val expiredCount = uiState.expiredCount
    val visibleProducts = uiState.visibleProducts

    val topBarScrollBehavior = androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        topBar = {
            androidx.compose.material3.MediumTopAppBar(
                colors = com.axenvoir.expira.ui.components.expiraTopAppBarColors(),
                title = {
                    Text(
                        text = stringResource(R.string.expiry_list_title),
                        maxLines = 1,
                    )
                },
                actions = { SettingsIconButton(onClick = onOpenSettings) },
                scrollBehavior = topBarScrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            androidx.compose.animation.AnimatedVisibility(
                visible = fabVisible && visibleProducts.isNotEmpty(),
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(
                    initialScale = 0.85f,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    ),
                ),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(
                    targetScale = 0.85f,
                ),
            ) {
                FloatingActionButton(
                    onClick = onAddProduct,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_product))
                }
            }
        },
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 800.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = viewModel::onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text(stringResource(R.string.search_products)) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search_products)) },
                        trailingIcon = if (uiState.query.isNotEmpty()) {
                            {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(
                                        Icons.Filled.Close,
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
                    val eatFirst = remember(uiState.products, referenceDate) {
                        uiState.products
                            .filter { expiryStatus(it.expiryDate, referenceDate) != ExpiryStatus.EXPIRED }
                            .sortedBy { it.expiryDate }
                            .take(3)
                    }
                    if (eatFirst.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = stringResource(R.string.eat_first),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                eatFirst.forEach { item ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 3.dp),
                                    ) {
                                        StatusDot(statusColor(expiryStatus(item.expiryDate, referenceDate)))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        )
                                        val d = daysUntil(item.expiryDate, referenceDate)
                                        Text(
                                            text = "$d ${stringResource(R.string.days_short)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
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
                                ExpiraEmptyState(
                                    icon = Icons.Filled.Search,
                                    title = stringResource(R.string.empty_search),
                                    subtitle = stringResource(R.string.empty_search_subtitle),
                                    actionLabel = stringResource(R.string.clear_search),
                                    onAction = { viewModel.onQueryChange("") },
                                )
                            } else {
                                ExpiraEmptyState(
                                    icon = Icons.Filled.DateRange,
                                    title = stringResource(R.string.empty_products_title),
                                    subtitle = stringResource(R.string.empty_products),
                                    actionLabel = stringResource(R.string.add_product),
                                    onAction = onAddProduct,
                                )
                            }
                        }
                    } else {
                        LaunchedEffect(listState) {
                            androidx.compose.runtime.snapshotFlow { listState.firstVisibleItemIndex }
                                .collect { index -> fabVisible = index == 0 }
                        }
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(visibleProducts, key = { it.id }, contentType = { "product" }) { product ->
                                val dismissState = androidx.compose.material3.rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value ->
                                        if (value == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart) {
                                            productPendingDelete = product
                                        }
                                        false
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
                                                    .fillMaxSize()
                                                    .background(MaterialTheme.colorScheme.errorContainer),
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
                                            referenceDate = referenceDate,
                                            onClick = { onEditProduct(product.id) },
                                            onLongClick = { productDetail = product },
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
    }

    productDetail?.let { product ->
        val status = expiryStatus(product.expiryDate, uiState.currentDate)
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { productDetail = null },
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
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
                ) { Text(stringResource(R.string.delete_action)) }
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                )
                Text(
                    text = stringResource(R.string.product_summary_total, totalCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
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
                .height(6.dp),
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
            shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
            colors = com.axenvoir.expira.ui.components.expiraChipColors(),
        )
        FilterChip(
            selected = selected == ProductFilter.FRESH,
            onClick = { onFilterChange(ProductFilter.FRESH) },
            label = { Text(stringResource(R.string.status_fresh)) },
            leadingIcon = { com.axenvoir.expira.ui.components.ExpiraStatusDot(FreshGreenDot) },
            trailingIcon = if (freshBadge > 0) trailingBadge(freshBadge) else null,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
            colors = com.axenvoir.expira.ui.components.expiraChipColors(),
        )
        FilterChip(
            selected = selected == ProductFilter.EXPIRING_SOON,
            onClick = { onFilterChange(ProductFilter.EXPIRING_SOON) },
            label = { Text(stringResource(R.string.status_expiring_soon)) },
            leadingIcon = { com.axenvoir.expira.ui.components.ExpiraStatusDot(WarningOrangeDot) },
            trailingIcon = if (soonBadge > 0) trailingBadge(soonBadge) else null,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
            colors = com.axenvoir.expira.ui.components.expiraChipColors(),
        )
        FilterChip(
            selected = selected == ProductFilter.EXPIRED,
            onClick = { onFilterChange(ProductFilter.EXPIRED) },
            label = { Text(stringResource(R.string.status_expired)) },
            leadingIcon = { com.axenvoir.expira.ui.components.ExpiraStatusDot(ExpiredRedDot) },
            trailingIcon = if (expiredBadge > 0) trailingBadge(expiredBadge) else null,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
            colors = com.axenvoir.expira.ui.components.expiraChipColors(),
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
fun remainingDaysText(expiryDate: java.time.LocalDate): String {
    val days = daysUntil(expiryDate)
    return when {
        days == 0L -> stringResource(R.string.expires_today)
        days < 0 -> stringResource(R.string.expired_days, -days)
        else -> stringResource(R.string.days_left, days)
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ProductCard(
    product: Product,
    referenceDate: java.time.LocalDate,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val status = expiryStatus(product.expiryDate, referenceDate)
    val days = daysUntil(product.expiryDate, referenceDate)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
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
                    .background(statusColor(status), androidx.compose.foundation.shape.RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp))
                    .semantics { invisibleToUser() },
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    com.axenvoir.expira.ui.components.CategoryAvatar(name = product.categoryName)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = product.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "x${product.quantity}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = MaterialTheme.shapes.small,
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = product.expiryDate.format(
                            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = statusLabel(status),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor(status),
                        modifier = Modifier
                            .background(
                                color = statusColor(status).copy(alpha = 0.12f),
                                shape = MaterialTheme.shapes.small,
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 12.dp),
            ) {
                Text(
                    text = kotlin.math.abs(days).toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor(status),
                )
                Text(
                    text = if (days < 0) stringResource(R.string.days_passed_short)
                    else stringResource(R.string.days_short),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
