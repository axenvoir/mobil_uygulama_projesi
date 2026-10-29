package com.axenvoir.expira.data.repository

import android.content.Context
import com.axenvoir.expira.R
import com.axenvoir.expira.data.local.CategoryEntity
import com.axenvoir.expira.data.local.ProductDao
import com.axenvoir.expira.data.local.ProductEntity
import com.axenvoir.expira.domain.model.Category
import com.axenvoir.expira.domain.model.Product
import com.axenvoir.expira.domain.repository.ProductRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    @param:ApplicationContext private val context: Context,
) : ProductRepository {

    private val categoryMutex = Mutex()

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
            storageLocation = product.storageLocation,
        )
        return if (product.id == 0L) productDao.insert(entity) else {
            productDao.upsert(entity)
            product.id
        }
    }

    override suspend fun deleteById(id: Long) = productDao.deleteById(id)

    override fun observeCategories(): Flow<List<Category>> =
        productDao.observeCategories().map { entities ->
            entities.map {
                Category(
                    id = it.id,
                    name = displayCategoryName(it.name, it.isDefault),
                    isDefault = it.isDefault,
                )
            }
        }

    override suspend fun addCategory(name: String): Long = categoryMutex.withLock {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) throw IllegalArgumentException("Category name cannot be empty")
        val existing = productDao.findCategoryIdByName(trimmed)
        if (existing != null) return@withLock existing
        val result = productDao.insertCategory(CategoryEntity(name = trimmed, isDefault = false))
        if (result == -1L) {
            val retry = productDao.findCategoryIdByName(trimmed)
            if (retry != null) return@withLock retry
            throw IllegalStateException("Category could not be inserted: $trimmed")
        }
        return@withLock result
    }

    override suspend fun seedDefaultCategoriesIfEmpty() {
        categoryMutex.withLock {
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
    }

    private fun com.axenvoir.expira.data.local.ProductWithCategory.toDomain() = Product(
        id = id,
        name = name,
        categoryId = categoryId,
        categoryName = displayCategoryName(categoryName, categoryIsDefault),
        expiryDate = LocalDate.ofEpochDay(expiryEpochDay),
        quantity = quantity,
        createdAt = createdAt,
        storageLocation = storageLocation,
    )

    private fun displayCategoryName(name: String, isDefault: Boolean): String {
        if (!isDefault) return name
        val resourceId = when (name) {
            "Gıda", "Food" -> R.string.category_food
            "İlaç", "Medicine", "Medication" -> R.string.category_medicine
            "Kozmetik", "Cosmetics" -> R.string.category_cosmetics
            "Temizlik", "Cleaning" -> R.string.category_cleaning
            "Diğer", "Other" -> R.string.category_other
            else -> return name
        }
        return context.getString(resourceId)
    }
}
