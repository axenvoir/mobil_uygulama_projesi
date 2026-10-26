package com.expira.app.tr.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expira.app.tr.data.local.AppDatabase
import com.expira.app.tr.data.model.Note
import com.expira.app.tr.data.model.Product
import com.expira.app.tr.data.repository.NoteRepository
import com.expira.app.tr.data.repository.NoteRepositoryImpl
import com.expira.app.tr.data.repository.ProductRepository
import com.expira.app.tr.data.repository.ProductRepositoryImpl
import com.expira.app.tr.ui.screens.HomeScreen
import com.expira.app.tr.ui.screens.NotesScreen
import com.expira.app.tr.ui.screens.SettingsScreen
import kotlinx.coroutines.launch

enum class AppTab {
    EXPIRY,
    NOTES,
    SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiraApp(
    database: AppDatabase
) {
    val coroutineScope = rememberCoroutineScope()
    val productRepository: ProductRepository = remember { ProductRepositoryImpl(database.productDao()) }
    val noteRepository: NoteRepository = remember { NoteRepositoryImpl(database.noteDao()) }

    var currentTab by remember { mutableStateOf(AppTab.EXPIRY) }
    var isTurkish by remember { mutableStateOf(true) }

    val products by productRepository.productsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val notes by noteRepository.notesFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "🌿 Expira",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = when (currentTab) {
                                AppTab.EXPIRY -> if (isTurkish) "Son Kullanma Tarihi Takibi" else "Expiry Date Tracker"
                                AppTab.NOTES -> if (isTurkish) "Kiler & Alışveriş Notları" else "Pantry & Shopping Notes"
                                AppTab.SETTINGS -> if (isTurkish) "Ayarlar & Sistem" else "Settings & Preferences"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.EXPIRY,
                    onClick = { currentTab = AppTab.EXPIRY },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Expiry") },
                    label = { Text(if (isTurkish) "SKT Takibi" else "Expiry") }
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.NOTES,
                    onClick = { currentTab = AppTab.NOTES },
                    icon = { Icon(Icons.Default.Description, contentDescription = "Notes") },
                    label = { Text(if (isTurkish) "Notlar" else "Notes") }
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.SETTINGS,
                    onClick = { currentTab = AppTab.SETTINGS },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text(if (isTurkish) "Ayarlar" else "Settings") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.EXPIRY -> {
                    HomeScreen(
                        products = products,
                        isTurkish = isTurkish,
                        onAddProduct = { product ->
                            coroutineScope.launch { productRepository.addProduct(product) }
                        },
                        onUpdateProduct = { product ->
                            coroutineScope.launch { productRepository.updateProduct(product) }
                        },
                        onDeleteProduct = { id ->
                            coroutineScope.launch { productRepository.deleteProduct(id) }
                        }
                    )
                }

                AppTab.NOTES -> {
                    NotesScreen(
                        notes = notes,
                        isTurkish = isTurkish,
                        onAddNote = { note ->
                            coroutineScope.launch { noteRepository.addNote(note) }
                        },
                        onUpdateNote = { note ->
                            coroutineScope.launch { noteRepository.updateNote(note) }
                        },
                        onDeleteNote = { id ->
                            coroutineScope.launch { noteRepository.deleteNote(id) }
                        },
                        onTogglePin = { id ->
                            coroutineScope.launch { noteRepository.togglePin(id) }
                        }
                    )
                }

                AppTab.SETTINGS -> {
                    SettingsScreen(
                        isTurkish = isTurkish,
                        onLanguageChange = { isTurkish = it },
                        onResetToSampleData = {
                            coroutineScope.launch {
                                AppDatabase.populateInitialData(database.productDao(), database.noteDao())
                            }
                        },
                        onClearAllData = {
                            coroutineScope.launch {
                                productRepository.clearAll()
                                noteRepository.clearAll()
                            }
                        }
                    )
                }
            }
        }
    }
}
