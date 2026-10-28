package com.axenvoir.sktplus.ui.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axenvoir.sktplus.domain.model.Note
import com.axenvoir.sktplus.domain.repository.NoteRepository
import com.axenvoir.sktplus.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteEditViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val noteId: Long = savedStateHandle.get<Long>(Screen.EditNote.ARG_NOTE_ID) ?: 0L
    val isEditMode: Boolean = noteId != 0L

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _isPinned = MutableStateFlow(false)
    val isPinned: StateFlow<Boolean> = _isPinned.asStateFlow()

    private val _error = MutableStateFlow<NoteEditError?>(null)
    val error: StateFlow<NoteEditError?> = _error.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private var originalTitle: String = ""
    private var originalContent: String = ""
    private var originalIsPinned: Boolean = false
    private var loaded = MutableStateFlow(false)

    val isDirty: StateFlow<Boolean> = combine(
        title,
        content,
        isPinned,
        loaded,
    ) { t, c, pinned, isLoaded ->
        isLoaded && (t != originalTitle || c != originalContent || pinned != originalIsPinned)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    init {
        if (isEditMode) {
            viewModelScope.launch {
                val note = noteRepository.observeById(noteId).first()
                loaded.value = true
                if (note != null) {
                    originalTitle = note.title
                    originalContent = note.content
                    originalIsPinned = note.isPinned
                    _title.value = note.title
                    _content.value = note.content
                    _isPinned.value = note.isPinned
                }
            }
        } else {
            loaded.value = true
        }
    }

    fun onTitleChange(value: String) {
        _title.value = value
    }

    fun onContentChange(value: String) {
        _content.value = value
        if (_title.value.isBlank()) {
            _suggestedTitle.value = value.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
        } else {
            _suggestedTitle.value = ""
        }
    }

    private var _suggestedTitle = MutableStateFlow("")
    val suggestedTitle: StateFlow<String> = _suggestedTitle.asStateFlow()

    fun applySuggestedTitle() {
        if (_title.value.isBlank() && _suggestedTitle.value.isNotBlank()) {
            _title.value = _suggestedTitle.value.take(60)
            _suggestedTitle.value = ""
        }
    }

    fun onPinnedChange(value: Boolean) {
        _isPinned.value = value
    }

    fun save() {
        if (_isSaving.value || _saved.value) return
        val trimmedTitle = _title.value.trim()
        if (trimmedTitle.isEmpty()) {
            _error.value = NoteEditError.EmptyTitle
            return
        }
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val now = System.currentTimeMillis()
                val existing = if (isEditMode) noteRepository.observeById(noteId).first() else null
                val note = Note(
                    id = existing?.id ?: 0L,
                    title = trimmedTitle,
                    content = _content.value.trim(),
                    isPinned = _isPinned.value,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now,
                )
                noteRepository.upsert(note)
                _saved.value = true
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = NoteEditError.Generic
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun consumeError() {
        _error.value = null
    }
}

sealed interface NoteEditError {
    data object EmptyTitle : NoteEditError
    data object Generic : NoteEditError
}
