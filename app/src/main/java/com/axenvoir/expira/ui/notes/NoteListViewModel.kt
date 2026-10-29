package com.axenvoir.expira.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axenvoir.expira.domain.model.Note
import com.axenvoir.expira.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
) : ViewModel() {

    private val _events = MutableStateFlow<NoteListEvent?>(null)
    val events: StateFlow<NoteListEvent?> = _events.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    val query: StateFlow<String> = searchQuery.asStateFlow()

    private val selectedFilter = MutableStateFlow(NoteFilter.ALL)
    val filter: StateFlow<NoteFilter> = selectedFilter.asStateFlow()

    val notes: StateFlow<List<Note>> = combine(
        noteRepository.observeAll(),
        searchQuery,
        selectedFilter,
    ) { allNotes, query, filter ->
        val filteredByType = when (filter) {
            NoteFilter.ALL -> allNotes
            NoteFilter.PINNED -> allNotes.filter { it.isPinned }
            NoteFilter.RECENT -> allNotes.sortedByDescending { it.updatedAt }.take(20)
        }
        val filtered = if (query.isBlank()) filteredByType
        else filteredByType.filter { note ->
            note.title.contains(query, ignoreCase = true) ||
                note.content.contains(query, ignoreCase = true)
        }
        filtered.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt })
    }.distinctUntilChanged().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun onQueryChange(query: String) {
        searchQuery.value = query
    }

    fun setFilter(filter: NoteFilter) {
        selectedFilter.value = filter
    }

    fun togglePinned(note: Note) {
        viewModelScope.launch {
            runCatching { noteRepository.setPinned(id = note.id, isPinned = !note.isPinned) }
                .onFailure { if (it is kotlinx.coroutines.CancellationException) throw it else _events.value = NoteListEvent.Error }
        }
    }

    fun deleteNote(note: Note, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { noteRepository.deleteById(note.id) }
                .onSuccess { onDeleted() }
                .onFailure {
                    if (it is kotlinx.coroutines.CancellationException) throw it
                    _events.value = NoteListEvent.Error
                }
        }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch {
            runCatching { noteRepository.upsert(note) }
                .onFailure {
                    if (it is kotlinx.coroutines.CancellationException) throw it
                    _events.value = NoteListEvent.Error
                }
        }
    }

    fun consumeEvent() {
        _events.value = null
    }
}

sealed interface NoteListEvent {
    data object Error : NoteListEvent
}

enum class NoteFilter {
    ALL,
    PINNED,
    RECENT,
}
