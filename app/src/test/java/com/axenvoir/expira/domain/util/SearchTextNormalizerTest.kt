package com.axenvoir.expira.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchTextNormalizerTest {
    @Test
    fun trimsAndIgnoresTurkishCaseVariants() {
        assertEquals("icecek", normalizeSearchText("  İÇECEK  "))
        assertEquals("ispanak", normalizeSearchText("ıspanak"))
    }

    @Test
    fun ignoresAccentsWithoutChangingStoredText() {
        assertEquals("seker", normalizeSearchText("Şeker"))
        assertEquals("creme brulee", normalizeSearchText("Crème brûlée"))
    }

    @Test
    fun whitespaceOnlySearchBecomesEmpty() {
        assertEquals("", normalizeSearchText(" \n\t "))
    }
}
