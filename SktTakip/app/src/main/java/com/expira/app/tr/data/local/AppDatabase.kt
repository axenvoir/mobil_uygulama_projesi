package com.expira.app.tr.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Database(entities = [ProductEntity::class, NoteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expira_native_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.productDao(), database.noteDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(productDao: ProductDao, noteDao: NoteDao) {
            val today = LocalDate.now()
            val sampleProducts = listOf(
                ProductEntity(
                    name = "Süt (Whole Milk)",
                    categoryId = "dairy",
                    expiryEpochDay = today.toEpochDay(), // Today!
                    quantity = "2 Adet",
                    note = "Buzdolabı orta rafında saklayın"
                ),
                ProductEntity(
                    name = "Tavuk Göğsü (Chicken Breast)",
                    categoryId = "meat",
                    expiryEpochDay = today.plusDays(2).toEpochDay(), // Approaching
                    quantity = "750g",
                    note = "Dondurucuya aktarılabilir"
                ),
                ProductEntity(
                    name = "Yoğurt (Greek Yogurt)",
                    categoryId = "dairy",
                    expiryEpochDay = today.minusDays(1).toEpochDay(), // Expired
                    quantity = "1 Kutu",
                    note = "Kokusu ve dokusu kontrol edilmeli"
                ),
                ProductEntity(
                    name = "Kaşar Peyniri (Cheese)",
                    categoryId = "dairy",
                    expiryEpochDay = today.plusDays(14).toEpochDay(), // Fresh
                    quantity = "400g",
                    note = "Açılmamış vakumlu paket"
                ),
                ProductEntity(
                    name = "Domates (Tomatoes)",
                    categoryId = "veggie",
                    expiryEpochDay = today.plusDays(4).toEpochDay(),
                    quantity = "1 kg",
                    note = "Tezgâhta muhafaza edilsin"
                )
            )
            productDao.insertAll(sampleProducts)

            val sampleNotes = listOf(
                NoteEntity(
                    title = "Haftalık Alışveriş Notları",
                    content = "• Zeytinyağı (Soğuk sıkım)\n• Tam buğday ekmeği\n• Filtre kahve çekirdeği\n• Maden suyu (6'lı paket)",
                    colorHex = "#267968",
                    isPinned = true
                ),
                NoteEntity(
                    title = "Dondurucu Düzeni & Plan",
                    content = "Üst çekmece: Sebzeler ve hazır garnitürler\nOrta çekmece: Kırmızı et ve tavuk\nAlt çekmece: Balık ve hamur işleri",
                    colorHex = "#3B82F6",
                    isPinned = false
                )
            )
            noteDao.insertAll(sampleNotes)
        }
    }
}
