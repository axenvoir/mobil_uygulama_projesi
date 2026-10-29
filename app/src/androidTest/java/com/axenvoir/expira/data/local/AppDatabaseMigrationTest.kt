package com.axenvoir.expira.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.axenvoir.expira.data.repository.ProductRepositoryImpl
import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    private lateinit var context: Context
    private lateinit var databaseFile: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        databaseFile = context.getDatabasePath("migration-test.db")
        databaseFile.parentFile?.mkdirs()
        databaseFile.delete()

        SQLiteDatabase.openOrCreateDatabase(databaseFile, null).use { db ->
            db.execSQL(
                "CREATE TABLE categories (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, isDefault INTEGER NOT NULL)"
            )
            db.execSQL("CREATE UNIQUE INDEX index_categories_name ON categories(name)")
            db.execSQL(
                "CREATE TABLE products (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, categoryId INTEGER NOT NULL, expiryEpochDay INTEGER NOT NULL, quantity INTEGER NOT NULL, createdAt INTEGER NOT NULL, FOREIGN KEY(categoryId) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE RESTRICT)"
            )
            db.execSQL("CREATE INDEX index_products_categoryId ON products(categoryId)")
            db.execSQL("CREATE INDEX index_products_expiryEpochDay ON products(expiryEpochDay)")
            db.execSQL(
                "CREATE TABLE notes (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, content TEXT NOT NULL, isPinned INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)"
            )
            db.execSQL("PRAGMA user_version = 1")
        }
    }

    @After
    fun tearDown() {
        context.deleteDatabase("migration-test.db")
    }

    @Test
    fun migration_1_to_2_preserves_data_and_schema() = runBlocking {
        SQLiteDatabase.openDatabase(databaseFile.path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            db.execSQL("INSERT INTO categories(id, name, isDefault) VALUES (1, 'Gıda', 1)")
            db.execSQL(
                "INSERT INTO products(id, name, categoryId, expiryEpochDay, quantity, createdAt) VALUES (7, 'Süt', 1, 30000, 2, 1234)"
            )
            db.execSQL(
                "INSERT INTO notes(id, title, content, isPinned, createdAt, updatedAt) VALUES (9, 'Not', 'İçerik', 1, 1111, 2222)"
            )
        }

        val db = Room.databaseBuilder(context, AppDatabase::class.java, "migration-test.db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
        try {
                val readableDb = db.openHelper.readableDatabase
                assertThat(readableDb.version).isEqualTo(2)

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
