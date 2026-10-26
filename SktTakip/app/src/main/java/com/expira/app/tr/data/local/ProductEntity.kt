package com.expira.app.tr.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.expira.app.tr.data.model.Category
import com.expira.app.tr.data.model.Product
import java.time.LocalDate

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val categoryId: String,
    val expiryEpochDay: Long,
    val quantity: String,
    val note: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Product = Product(
        id = id,
        name = name,
        category = Category.fromId(categoryId),
        expiryDate = LocalDate.ofEpochDay(expiryEpochDay),
        quantity = quantity,
        note = note,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: Product): ProductEntity = ProductEntity(
            id = domain.id,
            name = domain.name,
            categoryId = domain.category.id,
            expiryEpochDay = domain.expiryDate.toEpochDay(),
            quantity = domain.quantity,
            note = domain.note,
            createdAt = domain.createdAt
        )
    }
}

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val colorHex: String,
    val isPinned: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): com.expira.app.tr.data.model.Note = com.expira.app.tr.data.model.Note(
        id = id,
        title = title,
        content = content,
        colorHex = colorHex,
        isPinned = isPinned,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(domain: com.expira.app.tr.data.model.Note): NoteEntity = NoteEntity(
            id = domain.id,
            title = domain.title,
            content = domain.content,
            colorHex = domain.colorHex,
            isPinned = domain.isPinned,
            updatedAt = domain.updatedAt
        )
    }
}
