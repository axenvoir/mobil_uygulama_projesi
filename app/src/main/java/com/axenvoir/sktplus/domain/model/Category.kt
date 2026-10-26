package com.axenvoir.sktplus.domain.model

data class Category(
    val id: Long = 0L,
    val name: String,
    val isDefault: Boolean = false,
)
