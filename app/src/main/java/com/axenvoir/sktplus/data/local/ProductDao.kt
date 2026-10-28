package com.axenvoir.sktplus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

data class ProductWithCategory(
    val id: Long,
    val name: String,
    val categoryId: Long,
    val categoryName: String,
    val expiryEpochDay: Long,
    val quantity: Int,
    val createdAt: Long,
    val storageLocation: String,
)

@Dao
interface ProductDao {
    @Query(
        """
        SELECT p.id AS id, p.name AS name, p.categoryId AS categoryId,
               c.name AS categoryName, p.expiryEpochDay AS expiryEpochDay,
               p.quantity AS quantity, p.createdAt AS createdAt,
               p.storageLocation AS storageLocation
        FROM products AS p
        INNER JOIN categories AS c ON c.id = p.categoryId
        ORDER BY p.expiryEpochDay ASC
        """
    )
    fun observeAllWithCategory(): Flow<List<ProductWithCategory>>

    @Query(
        """
        SELECT p.id AS id, p.name AS name, p.categoryId AS categoryId,
               c.name AS categoryName, p.expiryEpochDay AS expiryEpochDay,
               p.quantity AS quantity, p.createdAt AS createdAt,
               p.storageLocation AS storageLocation
        FROM products AS p
        INNER JOIN categories AS c ON c.id = p.categoryId
        WHERE p.id = :id
        """
    )
    fun observeByIdWithCategory(id: Long): Flow<ProductWithCategory?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Upsert
    suspend fun upsert(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun categoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("SELECT * FROM categories ORDER BY isDefault DESC, name ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Query("SELECT id FROM categories WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun findCategoryIdByName(name: String): Long?

    @Query(
        """
        SELECT COUNT(*) FROM products AS p
        WHERE p.expiryEpochDay - :todayEpochDay BETWEEN 0 AND :thresholdDays
        """
    )
    suspend fun countExpiringSoon(todayEpochDay: Long, thresholdDays: Long): Int

    @Query(
        """
        SELECT p.name AS name, p.expiryEpochDay - :todayEpochDay AS daysLeft
        FROM products AS p
        WHERE p.expiryEpochDay - :todayEpochDay BETWEEN 0 AND :thresholdDays
        ORDER BY p.expiryEpochDay ASC
        LIMIT 3
        """
    )
    suspend fun topExpiringNames(todayEpochDay: Long, thresholdDays: Long): List<ExpiringItem>

    @Query("SELECT expiryEpochDay FROM products WHERE id = :id")
    suspend fun findExpiryEpochDayById(id: Long): Long?
}

data class ExpiringItem(val name: String, val daysLeft: Long)
