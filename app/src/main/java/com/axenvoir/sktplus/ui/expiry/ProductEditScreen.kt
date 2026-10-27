package com.axenvoir.sktplus.ui.expiry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axenvoir.sktplus.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductEditScreen(
    onBack: () -> Unit,
    viewModel: ProductEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showPastDateWarning by remember { mutableStateOf(false) }
    var pendingPastDate by remember { mutableStateOf<java.time.LocalDate?>(null) }
    val genericErrorText = stringResource(R.string.error_generic)

    val isDirty by viewModel.isDirty.collectAsStateWithLifecycle()
    var showUnsavedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(saved) {
        if (saved) onBack()
    }

    val confirmBack = {
        if (isDirty) showUnsavedDialog = true else onBack()
    }
    BackHandler { confirmBack() }

    val emptyNameText = stringResource(R.string.error_empty_name)
    val missingDateText = stringResource(R.string.error_past_date)
    val missingCategoryText = stringResource(R.string.category_name)
    val invalidQuantityText = stringResource(R.string.quantity)

    LaunchedEffect(uiState.error) {
        val message = when (uiState.error) {
            ProductEditError.EmptyName -> emptyNameText
            ProductEditError.MissingExpiryDate -> missingDateText
            ProductEditError.MissingCategory -> missingCategoryText
            ProductEditError.InvalidQuantity -> invalidQuantityText
            ProductEditError.Generic -> genericErrorText
            null -> null
        }
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (viewModel.isEditMode) R.string.edit_product else R.string.add_product
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { confirmBack() }) {
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
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    modifier = Modifier.weight(5f),
                    label = { Text(stringResource(R.string.product_name)) },
                    placeholder = { Text(stringResource(R.string.name_placeholder)) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                    ),
                )
                OutlinedTextField(
                    value = uiState.quantityText,
                    onValueChange = viewModel::onQuantityChange,
                    modifier = Modifier.weight(2f),
                    label = { Text(stringResource(R.string.quantity)) },
                    singleLine = true,
                )
            }
            Text(
                text = stringResource(R.string.category),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CategorySelector(
                uiState = uiState,
                onSelect = viewModel::onCategorySelect,
                onNewCategoryChange = viewModel::onNewCategoryNameChange,
            )
            Text(
                text = stringResource(R.string.storage_location),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "" to R.string.location_other,
                    "fridge" to R.string.location_fridge,
                    "pantry" to R.string.location_pantry,
                    "freezer" to R.string.location_freezer,
                ).forEach { (key, labelRes) ->
                    androidx.compose.material3.FilterChip(
                        selected = uiState.storageLocation == key,
                        onClick = { viewModel.onStorageLocationChange(key) },
                        label = { Text(stringResource(labelRes)) },
                    )
                }
            }
            Text(
                text = stringResource(R.string.expiry_date),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = uiState.expiryDate?.format(
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                    ) ?: stringResource(R.string.pick_date)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = viewModel::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) { Text(stringResource(R.string.save)) }
        }
    }

    val pastDateToConfirm = pendingPastDate
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text(stringResource(R.string.unsaved_title)) },
            text = { Text(stringResource(R.string.unsaved_message)) },
            confirmButton = {
                TextButton(onClick = { showUnsavedDialog = false; onBack() }) {
                    Text(stringResource(R.string.discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            },
        )
    }

    if (showPastDateWarning && pastDateToConfirm != null) {
        AlertDialog(
            onDismissRequest = {
                showPastDateWarning = false
                pendingPastDate = null
            },
            title = { Text(stringResource(R.string.past_date_title)) },
            text = { Text(stringResource(R.string.past_date_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onExpiryDateChange(pastDateToConfirm)
                        showPastDateWarning = false
                        pendingPastDate = null
                    },
                ) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPastDateWarning = false
                        pendingPastDate = null
                    },
                ) { Text(stringResource(R.string.no)) }
            },
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.expiryDate?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            if (date.isBefore(java.time.LocalDate.now())) {
                                pendingPastDate = date
                                showPastDateWarning = true
                            } else {
                                viewModel.onExpiryDateChange(date)
                            }
                        }
                        showDatePicker = false
                    },
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySelector(
    uiState: ProductEditUiState,
    onSelect: (Long) -> Unit,
    onNewCategoryChange: (String) -> Unit,
) {
    Column {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            uiState.categories.forEach { category ->
                FilterChip(
                    selected = uiState.selectedCategoryId == category.id,
                    onClick = { onSelect(category.id) },
                    label = { Text(category.name) },
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.newCategoryName,
            onValueChange = onNewCategoryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.new_category)) },
            singleLine = true,
        )
    }
}
