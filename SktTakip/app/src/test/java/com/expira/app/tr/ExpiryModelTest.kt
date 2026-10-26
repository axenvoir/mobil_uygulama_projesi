package com.expira.app.tr

import com.expira.app.tr.data.model.Category
import com.expira.app.tr.data.model.ExpiryStatus
import com.expira.app.tr.data.model.Product
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ExpiryModelTest {

    @Test
    fun testExpiryStatusCalculation() {
        assertEquals(ExpiryStatus.EXPIRED, ExpiryStatus.fromDays(-5))
        assertEquals(ExpiryStatus.EXPIRED, ExpiryStatus.fromDays(-1))
        assertEquals(ExpiryStatus.APPROACHING, ExpiryStatus.fromDays(0))
        assertEquals(ExpiryStatus.APPROACHING, ExpiryStatus.fromDays(1))
        assertEquals(ExpiryStatus.APPROACHING, ExpiryStatus.fromDays(2))
        assertEquals(ExpiryStatus.APPROACHING, ExpiryStatus.fromDays(3))
        assertEquals(ExpiryStatus.FRESH, ExpiryStatus.fromDays(4))
        assertEquals(ExpiryStatus.FRESH, ExpiryStatus.fromDays(30))
    }

    @Test
    fun testCategoryLookup() {
        assertEquals(Category.DAIRY, Category.fromId("dairy"))
        assertEquals(Category.MEAT, Category.fromId("meat"))
        assertEquals(Category.VEGGIE, Category.fromId("veggie"))
        assertEquals(Category.OTHER, Category.fromId("unknown_category"))
    }

    @Test
    fun testProductDaysLeft() {
        val today = LocalDate.now()
        val p1 = Product(name = "Milk", expiryDate = today)
        assertEquals(0L, p1.daysLeft)
        assertEquals(ExpiryStatus.APPROACHING, p1.status)

        val p2 = Product(name = "Cheese", expiryDate = today.plusDays(10))
        assertEquals(10L, p2.daysLeft)
        assertEquals(ExpiryStatus.FRESH, p2.status)

        val p3 = Product(name = "Yogurt", expiryDate = today.minusDays(2))
        assertEquals(-2L, p3.daysLeft)
        assertEquals(ExpiryStatus.EXPIRED, p3.status)
    }
}
