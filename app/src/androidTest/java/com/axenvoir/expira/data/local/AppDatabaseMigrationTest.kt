package com.axenvoir.expira.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import java.io.File
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
            db.execSQL("CREATE TABLE categories (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, isDefault INTEGER NOT NULL)")
            db.execSQL("CREATE UNIQUE INDEX index_categories_name ON categories(name)")
            db.execSQL("CREATE TABLE products (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, categoryId INTEGER NOT NULL, expiryEpochDay INTEGER NOT NULL, quantity INTEGER NOT NULL, createdAt INTEGER NOT NULL, FOREIGN KEY(categoryId) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
            db.execSQL("CREATE INDEX index_products_categoryId ON products(categoryId)")
            db.execSQL("CREATE INDEX index_products_expiryEpochDay ON products(expiryEpochDay)")
            db.execSQL("CREATE TABLE notes (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, content TEXT NOT NULL, isPinned INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
            db.execSQL("PRAGMA user_version = 1")
        }
    }

    @After
    fun tearDown() {
        context.deleteDatabase("migration-test.db")
    }

    @Test
    fun migration_1_to_2_preserves_products_and_adds_storage_location() = runBlocking {
        SQLiteDatabase.openDatabase(databaseFile.path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            db.execSQL("INSERT INTO categories(id, name, isDefault) VALUES (1, 'Gıda', 1)")
            db.execSQL("INSERT INTO products(id, name, categoryId, expiryEpochDay, quantity, createdAt) VALUES (7, 'Süt', 1, 30000, 2, 1234)")
        }

        Room.databaseBuilder(context, AppDatabase::class.java, "migration-test.db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
            .use { db ->
                val cursor = db.openHelper.readableDatabase.query("PRAGMA table_info(products)")
                val columns = buildList {
                    cursor.use {
                        val nameIndex = it.getColumnIndexOrThrow("name")
                        while (it.moveToNext()) add(it.getString(nameIndex))
                    }
                }
                assertThat(columns).contains("storageLocation")
                val product = db.productDao().observeByIdWithCategory(7L).first()
                assertThat(product).isNotNull()
                assertThat(product?.storageLocation).isEqualTo("")
            }
    }
}
