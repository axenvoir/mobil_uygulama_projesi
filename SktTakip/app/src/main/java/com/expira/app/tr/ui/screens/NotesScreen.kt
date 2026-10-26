package com.expira.app.tr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expira.app.tr.data.model.Note
import com.expira.app.tr.ui.components.AddEditNoteDialog
import com.expira.app.tr.ui.theme.DangerRed
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotesScreen(
    notes: List<Note>,
    isTurkish: Boolean,
    onAddNote: (Note) -> Unit,
    onUpdateNote: (Note) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onTogglePin: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    val filteredNotes = remember(notes, searchQuery) {
        notes.filter {
            searchQuery.isBlank() ||
                    it.title.contains(searchQuery, ignoreCase = true) ||
                    it.content.contains(searchQuery, ignoreCase = true)
        }
    }

    val pinnedNotes = remember(filteredNotes) { filteredNotes.filter { it.isPinned } }
    val otherNotes = remember(filteredNotes) { filteredNotes.filter { !it.isPinned } }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isTurkish) "Notlarda ara..." else "Search notes...") },
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📝", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isTurkish) "Henüz not kaydedilmemiş" else "No notes found",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isTurkish) "Alışveriş listenizi veya kiler notlarınızı eklemek için '+' butonuna dokunun."
                            else "Tap '+' below to jot down grocery lists or pantry notes.",
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
                    if (pinnedNotes.isNotEmpty()) {
                        item {
                            Text(
                                text = if (isTurkish) "📌 Sabitlenen Notlar" else "📌 Pinned Notes",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(pinnedNotes, key = { "pinned_${it.id}" }) { note ->
                            NoteCardItem(
                                note = note,
                                isTurkish = isTurkish,
                                onEdit = { noteToEdit = note },
                                onDelete = { noteToDelete = note },
                                onTogglePin = { onTogglePin(note.id) }
                            )
                        }
                    }

                    if (otherNotes.isNotEmpty()) {
                        if (pinnedNotes.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isTurkish) "Tüm Notlar" else "All Notes",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        items(otherNotes, key = { "other_${it.id}" }) { note ->
                            NoteCardItem(
                                note = note,
                                isTurkish = isTurkish,
                                onEdit = { noteToEdit = note },
                                onDelete = { noteToDelete = note },
                                onTogglePin = { onTogglePin(note.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditNoteDialog(
            isTurkish = isTurkish,
            onDismiss = { showAddDialog = false },
            onSave = {
                onAddNote(it)
                showAddDialog = false
            }
        )
    }

    noteToEdit?.let { item ->
        AddEditNoteDialog(
            initialNote = item,
            isTurkish = isTurkish,
            onDismiss = { noteToEdit = null },
            onSave = {
                onUpdateNote(it)
                noteToEdit = null
            }
        )
    }

    noteToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text(if (isTurkish) "Notu Sil" else "Delete Note") },
            text = {
                Text(
                    if (isTurkish)
                        "'${item.title}' notunu silmek istediğinize emin misiniz?"
                    else
                        "Are you sure you want to delete note '${item.title}'?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteNote(item.id)
                        noteToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text(if (isTurkish) "Sil" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text(if (isTurkish) "İptal" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun NoteCardItem(
    note: Note,
    isTurkish: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit
) {
    val dateStr = remember(note.updatedAt) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(note.updatedAt))
    }
    val noteColor = remember(note.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(note.colorHex))
        } catch (e: Exception) {
            Color(0xFF267968)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.5.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left color indicator accent bar
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(noteColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onTogglePin, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Default.OutlinedFlag,
                                contentDescription = "Pin",
                                tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (note.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        maxLines = 5
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
