package com.axenvoir.sktplus.domain.repository

import com.axenvoir.sktplus.domain.model.Category
import com.axenvoir.sktplus.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeAll(): Flow<List<Product>>
    fun observeById(id: Long): Flow<Product?>
    suspend fun upsert(product: Product): Long
    suspend fun deleteById(id: Long)
    fun observeCategories(): Flow<List<Category>>
    suspend fun addCategory(name: String): Long
    suspend fun seedDefaultCategoriesIfEmpty()
}
