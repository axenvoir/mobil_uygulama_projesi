package com.axenvoir.sktplus.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.axenvoir.sktplus.ui.expiry.ProductEditScreen
import com.axenvoir.sktplus.ui.expiry.ProductListScreen
import com.axenvoir.sktplus.ui.notes.NoteEditScreen
import com.axenvoir.sktplus.ui.notes.NoteListScreen

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.ProductList.route,
        modifier = modifier,
    ) {
        composable(Screen.ProductList.route) {
            ProductListScreen(
                onAddProduct = { navController.navigate(Screen.AddProduct.route) },
                onEditProduct = { id -> navController.navigate(Screen.EditProduct.createRoute(id)) },
            )
        }
        composable(Screen.AddProduct.route) {
            ProductEditScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.EditProduct.route,
            arguments = listOf(navArgument(Screen.EditProduct.ARG_PRODUCT_ID) { type = NavType.LongType }),
        ) {
            ProductEditScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.NoteList.route) {
            NoteListScreen(
                onAddNote = { navController.navigate(Screen.AddNote.route) },
                onEditNote = { id -> navController.navigate(Screen.EditNote.createRoute(id)) },
            )
        }
        composable(Screen.AddNote.route) {
            NoteEditScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.EditNote.route,
            arguments = listOf(navArgument(Screen.EditNote.ARG_NOTE_ID) { type = NavType.LongType }),
        ) {
            NoteEditScreen(onBack = { navController.popBackStack() })
        }
    }
}
