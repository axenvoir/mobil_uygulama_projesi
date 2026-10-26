package com.axenvoir.sktplus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ProductEntity::class, CategoryEntity::class, NoteEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun noteDao(): NoteDao

    companion object {
        const val NAME = "skt_plus.db"
    }
}
