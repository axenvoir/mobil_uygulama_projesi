package com.axenvoir.expira.ui.expiry

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
    import androidx.compose.foundation.rememberScrollState
    import androidx.compose.foundation.verticalScroll
    import androidx.compose.foundation.layout.widthIn
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axenvoir.expira.R
import com.axenvoir.expira.domain.util.systemTodayFlow
import com.axenvoir.expira.notification.SystemTimeChangeEvents
import com.axenvoir.expira.ui.components.ExpiraEmptyState
import com.axenvoir.expira.ui.components.ExpiraSectionCard
import com.axenvoir.expira.ui.components.ExpiraSelectChip
import com.axenvoir.expira.ui.components.expiraTopAppBarColors
import com.axenvoir.expira.ui.components.rememberLocalizedDateFormatter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductEditScreen(
    onBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    viewModel: ProductEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val isDirty by viewModel.isDirty.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val referenceDate by systemTodayFlow(SystemTimeChangeEvents.changes)
        .collectAsStateWithLifecycle(initialValue = LocalDate.now())
    val mediumDateFormatter = rememberLocalizedDateFormatter(FormatStyle.MEDIUM)
    var showDatePicker by remember { mutableStateOf(false) }
    var showPastDateWarning by remember { mutableStateOf(false) }
    var pendingPastDate by remember { mutableStateOf<LocalDate?>(null) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val genericErrorText = stringResource(R.string.error_generic)
    val emptyNameText = stringResource(R.string.error_empty_name)
    val missingDateText = stringResource(R.string.error_missing_expiry_date)
    val missingCategoryText = stringResource(R.string.error_missing_category)
    val invalidQuantityText = stringResource(R.string.error_invalid_quantity)

    LaunchedEffect(saved) {
        if (saved) onBack()
    }
    LaunchedEffect(uiState.error, uiState.loadFailed) {
        if (uiState.error == ProductEditError.Generic && !uiState.loadFailed) {
            snackbarHostState.showSnackbar(genericErrorText)
            viewModel.consumeError()
        }
    }

    val confirmBack = {
        if (isDirty) {
            showUnsavedDialog = true
        } else {
            onBack()
        }
    }
    BackHandler { confirmBack() }

    val canEdit = uiState.isHydrated && !uiState.editRecordMissing && !uiState.loadFailed
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = expiraTopAppBarColors(),
                title = {
                    Text(
                        text = stringResource(
                            if (viewModel.isEditMode) R.string.edit_product else R.string.add_product,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = confirmBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 3.dp,
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = viewModel::save,
                        modifier = Modifier
                            .widthIn(max = 640.dp)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .heightIn(min = 52.dp),
                        enabled = canEdit && !isSaving,
                        shape = MaterialTheme.shapes.large,
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                            )
                        } else {
                            Text(stringResource(R.string.save), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        when {
            !uiState.isHydrated && !uiState.loadFailed -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.editRecordMissing -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    ExpiraEmptyState(
                        icon = Icons.Filled.DateRange,
                        title = stringResource(R.string.edit_record_missing_title),
                        subtitle = stringResource(R.string.product_edit_record_missing_message),
                        actionLabel = stringResource(R.string.create_new_product),
                        onAction = onNavigateToAdd,
                    )
                }
            }
            uiState.loadFailed -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    ExpiraEmptyState(
                        icon = Icons.Filled.DateRange,
                        title = stringResource(R.string.product_load_failed_title),
                        subtitle = stringResource(R.string.product_load_failed_message),
                        actionLabel = stringResource(R.string.try_again),
                        onAction = viewModel::retryLoad,
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 720.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                    ExpiraSectionCard(title = stringResource(R.string.product_basic_info)) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = uiState.name,
                                onValueChange = viewModel::onNameChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.product_name)) },
                                placeholder = { Text(stringResource(R.string.name_placeholder)) },
                                shape = MaterialTheme.shapes.large,
                                isError = uiState.error is ProductEditError.EmptyName,
                                supportingText = if (uiState.error is ProductEditError.EmptyName) {
                                    { Text(emptyNameText) }
                                } else {
                                    null
                                },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    imeAction = ImeAction.Next,
                                ),
                            )
                            OutlinedTextField(
                                value = uiState.quantityText,
                                onValueChange = viewModel::onQuantityChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.quantity)) },
                                shape = MaterialTheme.shapes.large,
                                isError = uiState.error is ProductEditError.InvalidQuantity,
                                supportingText = if (uiState.error is ProductEditError.InvalidQuantity) {
                                    { Text(invalidQuantityText) }
                                } else {
                                    null
                                },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next,
                                ),
                            )
                        }
                    }

                    ExpiraSectionCard(title = stringResource(R.string.category)) {
                        Text(
                            text = stringResource(R.string.category_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            uiState.categories.forEach { category ->
                                ExpiraSelectChip(
                                    selected = uiState.selectedCategoryId == category.id,
                                    onClick = { viewModel.onCategorySelect(category.id) },
                                    label = category.name,
                                )
                            }
                        }
                        OutlinedTextField(
                            value = uiState.newCategoryName,
                            onValueChange = viewModel::onNewCategoryNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.new_category)) },
                            shape = MaterialTheme.shapes.large,
                            isError = uiState.error is ProductEditError.MissingCategory,
                            supportingText = if (uiState.error is ProductEditError.MissingCategory) {
                                { Text(missingCategoryText) }
                            } else {
                                null
                            },
                            singleLine = true,
                        )
                        }
                    }

                    ExpiraSectionCard(title = stringResource(R.string.storage_location)) {
                        Text(
                            text = stringResource(R.string.storage_location_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(
                                "fridge" to R.string.location_fridge,
                                "pantry" to R.string.location_pantry,
                                "freezer" to R.string.location_freezer,
                                "other" to R.string.location_other,
                            ).forEach { (key, labelRes) ->
                                ExpiraSelectChip(
                                    selected = uiState.storageLocation == key,
                                    onClick = { viewModel.onStorageLocationChange(key) },
                                    label = stringResource(labelRes),
                                )
                            }
                        }
                    }

                    ExpiraSectionCard(title = stringResource(R.string.expiry_date)) {
                        Text(
                            text = stringResource(R.string.expiry_date_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(
                                referenceDate.plusDays(1) to R.string.quick_date_tomorrow,
                                referenceDate.plusDays(3) to R.string.quick_date_three_days,
                                referenceDate.plusWeeks(1) to R.string.quick_date_week,
                                referenceDate.plusMonths(1) to R.string.quick_date_month,
                            ).forEach { (date, labelRes) ->
                                ExpiraSelectChip(
                                    selected = uiState.expiryDate == date,
                                    onClick = { viewModel.onExpiryDateChange(date) },
                                    label = stringResource(labelRes),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.size(4.dp))
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = stringResource(R.string.pick_date),
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = uiState.expiryDate?.format(mediumDateFormatter)
                                    ?: stringResource(R.string.pick_date),
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (uiState.error is ProductEditError.MissingExpiryDate) {
                            Text(
                                text = missingDateText,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    }
                }
            }
        }
    }

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
                    Text(stringResource(R.string.keep_editing))
                }
            },
        )
    }

    val pastDateToConfirm = pendingPastDate
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
                ) {
                    Text(stringResource(R.string.use_date))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPastDateWarning = false
                        pendingPastDate = null
                    },
                ) {
                    Text(stringResource(R.string.cancel_action))
                }
            },
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.expiryDate
                ?.atStartOfDay(ZoneOffset.UTC)
                ?.toInstant()
                ?.toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            if (date.isBefore(referenceDate)) {
                                pendingPastDate = date
                                showPastDateWarning = true
                            } else {
                                viewModel.onExpiryDateChange(date)
                            }
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.use_date))
                }
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
