package com.expira.app.tr.data.repository

import com.expira.app.tr.data.local.NoteDao
import com.expira.app.tr.data.local.NoteEntity
import com.expira.app.tr.data.local.ProductDao
import com.expira.app.tr.data.local.ProductEntity
import com.expira.app.tr.data.model.Note
import com.expira.app.tr.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ProductRepository {
    val productsFlow: Flow<List<Product>>
    suspend fun getProductsSync(): List<Product>
    suspend fun addProduct(product: Product): Long
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(id: Long)
    suspend fun clearAll()
}

class ProductRepositoryImpl(private val productDao: ProductDao) : ProductRepository {
    override val productsFlow: Flow<List<Product>> = productDao.getAllProductsFlow().map { list ->
        list.map { it.toDomain() }
    }

    override suspend fun getProductsSync(): List<Product> {
        return productDao.getAllProductsSync().map { it.toDomain() }
    }

    override suspend fun addProduct(product: Product): Long {
        return productDao.insertProduct(ProductEntity.fromDomain(product))
    }

    override suspend fun updateProduct(product: Product) {
        productDao.updateProduct(ProductEntity.fromDomain(product))
    }

    override suspend fun deleteProduct(id: Long) {
        productDao.deleteById(id)
    }

    override suspend fun clearAll() {
        productDao.clearAll()
    }
}

interface NoteRepository {
    val notesFlow: Flow<List<Note>>
    suspend fun addNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: Long)
    suspend fun togglePin(id: Long)
    suspend fun clearAll()
}

class NoteRepositoryImpl(private val noteDao: NoteDao) : NoteRepository {
    override val notesFlow: Flow<List<Note>> = noteDao.getAllNotesFlow().map { list ->
        list.map { it.toDomain() }
    }

    override suspend fun addNote(note: Note): Long {
        return noteDao.insertNote(NoteEntity.fromDomain(note))
    }

    override suspend fun updateNote(note: Note) {
        noteDao.updateNote(NoteEntity.fromDomain(note))
    }

    override suspend fun deleteNote(id: Long) {
        noteDao.deleteById(id)
    }

    override suspend fun togglePin(id: Long) {
        noteDao.togglePin(id)
    }

    override suspend fun clearAll() {
        noteDao.clearAll()
    }
}
