package com.expira.app.tr.data.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Product(
    val id: Long = 0,
    val name: String,
    val category: Category = Category.DAIRY,
    val expiryDate: LocalDate,
    val quantity: String = "1 Adet",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val daysLeft: Long
        get() = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate)

    val status: ExpiryStatus
        get() = ExpiryStatus.fromDays(daysLeft)
}

data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val colorHex: String = "#267968",
    val isPinned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
