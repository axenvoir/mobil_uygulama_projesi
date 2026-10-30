package com.axenvoir.expira.ui.theme

object ExpiraPalettes {
    const val GROVE = "grove"
    const val QUIET_DUSK = "quiet_dusk"
    const val CITRUS_LEDGER = "citrus_ledger"
    const val BERRY_ARCHIVE = "berry_archive"

    val all: Set<String> = setOf(GROVE, QUIET_DUSK, CITRUS_LEDGER, BERRY_ARCHIVE)

    fun normalize(id: String?): String = id?.takeIf(all::contains) ?: GROVE
}
