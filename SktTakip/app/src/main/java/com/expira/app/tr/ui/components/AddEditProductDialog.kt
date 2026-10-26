package com.expira.app.tr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expira.app.tr.data.model.Category
import com.expira.app.tr.data.model.Note
import com.expira.app.tr.data.model.Product
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddEditProductDialog(
    initialProduct: Product? = null,
    isTurkish: Boolean = true,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(initialProduct?.category ?: Category.DAIRY) }
    var expiryDate by remember { mutableStateOf(initialProduct?.expiryDate ?: LocalDate.now().plusDays(7)) }
    var quantity by remember { mutableStateOf(initialProduct?.quantity ?: "1 Adet") }
    var note by remember { mutableStateOf(initialProduct?.note ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialProduct != null) {
                    if (isTurkish) "Ürünü Düzenle" else "Edit Item"
                } else {
                    if (isTurkish) "Yeni Ürün Ekle" else "Add New Item"
                },
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isTurkish) "Ürün Adı *" else "Item Name *") },
                    placeholder = { Text(if (isTurkish) "Örn: Süt, Yoğurt, Peynir" else "e.g. Milk, Cheese") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = if (isTurkish) "Kategori" else "Category",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Category.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text("${cat.icon} ${cat.getDisplayName(isTurkish)}") }
                        )
                    }
                }

                Text(
                    text = if (isTurkish) "Son Kullanma Tarihi" else "Expiry Date",
                    style = MaterialTheme.typography.labelMedium
                )
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = expiryDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Select Date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Quick Increment helper chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SuggestionChip(
                        onClick = { expiryDate = LocalDate.now().plusDays(3) },
                        label = { Text("+3 Gün") }
                    )
                    SuggestionChip(
                        onClick = { expiryDate = LocalDate.now().plusDays(7) },
                        label = { Text("+1 Hafta") }
                    )
                    SuggestionChip(
                        onClick = { expiryDate = LocalDate.now().plusMonths(1) },
                        label = { Text("+1 Ay") }
                    )
                    SuggestionChip(
                        onClick = { expiryDate = LocalDate.now().plusMonths(6) },
                        label = { Text("+6 Ay") }
                    )
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text(if (isTurkish) "Miktar / Adet" else "Quantity") },
                    placeholder = { Text(if (isTurkish) "Örn: 2 kutu, 500g" else "e.g. 2 packs") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isTurkish) "Not (İsteğe Bağlı)" else "Note (Optional)") },
                    placeholder = { Text(if (isTurkish) "Dolap kapağı, dondurucu..." else "Shelf location...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val product = initialProduct?.copy(
                            name = name.trim(),
                            category = selectedCategory,
                            expiryDate = expiryDate,
                            quantity = quantity.trim(),
                            note = note.trim()
                        ) ?: Product(
                            name = name.trim(),
                            category = selectedCategory,
                            expiryDate = expiryDate,
                            quantity = quantity.trim(),
                            note = note.trim()
                        )
                        onSave(product)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (isTurkish) "Kaydet" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isTurkish) "İptal" else "Cancel")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = expiryDate.toEpochDay() * 24 * 60 * 60 * 1000
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        expiryDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                    }
                    showDatePicker = false
                }) {
                    Text(if (isTurkish) "Tamam" else "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(if (isTurkish) "İptal" else "Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun AddEditNoteDialog(
    initialNote: Note? = null,
    isTurkish: Boolean = true,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit
) {
    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    var content by remember { mutableStateOf(initialNote?.content ?: "") }
    var selectedColor by remember { mutableStateOf(initialNote?.colorHex ?: "#267968") }
    var isPinned by remember { mutableStateOf(initialNote?.isPinned ?: false) }

    val colorOptions = listOf("#267968", "#3B82F6", "#8B5CF6", "#EF4444", "#F59E0B", "#10B981")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialNote != null) {
                    if (isTurkish) "Notu Düzenle" else "Edit Note"
                } else {
                    if (isTurkish) "Yeni Not Yaz" else "New Note"
                },
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isTurkish) "Not Başlığı *" else "Note Title *") },
                    placeholder = { Text(if (isTurkish) "Örn: Haftalık Alışveriş" else "e.g. Grocery List") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(if (isTurkish) "Not İçeriği" else "Content") },
                    placeholder = { Text(if (isTurkish) "• Maden suyu\n• Zeytinyağı" else "• Item 1\n• Item 2") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    maxLines = 8
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isTurkish) "Başa Sabitle" else "Pin to Top",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(checked = isPinned, onCheckedChange = { isPinned = it })
                }

                Text(
                    text = if (isTurkish) "Renk Etiketi" else "Color Tag",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = hex }
                                .then(
                                    if (selectedColor == hex) Modifier.border(3.dp, Color.Black, CircleShape)
                                    else Modifier
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val noteItem = initialNote?.copy(
                            title = title.trim(),
                            content = content.trim(),
                            colorHex = selectedColor,
                            isPinned = isPinned,
                            updatedAt = System.currentTimeMillis()
                        ) ?: Note(
                            title = title.trim(),
                            content = content.trim(),
                            colorHex = selectedColor,
                            isPinned = isPinned
                        )
                        onSave(noteItem)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text(if (isTurkish) "Kaydet" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isTurkish) "İptal" else "Cancel")
            }
        }
    )
}
