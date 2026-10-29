package com.axenvoir.expira.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.axenvoir.expira.ui.expiry.ProductEditScreen
import com.axenvoir.expira.ui.expiry.ProductListScreen
import com.axenvoir.expira.ui.notes.NoteEditScreen
import com.axenvoir.expira.ui.settings.SettingsScreen
import com.axenvoir.expira.ui.notes.NoteListScreen

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
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
            )
        }
        composable(Screen.AddProduct.route) {
            ProductEditScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAdd = { navController.navigate(Screen.AddProduct.route) },
            )
        }
        composable(
            route = Screen.EditProduct.route,
            arguments = listOf(navArgument(Screen.EditProduct.ARG_PRODUCT_ID) { type = NavType.LongType }),
        ) {
            ProductEditScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAdd = {
                    navController.navigate(Screen.AddProduct.route) {
                        popUpTo(Screen.EditProduct.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(Screen.NoteList.route) {
            NoteListScreen(
                onAddNote = { navController.navigate(Screen.AddNote.route) },
                onEditNote = { id -> navController.navigate(Screen.EditNote.createRoute(id)) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
            )
        }
        composable(Screen.AddNote.route) {
            NoteEditScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAdd = { navController.navigate(Screen.AddNote.route) },
            )
        }
        composable(
            route = Screen.EditNote.route,
            arguments = listOf(navArgument(Screen.EditNote.ARG_NOTE_ID) { type = NavType.LongType }),
        ) {
            NoteEditScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAdd = {
                    navController.navigate(Screen.AddNote.route) {
                        popUpTo(Screen.EditNote.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
