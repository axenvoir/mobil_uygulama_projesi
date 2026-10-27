package com.axenvoir.sktplus.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axenvoir.sktplus.R
import com.axenvoir.sktplus.domain.model.Note
import com.axenvoir.sktplus.ui.components.EmptyState
import com.axenvoir.sktplus.ui.main.SettingsIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    onAddNote: () -> Unit,
    onEditNote: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: NoteListViewModel = hiltViewModel(),
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var notePendingDelete by remember { mutableStateOf<Note?>(null) }
    val errorText = stringResource(R.string.error_generic)

    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val undoText = stringResource(R.string.undo)

    LaunchedEffect(event) {
        if (event is NoteListEvent.Error) {
            snackbarHostState.showSnackbar(errorText)
            viewModel.consumeEvent()
        }
    }

    val scrollBehavior = androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            androidx.compose.material3.LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.notes_list_title),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                },
                actions = { SettingsIconButton(onClick = onOpenSettings) },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNote) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_note))
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
                    OutlinedTextField(
                        value = query,
                        onValueChange = viewModel::onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text(stringResource(R.string.search_notes)) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.extraLarge,
                    )
                    if (notes.isEmpty()) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyState(
                                icon = Icons.Filled.Edit,
                                title = stringResource(R.string.empty_notes_title),
                                subtitle = stringResource(R.string.empty_notes),
                                actionLabel = stringResource(R.string.add_note),
                                onAction = onAddNote,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(notes, key = { it.id }, contentType = { "note" }) { note ->
                                val dismissState = androidx.compose.material3.rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value ->
                                        if (value == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart) {
                                            notePendingDelete = note
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
                                        NoteCard(
                                            note = note,
                                            onClick = { onEditNote(note.id) },
                                            onTogglePin = { viewModel.togglePinned(note) },
                                            onDelete = { notePendingDelete = note },
                                        )
                                    }
                                }
                                androidx.compose.runtime.LaunchedEffect(notePendingDelete) {
                                    if (notePendingDelete == null &&
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

    notePendingDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { notePendingDelete = null },
            title = { Text(stringResource(R.string.delete_confirm)) },
            text = { Text(note.title) },
            confirmButton = {
                TextButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        viewModel.deleteNote(note) {
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = note.title,
                                    actionLabel = undoText,
                                    duration = androidx.compose.material3.SnackbarDuration.Long,
                                )
                                if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                    viewModel.restoreNote(note)
                                }
                            }
                        }
                        notePendingDelete = null
                    },
                ) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = {
                TextButton(onClick = { notePendingDelete = null }) {
                    Text(stringResource(R.string.no))
                }
            },
        )
    }
}

@Composable
private fun relativeUpdatedAt(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val days = java.time.temporal.ChronoUnit.DAYS.between(
        java.time.Instant.ofEpochMilli(timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
        java.time.Instant.ofEpochMilli(now).atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
    )
    return when {
        days <= 0L -> stringResource(R.string.relative_today)
        days == 1L -> stringResource(R.string.relative_yesterday)
        else -> stringResource(R.string.relative_days_ago, days)
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (note.isPinned) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(onClick = onTogglePin) {
                    if (note.isPinned) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = stringResource(R.string.pin),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        Icon(
                            Icons.Outlined.FavoriteBorder,
                            contentDescription = stringResource(R.string.pin),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = relativeUpdatedAt(note.updatedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
