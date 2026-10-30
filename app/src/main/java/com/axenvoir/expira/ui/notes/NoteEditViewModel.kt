package com.axenvoir.expira.ui.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axenvoir.expira.domain.model.Note
import com.axenvoir.expira.domain.repository.NoteRepository
import com.axenvoir.expira.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteEditViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        const val MAX_TITLE_LENGTH = 80
        const val MAX_CONTENT_LENGTH = 10_000
    }

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

    private val _editRecordMissing = MutableStateFlow(false)
    val editRecordMissing: StateFlow<Boolean> = _editRecordMissing.asStateFlow()

    private val _loadFailed = MutableStateFlow(false)
    val loadFailed: StateFlow<Boolean> = _loadFailed.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val loaded = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = loaded
        .map { !it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = isEditMode,
        )

    private var originalTitle: String = ""
    private var originalContent: String = ""
    private var originalIsPinned: Boolean = false
    private val _userEditedTitle = MutableStateFlow(false)

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

    private val _suggestedTitle = MutableStateFlow("")
    val suggestedTitle: StateFlow<String> = _suggestedTitle.asStateFlow()

    init {
        if (isEditMode) {
            loadExistingNote()
        } else {
            loaded.value = true
        }
    }

    fun retryLoad() {
        if (isEditMode && _loadFailed.value) loadExistingNote()
    }

    private fun loadExistingNote() {
        viewModelScope.launch {
            loaded.value = false
            _editRecordMissing.value = false
            _loadFailed.value = false
            _error.value = null
            try {
                val note = noteRepository.observeById(noteId).first()
                if (note != null) {
                    originalTitle = note.title
                    originalContent = note.content
                    originalIsPinned = note.isPinned
                    _title.value = note.title
                    _content.value = note.content
                    _isPinned.value = note.isPinned
                } else {
                    _editRecordMissing.value = true
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _loadFailed.value = true
            } finally {
                loaded.value = true
            }
        }
    }

    fun onTitleChange(value: String) {
        _userEditedTitle.value = true
        _title.value = value
        if (_error.value is NoteEditError.EmptyTitle ||
            _error.value is NoteEditError.TitleTooLong ||
            _error.value is NoteEditError.Generic
        ) {
            _error.value = null
        }
    }

    fun onContentChange(value: String) {
        _content.value = value
        if (_title.value.isBlank() && !_userEditedTitle.value) {
            val candidate = value.lineSequence()
                .firstOrNull { it.isNotBlank() }
                ?.trim()
                .orEmpty()
            _suggestedTitle.value = candidate.takeIf { it.length <= MAX_TITLE_LENGTH }.orEmpty()
        } else {
            _suggestedTitle.value = ""
        }
        if (_error.value is NoteEditError.ContentTooLong || _error.value is NoteEditError.Generic) {
            _error.value = null
        }
    }

    fun applySuggestedTitle() {
        if (_title.value.isBlank() && _suggestedTitle.value.isNotBlank() && !_userEditedTitle.value) {
            _title.value = _suggestedTitle.value
            _suggestedTitle.value = ""
            if (_error.value is NoteEditError.EmptyTitle) _error.value = null
        }
    }

    fun onPinnedChange(value: Boolean) {
        _isPinned.value = value
    }

    fun save() {
        if (
            _isSaving.value || _saved.value || !loaded.value || _loadFailed.value ||
            (isEditMode && _editRecordMissing.value)
        ) return

        val titleForValidation = _title.value.trim()
        when {
            titleForValidation.isEmpty() -> {
                _error.value = NoteEditError.EmptyTitle
                return
            }

            _title.value.length > MAX_TITLE_LENGTH -> {
                _error.value = NoteEditError.TitleTooLong
                return
            }

            _content.value.length > MAX_CONTENT_LENGTH -> {
                _error.value = NoteEditError.ContentTooLong
                return
            }
        }

        _error.value = null
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val now = System.currentTimeMillis()
                val existing = if (isEditMode) noteRepository.observeById(noteId).first() else null
                if (isEditMode && existing == null) {
                    _editRecordMissing.value = true
                    return@launch
                }
                val note = Note(
                    id = existing?.id ?: 0L,
                    title = _title.value,
                    content = _content.value,
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

}

sealed interface NoteEditError {
    data object EmptyTitle : NoteEditError
    data object TitleTooLong : NoteEditError
    data object ContentTooLong : NoteEditError
    data object Generic : NoteEditError
}
