package com.axenvoir.expira.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axenvoir.expira.R
import com.axenvoir.expira.domain.model.Note
import com.axenvoir.expira.domain.util.systemTodayFlow
import com.axenvoir.expira.notification.SystemTimeChangeEvents
import com.axenvoir.expira.ui.components.ExpiraEmptyState
import com.axenvoir.expira.ui.components.expiraChipColors
import com.axenvoir.expira.ui.main.SettingsIconButton
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

// Keep the existing ViewModel-owned String query; do not create a second TextFieldState for presentation.
@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    onAddNote: () -> Unit,
    onEditNote: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: NoteListViewModel = hiltViewModel(),
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val referenceDate by systemTodayFlow(SystemTimeChangeEvents.changes)
        .collectAsStateWithLifecycle(initialValue = LocalDate.now())
    val query by viewModel.query.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val loadFailed by viewModel.loadFailed.collectAsStateWithLifecycle()
    val hasAnyNotes by viewModel.hasAnyNotes.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var notePendingDelete by remember { mutableStateOf<Note?>(null) }
    val errorText = stringResource(R.string.error_generic)
    val listState = rememberLazyListState()

    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val undoText = stringResource(R.string.undo)

    LaunchedEffect(event) {
        if (event is NoteListEvent.Error) {
            snackbarHostState.showSnackbar(errorText)
            viewModel.consumeEvent()
        }
    }

    val scrollBehavior = androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            androidx.compose.material3.MediumTopAppBar(
                colors = com.axenvoir.expira.ui.components.expiraTopAppBarColors(),
                title = {
                    Text(
                        text = stringResource(R.string.notes_list_title),
                        maxLines = 1,
                    )
                },
                actions = { SettingsIconButton(onClick = onOpenSettings) },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!isLoading && !loadFailed) {
                FloatingActionButton(
                    onClick = onAddNote,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_note))
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
            Box(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxSize()
                    .align(Alignment.TopCenter),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    SearchBar(
                        query = query,
                        onQueryChange = viewModel::onQueryChange,
                        onSearch = viewModel::onQueryChange,
                        active = false,
                        onActiveChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text(stringResource(R.string.search_notes)) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                            )
                        },
                        trailingIcon = if (query.isNotBlank()) {
                            {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = stringResource(R.string.clear_notes_search),
                                    )
                                }
                            }
                        } else null,
                        shape = MaterialTheme.shapes.extraLarge,
                    ) { }
                    if (!isLoading && !loadFailed && notes.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ) {
                                Text(
                                    text = pluralStringResource(R.plurals.notes_count, notes.size, notes.size),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .selectableGroup()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = filter == NoteFilter.ALL,
                            onClick = { viewModel.setFilter(NoteFilter.ALL) },
                            modifier = Modifier.heightIn(min = 48.dp),
                            label = { Text(stringResource(R.string.notes_filter_all)) },
                            colors = expiraChipColors(),
                        )
                        FilterChip(
                            selected = filter == NoteFilter.PINNED,
                            onClick = { viewModel.setFilter(NoteFilter.PINNED) },
                            modifier = Modifier.heightIn(min = 48.dp),
                            label = { Text(stringResource(R.string.notes_filter_pinned)) },
                            colors = expiraChipColors(),
                        )
                        FilterChip(
                            selected = filter == NoteFilter.RECENT,
                            onClick = { viewModel.setFilter(NoteFilter.RECENT) },
                            modifier = Modifier.heightIn(min = 48.dp),
                            label = { Text(stringResource(R.string.notes_filter_recent)) },
                            colors = expiraChipColors(),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (loadFailed) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            ExpiraEmptyState(
                                icon = Icons.Filled.Edit,
                                title = stringResource(R.string.notes_load_failed_title),
                                subtitle = stringResource(R.string.notes_load_failed_message),
                                actionLabel = stringResource(R.string.try_again),
                                onAction = viewModel::retryLoad,
                            )
                        }
                    } else if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(R.string.notes_loading),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else if (notes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            when {
                                query.isNotBlank() -> ExpiraEmptyState(
                                    icon = Icons.Filled.Search,
                                    title = stringResource(R.string.empty_notes_search),
                                    subtitle = stringResource(R.string.empty_notes_search_subtitle),
                                    actionLabel = stringResource(R.string.clear_notes_search),
                                    onAction = { viewModel.onQueryChange("") },
                                )

                                !hasAnyNotes -> ExpiraEmptyState(
                                    icon = Icons.Filled.Edit,
                                    title = stringResource(R.string.empty_notes_title),
                                    subtitle = stringResource(R.string.empty_notes),
                                    actionLabel = stringResource(R.string.add_note),
                                    onAction = onAddNote,
                                )

                                filter == NoteFilter.PINNED -> ExpiraEmptyState(
                                    icon = Icons.Filled.Star,
                                    title = stringResource(R.string.empty_pinned_notes_title),
                                    subtitle = stringResource(R.string.empty_pinned_notes),
                                )

                                filter == NoteFilter.RECENT -> ExpiraEmptyState(
                                    icon = Icons.Filled.Edit,
                                    title = stringResource(R.string.empty_recent_notes_title),
                                    subtitle = stringResource(R.string.empty_recent_notes),
                                )

                                else -> ExpiraEmptyState(
                                    icon = Icons.Filled.Edit,
                                    title = stringResource(R.string.empty_notes_title),
                                    subtitle = stringResource(R.string.empty_notes),
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 96.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(notes, key = { it.id }, contentType = { "note" }) { note ->
                                NoteCard(
                                    note = note,
                                    referenceDate = referenceDate,
                                    onClick = { onEditNote(note.id) },
                                    onTogglePin = { viewModel.togglePinned(note) },
                                    onDelete = { notePendingDelete = note },
                                )
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
            text = {
                Text(
                    text = note.title,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            },
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
                ) { Text(stringResource(R.string.delete_action)) }
            },
            dismissButton = {
                TextButton(onClick = { notePendingDelete = null }) {
                    Text(stringResource(R.string.cancel_action))
                }
            },
        )
    }
}

@Composable
private fun relativeUpdatedAt(timestamp: Long, referenceDate: LocalDate): String {
    val updatedDate = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val days = ChronoUnit.DAYS.between(updatedDate, referenceDate)
    return when {
        days <= 0L -> stringResource(R.string.relative_today)
        days == 1L -> stringResource(R.string.relative_yesterday)
        else -> stringResource(R.string.relative_days_ago, days)
    }
}

@Composable
private fun NoteCard(
    note: Note,
    referenceDate: LocalDate,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    ElevatedCard(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (note.isPinned) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp, pressedElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(onClick = onTogglePin) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = stringResource(
                            if (note.isPinned) R.string.unpin else R.string.pin,
                        ),
                        tint = if (note.isPinned) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.note_more_actions),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete)) },
                            leadingIcon = {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                        )
                    }
                }
            }
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.content.trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = relativeUpdatedAt(note.updatedAt, referenceDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
