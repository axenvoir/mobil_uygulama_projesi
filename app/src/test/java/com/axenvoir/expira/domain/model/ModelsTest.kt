package com.axenvoir.expira.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class ModelsTest {

    @Test
    fun `product equality is based on all fields`() {
        val date = LocalDate.of(2025, 6, 15)
        val a = Product(id = 1, name = "Süt", categoryId = 2, categoryName = "Gıda", expiryDate = date, quantity = 1, createdAt = 100L)
        val b = Product(id = 1, name = "Süt", categoryId = 2, categoryName = "Gıda", expiryDate = date, quantity = 1, createdAt = 100L)
        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `note defaults are unpinned with zero id`() {
        val note = Note(title = "t", content = "c", createdAt = 1L, updatedAt = 1L)
        assertThat(note.id).isEqualTo(0L)
        assertThat(note.isPinned).isFalse()
    }

    @Test
    fun `category default is not default category`() {
        val category = Category(name = "X")
        assertThat(category.isDefault).isFalse()
    }
}
