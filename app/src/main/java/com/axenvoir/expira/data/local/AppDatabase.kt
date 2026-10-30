package com.axenvoir.expira.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        NoteEntity::class,
        SupportWalletEntity::class,
        SupportRewardTransactionEntity::class,
        SupportCosmeticUnlockEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun noteDao(): NoteDao
    abstract fun supportRewardDao(): SupportRewardDao

    companion object {
        const val NAME = "skt_plus.db"

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE products ADD COLUMN storageLocation TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `support_wallet` (
                        `id` INTEGER NOT NULL,
                        `balance` INTEGER NOT NULL,
                        `lifetimeEarnedPoints` INTEGER NOT NULL,
                        `supporterBadgeUnlocked` INTEGER NOT NULL,
                        `legacyPointsImported` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `support_reward_transactions` (
                        `eventId` TEXT NOT NULL,
                        `pointsDelta` INTEGER NOT NULL,
                        `kind` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`eventId`)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `support_cosmetic_unlocks` (
                        `cosmeticId` TEXT NOT NULL,
                        `unlockedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`cosmeticId`)
                    )
                    """.trimIndent(),
                )
            }
        }
    }
}
