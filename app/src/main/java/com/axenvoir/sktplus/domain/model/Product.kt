package com.axenvoir.sktplus.domain.model

import java.time.LocalDate

data class Product(
    val id: Long = 0L,
    val name: String,
    val categoryId: Long,
    val categoryName: String,
    val expiryDate: LocalDate,
    val quantity: Int,
    val createdAt: Long,
)
