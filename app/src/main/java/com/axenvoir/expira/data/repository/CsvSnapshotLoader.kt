package com.axenvoir.expira.data.repository

import com.axenvoir.expira.domain.model.Note
import com.axenvoir.expira.domain.model.Product
import com.axenvoir.expira.domain.repository.NoteRepository
import com.axenvoir.expira.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

data class CsvDataSnapshot(
    val products: List<Product>,
    val notes: List<Note>,
)

@Singleton
class CsvSnapshotLoader @Inject constructor(
    private val productRepository: ProductRepository,
    private val noteRepository: NoteRepository,
) {
    suspend fun loadSnapshot(): Result<CsvDataSnapshot> = withContext(Dispatchers.IO) {
        try {
            val products = productRepository.observeAll().first()
            val notes = noteRepository.observeAll().first()
            Result.success(CsvDataSnapshot(products = products, notes = notes))
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}
