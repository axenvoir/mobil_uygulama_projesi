package com.axenvoir.expira.ui.notes

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModelStore
import com.axenvoir.expira.domain.model.Note
import com.axenvoir.expira.domain.repository.NoteRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteListViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUpMainDispatcher() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun searchMatchesTurkishCaseAndAccents() = runTest {
        val repository = FakeNoteRepository(
            listOf(
                Note(id = 1, title = "İçecek listesi", content = "Crème brûlée", createdAt = 1, updatedAt = 2),
                Note(id = 2, title = "Diğer not", content = "Şeker", createdAt = 1, updatedAt = 1),
            ),
        )
        val viewModel = NoteListViewModel(repository)
        val store = ViewModelStore().apply { put("note-list", viewModel) }
        try {
            val collection = backgroundScope.launch { viewModel.notes.collect {} }
            viewModel.onQueryChange("icecek")
            advanceUntilIdle()
            assertThat(viewModel.notes.value.map { it.id }).containsExactly(1L)

            viewModel.onQueryChange("seker")
            advanceUntilIdle()
            assertThat(viewModel.notes.value.map { it.id }).containsExactly(2L)
            collection.cancel()
        } finally {
            store.clear()
        }
    }

    @Test
    fun recentFilterIsChronologicalEvenWhenAnOlderNoteIsPinned() = runTest {
        val repository = FakeNoteRepository(
            listOf(
                Note(id = 1, title = "Older pinned", content = "", isPinned = true, createdAt = 1, updatedAt = 10),
                Note(id = 2, title = "Newest", content = "", createdAt = 2, updatedAt = 20),
                Note(id = 3, title = "Middle", content = "", createdAt = 3, updatedAt = 15),
            ),
        )
        val viewModel = NoteListViewModel(repository)
        val store = ViewModelStore().apply { put("note-list", viewModel) }
        try {
            val collection = backgroundScope.launch { viewModel.notes.collect {} }
            viewModel.setFilter(NoteFilter.RECENT)
            advanceUntilIdle()

            assertThat(viewModel.notes.value.map { it.id }).containsExactly(2L, 3L, 1L).inOrder()
            collection.cancel()
        } finally {
            store.clear()
        }
    }

    private class FakeNoteRepository(notes: List<Note>) : NoteRepository {
        private val values = MutableStateFlow(notes)

        override fun observeAll() = values

        override fun observeById(id: Long) = values.map { list -> list.firstOrNull { it.id == id } }

        override suspend fun upsert(note: Note): Long {
            values.value = values.value.filterNot { it.id == note.id } + note
            return note.id
        }

        override suspend fun deleteById(id: Long) {
            values.value = values.value.filterNot { it.id == id }
        }

        override suspend fun setPinned(id: Long, isPinned: Boolean) {
            values.value = values.value.map { if (it.id == id) it.copy(isPinned = isPinned) else it }
        }
    }
}
