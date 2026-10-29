package com.axenvoir.expira.ui.main

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.axenvoir.expira.R
import com.axenvoir.expira.ui.components.OnboardingOverlay
import com.axenvoir.expira.ui.navigation.AppNavHost
import com.axenvoir.expira.ui.navigation.Screen

private data class BottomTab(
    val screen: Screen,
    val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val tabs = listOf(
    BottomTab(Screen.ProductList, R.string.tab_expiry, Icons.Filled.DateRange),
    BottomTab(Screen.NoteList, R.string.tab_notes, Icons.AutoMirrored.Filled.List),
)

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val widthSizeClass = if (activity != null) {
        calculateWindowSizeClass(activity).widthSizeClass
    } else {
        WindowWidthSizeClass.Compact
    }
    val useRail = widthSizeClass != WindowWidthSizeClass.Compact
    val prefs = remember { context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE) }
    var showOnboarding by remember { mutableStateOf(prefs.getBoolean("onboarding_done", false).not()) }
    val dismissOnboarding = {
        prefs.edit { putBoolean("onboarding_done", true) }
        showOnboarding = false
    }
    OnboardingOverlay(
        visible = showOnboarding,
        onDismiss = dismissOnboarding,
        onOpenSettings = { navController.navigate(Screen.Settings.route) },
    )
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // Alt menu / yan menu sadece ana sekmelerde gorunur.
    // Ekle, duzenle ve ayarlar ekranlarinda gizlenir (Kaydet butonunu kapatmasin).
    // currentDestination ilk karede null olabilir; bu durumda menuyu gostererek titremeyi onle.
    val showNavigation = currentDestination == null || tabs.any { tab ->
        currentDestination.hierarchy.any { it.route == tab.screen.route }
    }

    val onTabClick: (Screen) -> Unit = { screen ->
        navController.navigate(screen.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // contentWindowInsets = 0: sistem cubugu bosluklarini her ekran kendi Scaffold'unda
    // bir kez uygular. Boylece ust barin ustunde ikinci bir bosluk / ton seridi olusmaz.
    if (useRail) {
        Row {
            if (showNavigation) {
                NavigationRail(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                ) {
                    tabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.screen.route } == true
                        NavigationRailItem(
                            selected = selected,
                            onClick = { onTabClick(tab.screen) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) },
                            colors = androidx.compose.material3.NavigationRailItemDefaults.colors(
                                selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                                indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }
                }
            }
            Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { innerPadding ->
                AppNavHost(
                    navController = navController,
                    modifier = Modifier
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
                )
            }
        }
    } else {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showNavigation) {
                    NavigationBar(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                    ) {
                        tabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.screen.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { onTabClick(tab.screen) },
                                icon = { Icon(tab.icon, contentDescription = null) },
                                label = { Text(stringResource(tab.labelRes)) },
                                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                    selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                                    indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                                ),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            )
        }
    }
}

@Composable
fun SettingsIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            Icons.Outlined.Settings,
            contentDescription = stringResource(R.string.settings_title),
        )
    }
}
