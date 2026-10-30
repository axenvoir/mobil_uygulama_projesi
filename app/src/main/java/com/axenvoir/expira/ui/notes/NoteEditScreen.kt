package com.axenvoir.expira.ui.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axenvoir.expira.R
import com.axenvoir.expira.ui.components.ExpiraEmptyState
import com.axenvoir.expira.ui.components.ExpiraSectionCard
import com.axenvoir.expira.ui.components.expiraTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    onBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    viewModel: NoteEditViewModel = hiltViewModel(),
) {
    val title by viewModel.title.collectAsStateWithLifecycle()
    val content by viewModel.content.collectAsStateWithLifecycle()
    val isPinned by viewModel.isPinned.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val editRecordMissing by viewModel.editRecordMissing.collectAsStateWithLifecycle()
    val loadFailed by viewModel.loadFailed.collectAsStateWithLifecycle()

    val isDirty by viewModel.isDirty.collectAsStateWithLifecycle()
    val suggestedTitle by viewModel.suggestedTitle.collectAsStateWithLifecycle()
    var showUnsavedDialog by remember { mutableStateOf(false) }
    val titleFocusRequester = remember { FocusRequester() }

    val titleOverLimit = title.length > NoteEditViewModel.MAX_TITLE_LENGTH
    val contentOverLimit = content.length > NoteEditViewModel.MAX_CONTENT_LENGTH
    val fieldsEnabled = !isLoading && !editRecordMissing && !loadFailed && !isSaving

    LaunchedEffect(saved) {
        if (saved) onBack()
    }

    LaunchedEffect(isLoading, editRecordMissing) {
        if (!isLoading && !viewModel.isEditMode && !editRecordMissing) {
            titleFocusRequester.requestFocus()
        }
    }

    val confirmBack = {
        if (!isSaving) {
            if (isDirty) showUnsavedDialog = true else onBack()
        }
    }
    BackHandler(enabled = !isSaving) { confirmBack() }

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = expiraTopAppBarColors(),
                title = {
                    Text(
                        text = stringResource(
                            if (viewModel.isEditMode) R.string.edit_note else R.string.add_note,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { confirmBack() },
                        enabled = !isSaving,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onPinnedChange(!isPinned) },
                        enabled = fieldsEnabled,
                    ) {
                        Icon(
                            imageVector = if (isPinned) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = stringResource(
                                if (isPinned) R.string.unpin else R.string.pin,
                            ),
                            tint = if (isPinned) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    IconButton(
                        onClick = viewModel::save,
                        enabled = !isLoading && !loadFailed && !editRecordMissing && !isSaving,
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = stringResource(R.string.save),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.note_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else if (loadFailed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                ExpiraEmptyState(
                    icon = Icons.Filled.Edit,
                    title = stringResource(R.string.note_load_failed_title),
                    subtitle = stringResource(R.string.note_load_failed_message),
                    actionLabel = stringResource(R.string.try_again),
                    onAction = viewModel::retryLoad,
                )
            }
        } else if (editRecordMissing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                ExpiraEmptyState(
                    icon = Icons.Filled.Edit,
                    title = stringResource(R.string.edit_record_missing_title),
                    subtitle = stringResource(R.string.note_edit_record_missing_message),
                    actionLabel = stringResource(R.string.create_new_note),
                    onAction = onNavigateToAdd,
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                ExpiraSectionCard(title = stringResource(R.string.note_basic_info)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = viewModel::onTitleChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(titleFocusRequester),
                        shape = MaterialTheme.shapes.large,
                        label = { Text(stringResource(R.string.note_title)) },
                        placeholder = { Text(stringResource(R.string.note_title_placeholder)) },
                        isError = error is NoteEditError.EmptyTitle || titleOverLimit ||
                            error is NoteEditError.TitleTooLong,
                        supportingText = {
                            Column {
                                if (error is NoteEditError.EmptyTitle) {
                                    Text(stringResource(R.string.error_empty_title))
                                }
                                Text(
                                    text = stringResource(
                                        R.string.note_title_counter,
                                        title.length,
                                        NoteEditViewModel.MAX_TITLE_LENGTH,
                                    ),
                                    color = if (titleOverLimit) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (titleOverLimit || error is NoteEditError.TitleTooLong) {
                                    Text(
                                        text = stringResource(R.string.error_title_too_long),
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        },
                        enabled = fieldsEnabled,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next,
                        ),
                    )
                    if (suggestedTitle.isNotBlank() && title.isBlank() && fieldsEnabled) {
                        androidx.compose.material3.AssistChip(
                            onClick = viewModel::applySuggestedTitle,
                            label = { Text(suggestedTitle) },
                        )
                    }
                }
                ExpiraSectionCard(title = stringResource(R.string.note_content_section)) {
                    OutlinedTextField(
                        value = content,
                        onValueChange = viewModel::onContentChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp, max = 480.dp),
                        shape = MaterialTheme.shapes.large,
                        label = { Text(stringResource(R.string.note_content)) },
                        placeholder = { Text(stringResource(R.string.note_content_placeholder)) },
                        isError = contentOverLimit || error is NoteEditError.ContentTooLong,
                        supportingText = {
                            Column {
                                Text(
                                    text = stringResource(
                                        R.string.note_content_counter,
                                        content.length,
                                        NoteEditViewModel.MAX_CONTENT_LENGTH,
                                    ),
                                    color = if (contentOverLimit) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (contentOverLimit || error is NoteEditError.ContentTooLong) {
                                    Text(
                                        text = stringResource(R.string.error_content_too_long),
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        },
                        enabled = fieldsEnabled,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Default,
                        ),
                    )
                }
                if (error is NoteEditError.Generic) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.error_generic),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                }
            }
        }
    }
}
