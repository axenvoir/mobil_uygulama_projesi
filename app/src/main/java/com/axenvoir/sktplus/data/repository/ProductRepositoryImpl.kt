package com.axenvoir.sktplus.data.repository

import com.axenvoir.sktplus.data.local.CategoryEntity
import com.axenvoir.sktplus.data.local.ProductDao
import com.axenvoir.sktplus.data.local.ProductEntity
import com.axenvoir.sktplus.domain.model.Category
import com.axenvoir.sktplus.domain.model.Product
import com.axenvoir.sktplus.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
) : ProductRepository {

    override fun observeAll(): Flow<List<Product>> =
        productDao.observeAllWithCategory().map { rows -> rows.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Product?> =
        productDao.observeByIdWithCategory(id).map { it?.toDomain() }

    override suspend fun upsert(product: Product): Long {
        val entity = ProductEntity(
            id = product.id,
            name = product.name,
            categoryId = product.categoryId,
            expiryEpochDay = product.expiryDate.toEpochDay(),
            quantity = product.quantity,
            createdAt = product.createdAt,
        )
        return if (product.id == 0L) productDao.insert(entity) else {
            productDao.update(entity)
            product.id
        }
    }

    override suspend fun deleteById(id: Long) = productDao.deleteById(id)

    override fun observeCategories(): Flow<List<Category>> =
        productDao.observeCategories().map { entities ->
            entities.map { Category(id = it.id, name = it.name, isDefault = it.isDefault) }
        }

    override suspend fun addCategory(name: String): Long {
        val trimmed = name.trim()
        val existing = productDao.findCategoryIdByName(trimmed)
        if (existing != null) return existing
        val result = productDao.insertCategory(CategoryEntity(name = trimmed, isDefault = false))
        if (result == -1L) {
            val retry = productDao.findCategoryIdByName(trimmed)
            if (retry != null) return retry
            throw IllegalStateException("Category could not be inserted: $trimmed")
        }
        return result
    }

    override suspend fun seedDefaultCategoriesIfEmpty() {
        if (productDao.categoryCount() > 0) return
        productDao.insertCategories(
            listOf(
                CategoryEntity(name = "Gıda", isDefault = true),
                CategoryEntity(name = "İlaç", isDefault = true),
                CategoryEntity(name = "Kozmetik", isDefault = true),
                CategoryEntity(name = "Temizlik", isDefault = true),
                CategoryEntity(name = "Diğer", isDefault = true),
            )
        )
    }

    private fun com.axenvoir.sktplus.data.local.ProductWithCategory.toDomain() = Product(
        id = id,
        name = name,
        categoryId = categoryId,
        categoryName = categoryName,
        expiryDate = LocalDate.ofEpochDay(expiryEpochDay),
        quantity = quantity,
        createdAt = createdAt,
    )
}
