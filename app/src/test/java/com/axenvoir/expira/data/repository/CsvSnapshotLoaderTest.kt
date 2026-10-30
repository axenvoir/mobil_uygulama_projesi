package com.axenvoir.expira.data.repository

import com.axenvoir.expira.domain.model.Category
import com.axenvoir.expira.domain.model.Note
import com.axenvoir.expira.domain.model.Product
import com.axenvoir.expira.domain.repository.NoteRepository
import com.axenvoir.expira.domain.repository.ProductRepository
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CsvSnapshotLoaderTest {
    @Test
    fun loadSnapshotReturnsProductsAndNotes() = runTest {
        val products = listOf(
            Product(
                id = 1L,
                name = "Milk",
                categoryId = 2L,
                categoryName = "Dairy",
                expiryDate = LocalDate.of(2026, 11, 2),
                quantity = 1,
                createdAt = 100L,
            ),
        )
        val notes = listOf(
            Note(
                id = 3L,
                title = "Shopping",
                content = "Bread",
                createdAt = 200L,
                updatedAt = 300L,
            ),
        )
        val loader = CsvSnapshotLoader(
            productRepository = FakeProductRepository(flowOf(products)),
            noteRepository = FakeNoteRepository(flowOf(notes)),
        )

        val result = loader.loadSnapshot()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(CsvDataSnapshot(products, notes))
    }

    @Test
    fun productReadFailureIsReturnedAsFailure() = runTest {
        val productFailure = IOException("Simulated product snapshot read failure")
        val loader = CsvSnapshotLoader(
            productRepository = FakeProductRepository(flow { throw productFailure }),
            noteRepository = FakeNoteRepository(flowOf(emptyList())),
        )

        val result = loader.loadSnapshot()

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isSameInstanceAs(productFailure)
    }

    @Test
    fun noteReadFailureIsReturnedAsFailure() = runTest {
        val noteFailure = IOException("Simulated note snapshot read failure")
        val loader = CsvSnapshotLoader(
            productRepository = FakeProductRepository(flowOf(emptyList())),
            noteRepository = FakeNoteRepository(flow { throw noteFailure }),
        )

        val result = loader.loadSnapshot()

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isSameInstanceAs(noteFailure)
    }

    private class FakeProductRepository(
        private val products: Flow<List<Product>>,
    ) : ProductRepository {
        override fun observeAll(): Flow<List<Product>> = products

        override fun observeById(id: Long): Flow<Product?> = flowOf(null)

        override suspend fun upsert(product: Product): Long = product.id

        override suspend fun deleteById(id: Long) = Unit

        override fun observeCategories(): Flow<List<Category>> = flowOf(emptyList())

        override suspend fun addCategory(name: String): Long = 1L

        override suspend fun seedDefaultCategoriesIfEmpty() = Unit
    }

    private class FakeNoteRepository(
        private val notes: Flow<List<Note>>,
    ) : NoteRepository {
        override fun observeAll(): Flow<List<Note>> = notes

        override fun observeById(id: Long): Flow<Note?> = flowOf(null)

        override suspend fun upsert(note: Note): Long = note.id

        override suspend fun deleteById(id: Long) = Unit

        override suspend fun setPinned(id: Long, isPinned: Boolean) = Unit
    }
}
