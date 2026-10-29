package com.axenvoir.expira.ui.navigation

sealed class Screen(val route: String) {
    data object ProductList : Screen("products")
    data object AddProduct : Screen("product/add")
    data object EditProduct : Screen("product/edit/{productId}") {
        const val ARG_PRODUCT_ID = "productId"
        fun createRoute(productId: Long) = "product/edit/$productId"
    }
    data object NoteList : Screen("notes")
    data object AddNote : Screen("note/add")
    data object EditNote : Screen("note/edit/{noteId}") {
        const val ARG_NOTE_ID = "noteId"
        fun createRoute(noteId: Long) = "note/edit/$noteId"
    }
    data object Settings : Screen("settings")
}
