package com.expira.app.tr.data.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class Category(
    val id: String,
    val labelTr: String,
    val labelEn: String,
    val icon: String,
    val colorHex: String
) {
    DAIRY("dairy", "Süt Ürünleri", "Dairy", "🥛", "#3B82F6"),
    MEAT("meat", "Et & Balık", "Meat & Fish", "🥩", "#EF4444"),
    VEGGIE("veggie", "Meyve & Sebze", "Fruit & Veg", "🥦", "#10B981"),
    BAKERY("bakery", "Fırın & Ekmek", "Bakery", "🍞", "#F59E0B"),
    PANTRY("pantry", "Konserve & Kuru Gıda", "Pantry", "🥫", "#8B5CF6"),
    BEVERAGE("beverage", "İçecekler", "Beverages", "🧃", "#06B6D4"),
    SNACK("snack", "Atıştırmalık", "Snacks", "🍪", "#EC4899"),
    OTHER("other", "Diğer", "Other", "📦", "#6B7280");

    fun getDisplayName(isTurkish: Boolean): String = if (isTurkish) labelTr else labelEn

    companion object {
        fun fromId(id: String): Category = entries.find { it.id.equals(id, ignoreCase = true) } ?: OTHER
    }
}

enum class ExpiryStatus {
    FRESH,
    APPROACHING,
    EXPIRED;

    companion object {
        fun fromDays(daysLeft: Long): ExpiryStatus = when {
            daysLeft < 0 -> EXPIRED
            daysLeft <= 3 -> APPROACHING
            else -> FRESH
        }
    }
}
