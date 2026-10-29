package com.axenvoir.expira.ui.notes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
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
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axenvoir.expira.R
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
    val editRecordMissing by viewModel.editRecordMissing.collectAsStateWithLifecycle()

    val isDirty by viewModel.isDirty.collectAsStateWithLifecycle()
    val suggestedTitle by viewModel.suggestedTitle.collectAsStateWithLifecycle()
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showEditRecordMissingDialog by remember { mutableStateOf(false) }

    LaunchedEffect(saved) {
        if (saved) onBack()
    }

    LaunchedEffect(editRecordMissing) {
        if (editRecordMissing) showEditRecordMissingDialog = true
    }

    val confirmBack = {
        if (isDirty) showUnsavedDialog = true else onBack()
    }
    BackHandler { confirmBack() }

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

    if (showEditRecordMissingDialog) {
        AlertDialog(
            onDismissRequest = {
                showEditRecordMissingDialog = false
                viewModel.consumeEditRecordMissing()
            },
            title = { Text(stringResource(R.string.edit_record_missing_title)) },
            text = { Text(stringResource(R.string.note_edit_record_missing_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEditRecordMissingDialog = false
                        viewModel.consumeEditRecordMissing()
                        onNavigateToAdd()
                    },
                ) { Text(stringResource(R.string.create_new_note)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditRecordMissingDialog = false
                    viewModel.consumeEditRecordMissing()
                }) { Text(stringResource(R.string.cancel_action)) }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = expiraTopAppBarColors(),
                title = {
                    Text(
                        text = stringResource(
                            if (viewModel.isEditMode) R.string.edit_note else R.string.add_note
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
                actions = {
                    IconButton(onClick = { viewModel.onPinnedChange(!isPinned) }) {
                        Icon(
                            imageVector = if (isPinned) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = stringResource(
                                if (isPinned) R.string.unpin else R.string.pin
                            ),
                            tint = if (isPinned) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    TextButton(
                        onClick = viewModel::save,
                        enabled = title.isNotBlank() && !isSaving,
                    ) { Text(stringResource(R.string.save)) }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.note_title)) },
                placeholder = { Text(stringResource(R.string.note_title_placeholder)) },
                isError = error is NoteEditError.EmptyTitle,
                supportingText = {
                    if (error is NoteEditError.EmptyTitle) {
                        Text(stringResource(R.string.error_empty_title))
                    } else {
                        Text(stringResource(R.string.note_title_counter, title.length, NoteEditViewModel.MAX_TITLE_LENGTH))
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next,
                ),
            )
            if (suggestedTitle.isNotBlank() && title.isBlank()) {
                androidx.compose.material3.AssistChip(
                    onClick = viewModel::applySuggestedTitle,
                    label = { Text(suggestedTitle) },
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = content,
                onValueChange = viewModel::onContentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 480.dp),
                label = { Text(stringResource(R.string.note_content)) },
                placeholder = { Text(stringResource(R.string.note_content_placeholder)) },
                supportingText = {
                    Text(stringResource(R.string.note_content_counter, content.length, NoteEditViewModel.MAX_CONTENT_LENGTH))
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default,
                ),
            )
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
