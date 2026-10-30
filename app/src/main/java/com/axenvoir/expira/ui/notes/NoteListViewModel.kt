package com.axenvoir.expira.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axenvoir.expira.domain.model.Note
import com.axenvoir.expira.domain.repository.NoteRepository
import com.axenvoir.expira.domain.util.normalizeSearchText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class NoteSourceState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
)

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

    private val sourceState = MutableStateFlow(NoteSourceState())
    private var observationJob: Job? = null

    val isLoading: StateFlow<Boolean> = sourceState
        .map { it.isLoading }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val loadFailed: StateFlow<Boolean> = sourceState
        .map { it.loadFailed }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val hasAnyNotes: StateFlow<Boolean> = sourceState
        .map { it.notes.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val notes: StateFlow<List<Note>> = combine(
        sourceState,
        searchQuery,
        selectedFilter,
    ) { source, query, filter ->
        val normalizedQuery = normalizeSearchText(query)
        val filteredByType = when (filter) {
            NoteFilter.ALL -> source.notes
            NoteFilter.PINNED -> source.notes.filter { it.isPinned }
            NoteFilter.RECENT -> source.notes.sortedByDescending { it.updatedAt }.take(20)
        }
        val filtered = if (normalizedQuery.isBlank()) filteredByType
        else filteredByType.filter { note ->
            normalizeSearchText(note.title).contains(normalizedQuery) ||
                normalizeSearchText(note.content).contains(normalizedQuery)
        }
        if (filter == NoteFilter.RECENT) {
            filtered.sortedByDescending { it.updatedAt }
        } else {
            filtered.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt })
        }
    }.distinctUntilChanged().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    init {
        observeNotes()
    }

    fun retryLoad() {
        observeNotes()
    }

    private fun observeNotes() {
        observationJob?.cancel()
        sourceState.value = sourceState.value.copy(isLoading = true, loadFailed = false)
        observationJob = viewModelScope.launch {
            var hasEmitted = false
            try {
                noteRepository.observeAll().collect { notes ->
                    hasEmitted = true
                    sourceState.value = NoteSourceState(
                        notes = notes,
                        isLoading = false,
                        loadFailed = false,
                    )
                }
                if (!hasEmitted) {
                    sourceState.value = sourceState.value.copy(isLoading = false, loadFailed = true)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                sourceState.value = sourceState.value.copy(isLoading = false, loadFailed = true)
            }
        }
    }

    fun onQueryChange(query: String) {
        searchQuery.value = query
    }

    fun setFilter(filter: NoteFilter) {
        selectedFilter.value = filter
    }

    fun togglePinned(note: Note) {
        viewModelScope.launch {
            runCatching { noteRepository.setPinned(id = note.id, isPinned = !note.isPinned) }
                .onFailure { if (it is CancellationException) throw it else _events.value = NoteListEvent.Error }
        }
    }

    fun deleteNote(note: Note, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { noteRepository.deleteById(note.id) }
                .onSuccess { onDeleted() }
                .onFailure {
                    if (it is CancellationException) throw it
                    _events.value = NoteListEvent.Error
                }
        }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch {
            runCatching { noteRepository.upsert(note) }
                .onFailure {
                    if (it is CancellationException) throw it
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
