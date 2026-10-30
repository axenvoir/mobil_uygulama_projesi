package com.axenvoir.expira.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.axenvoir.expira.data.repository.ProductRepositoryImpl
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    private lateinit var context: Context

    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        context.deleteDatabase(AppDatabase.NAME)
    }

    @Test
    fun migration_1_to_3_preserves_data_and_schema() = runBlocking {
        // Use the production database name so this models a restored Room file, not a
        // separate test-only filename that could hide backup/restore naming mistakes.
        migrationTestHelper.createDatabase(AppDatabase.NAME, 1).use { db ->
            db.execSQL("INSERT INTO categories(id, name, isDefault) VALUES (1, 'Gıda', 1)")
            db.execSQL(
                "INSERT INTO products(id, name, categoryId, expiryEpochDay, quantity, createdAt) VALUES (7, 'Süt', 1, 30000, 2, 1234)"
            )
            db.execSQL(
                "INSERT INTO notes(id, title, content, isPinned, createdAt, updatedAt) VALUES (9, 'Not', 'İçerik', 1, 1111, 2222)"
            )
        }

        migrationTestHelper.runMigrationsAndValidate(
            AppDatabase.NAME,
            3,
            true,
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_3,
        ).use { migratedDb ->
            assertThat(migratedDb.version).isEqualTo(3)
            migratedDb.query("SELECT identity_hash FROM room_master_table WHERE id = 42").use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
                assertThat(cursor.getString(0)).isNotEmpty()
            }
            migratedDb.query(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name IN " +
                    "('support_wallet', 'support_reward_transactions', 'support_cosmetic_unlocks')",
            ).use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
                assertThat(cursor.getInt(0)).isEqualTo(3)
            }
        }

        val db = Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build()
        try {
                val readableDb = db.openHelper.readableDatabase
                assertThat(readableDb.version).isEqualTo(3)

                val productColumns = buildMap {
                    readableDb.query("PRAGMA table_info(products)").use { cursor ->
                        val nameIndex = cursor.getColumnIndexOrThrow("name")
                        val notNullIndex = cursor.getColumnIndexOrThrow("notnull")
                        val defaultIndex = cursor.getColumnIndexOrThrow("dflt_value")
                        while (cursor.moveToNext()) {
                            put(
                                cursor.getString(nameIndex),
                                cursor.getInt(notNullIndex) to
                                    if (cursor.isNull(defaultIndex)) null else cursor.getString(defaultIndex),
                            )
                        }
                    }
                }
                assertThat(productColumns.keys).containsExactly(
                    "id",
                    "name",
                    "categoryId",
                    "expiryEpochDay",
                    "quantity",
                    "createdAt",
                    "storageLocation",
                )
                assertThat(productColumns["storageLocation"]?.first).isEqualTo(1)
                assertThat(productColumns["storageLocation"]?.second).isEqualTo("''")

                val categoryIndex = readableDb.query("PRAGMA index_list(categories)").use { cursor ->
                    val nameIndex = cursor.getColumnIndexOrThrow("name")
                    val uniqueIndex = cursor.getColumnIndexOrThrow("unique")
                    buildList {
                        while (cursor.moveToNext()) {
                            add(cursor.getString(nameIndex) to cursor.getInt(uniqueIndex))
                        }
                    }
                }
                assertThat(categoryIndex).contains("index_categories_name" to 1)

                val foreignKey = readableDb.query("PRAGMA foreign_key_list(products)").use { cursor ->
                    val tableIndex = cursor.getColumnIndexOrThrow("table")
                    val onDeleteIndex = cursor.getColumnIndexOrThrow("on_delete")
                    check(cursor.moveToFirst())
                    cursor.getString(tableIndex) to cursor.getString(onDeleteIndex)
                }
                assertThat(foreignKey).isEqualTo("categories" to "RESTRICT")

                val product = db.productDao().observeByIdWithCategory(7L).first()
                assertThat(product).isNotNull()
                assertThat(product?.name).isEqualTo("Süt")
                assertThat(product?.storageLocation).isEqualTo("")
                val note = db.noteDao().observeById(9L).first()
                assertThat(note?.title).isEqualTo("Not")
                assertThat(note?.content).isEqualTo("İçerik")
                assertThat(note?.isPinned).isTrue()
                assertThat(note?.updatedAt).isEqualTo(2222L)
        } finally {
            db.close()
        }
    }

    @Test
    fun concurrent_category_inserts_return_one_id_and_one_row() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val repositories = listOf(
                ProductRepositoryImpl(database.productDao(), context),
                ProductRepositoryImpl(database.productDao(), context),
            )
            val ids = coroutineScope {
                (0 until 32).map { index ->
                    async(Dispatchers.Default) {
                        repositories[index % repositories.size].addCategory("Özel")
                    }
                }.awaitAll()
            }

            assertThat(ids.toSet()).hasSize(1)
            val categories = kotlinx.coroutines.withContext(Dispatchers.IO) {
                database.productDao().observeCategories().first()
            }
            assertThat(categories).hasSize(1)
            assertThat(categories.single().name).isEqualTo("Özel")
        } finally {
            database.close()
        }
    }
}
