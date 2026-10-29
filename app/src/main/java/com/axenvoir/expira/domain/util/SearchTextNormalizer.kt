package com.axenvoir.expira.domain.util

import java.text.Normalizer
import java.util.Locale

private val combiningMarks = Regex("\\p{M}+")

/** Normalizes user search input without changing the text shown in the search field. */
fun normalizeSearchText(value: String): String =
    Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
        .replace(combiningMarks, "")
        .lowercase(Locale.ROOT)
        .replace('ı', 'i')
