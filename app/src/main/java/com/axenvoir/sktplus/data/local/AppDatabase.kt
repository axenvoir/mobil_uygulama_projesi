package com.axenvoir.sktplus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ProductEntity::class, CategoryEntity::class, NoteEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun noteDao(): NoteDao

    companion object {
        const val NAME = "skt_plus.db"

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE products ADD COLUMN storageLocation TEXT NOT NULL DEFAULT ''"
                )
            }
        }
    }
}
