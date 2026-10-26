package com.expira.app.tr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expira.app.tr.data.model.Category
import com.expira.app.tr.data.model.ExpiryStatus
import com.expira.app.tr.data.model.Product
import com.expira.app.tr.ui.components.AddEditProductDialog
import com.expira.app.tr.ui.theme.*
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
fun HomeScreen(
    products: List<Product>,
    isTurkish: Boolean,
    onAddProduct: (Product) -> Unit,
    onUpdateProduct: (Product) -> Unit,
    onDeleteProduct: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<ExpiryStatus?>(null) }

    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    // Statistics Calculation
    val stats = remember(products) {
        val fresh = products.count { it.status == ExpiryStatus.FRESH }
        val approaching = products.count { it.status == ExpiryStatus.APPROACHING }
        val expired = products.count { it.status == ExpiryStatus.EXPIRED }
        Triple(products.size, fresh, Pair(approaching, expired))
    }

    val totalCount = stats.first
    val freshCount = stats.second
    val approachingCount = stats.third.first
    val expiredCount = stats.third.second

    // Filtered Products
    val filteredProducts = remember(products, searchQuery, selectedCategory, selectedStatusFilter) {
        products.filter { product ->
            // Search Query
            val matchesQuery = searchQuery.isBlank() ||
                    product.name.contains(searchQuery, ignoreCase = true) ||
                    product.note.contains(searchQuery, ignoreCase = true)

            // Category Filter
            val matchesCat = selectedCategory == null || product.category == selectedCategory

            // Status Filter
            val matchesStatus = selectedStatusFilter == null || product.status == selectedStatusFilter

            matchesQuery && matchesCat && matchesStatus
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = if (isTurkish) "Toplam" else "Total",
                    count = totalCount,
                    isSelected = selectedStatusFilter == null,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedStatusFilter = null }
                )
                StatCard(
                    title = if (isTurkish) "Taze" else "Fresh",
                    count = freshCount,
                    isSelected = selectedStatusFilter == ExpiryStatus.FRESH,
                    color = FreshGreen,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedStatusFilter = if (selectedStatusFilter == ExpiryStatus.FRESH) null else ExpiryStatus.FRESH
                    }
                )
                StatCard(
                    title = if (isTurkish) "Yaklaşan" else "Soon",
                    count = approachingCount,
                    isSelected = selectedStatusFilter == ExpiryStatus.APPROACHING,
                    color = WarningAmber,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedStatusFilter = if (selectedStatusFilter == ExpiryStatus.APPROACHING) null else ExpiryStatus.APPROACHING
                    }
                )
                StatCard(
                    title = if (isTurkish) "Dolmuş" else "Expired",
                    count = expiredCount,
                    isSelected = selectedStatusFilter == ExpiryStatus.EXPIRED,
                    color = DangerRed,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedStatusFilter = if (selectedStatusFilter == ExpiryStatus.EXPIRED) null else ExpiryStatus.EXPIRED
                    }
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isTurkish) "Ürün ara..." else "Search items...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text(if (isTurkish) "Tümü" else "All") }
                )
                Category.entries.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = if (selectedCategory == cat) null else cat
                        },
                        label = { Text("${cat.icon} ${cat.getDisplayName(isTurkish)}") }
                    )
                }
            }

            // Product List
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📦", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isTurkish) "Kayıtlı ürün bulunamadı" else "No items found",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isTurkish) "Sağ alttaki '+' butonuna basarak ilk ürününüzü ekleyin." else "Tap the '+' button below to add your first item.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            isTurkish = isTurkish,
                            onEdit = { productToEdit = product },
                            onDelete = { productToDelete = product }
                        )
                    }
                }
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        AddEditProductDialog(
            isTurkish = isTurkish,
            onDismiss = { showAddDialog = false },
            onSave = {
                onAddProduct(it)
                showAddDialog = false
            }
        )
    }

    // Edit Dialog
    productToEdit?.let { item ->
        AddEditProductDialog(
            initialProduct = item,
            isTurkish = isTurkish,
            onDismiss = { productToEdit = null },
            onSave = {
                onUpdateProduct(it)
                productToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    productToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text(if (isTurkish) "Ürünü Sil" else "Delete Item") },
            text = {
                Text(
                    if (isTurkish)
                        "'${item.name}' ürününü silmek istediğinize emin misiniz?"
                    else
                        "Are you sure you want to delete '${item.name}'?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProduct(item.id)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text(if (isTurkish) "Sil" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text(if (isTurkish) "İptal" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun ProductItemCard(
    product: Product,
    isTurkish: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val days = product.daysLeft

    val badgeBg = when (product.status) {
        ExpiryStatus.EXPIRED -> DangerRedBg
        ExpiryStatus.APPROACHING -> WarningAmberBg
        ExpiryStatus.FRESH -> FreshGreenBg
    }
    val badgeColor = when (product.status) {
        ExpiryStatus.EXPIRED -> DangerRed
        ExpiryStatus.APPROACHING -> WarningAmber
        ExpiryStatus.FRESH -> FreshGreen
    }
    val badgeText = when {
        days < 0 -> if (isTurkish) "${abs(days)} gün geçti" else "${abs(days)}d ago"
        days == 0L -> if (isTurkish) "Bugün doluyor!" else "Today!"
        days <= 3L -> if (isTurkish) "$days gün kaldı" else "${days}d left"
        else -> if (isTurkish) "$days gün kaldı" else "${days}d left"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = product.category.icon, fontSize = 24.sp)
                    Column {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${product.category.getDisplayName(isTurkish)} • ${product.quantity}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (isTurkish) "SKT" else "EXP"}: ${product.expiryDate.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = DangerRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (product.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "💬 ${product.note}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        }
    }
}
