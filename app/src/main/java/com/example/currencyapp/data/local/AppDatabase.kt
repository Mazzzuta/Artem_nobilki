package com.example.currencyapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.currencyapp.data.local.dao.*
import com.example.currencyapp.data.local.entity.*

@Database(
    entities = [
        CurrencyEntity::class,
        CategoryEntity::class,
        ConversionHistoryEntity::class,
        CurrencyCategoryCrossRef::class,
        FavoritePairEntity::class, PairCategoryCrossRef::class, RatePointEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun currencyDao(): CurrencyDao
    abstract fun categoryDao(): CategoryDao
    abstract fun historyDao(): HistoryDao
    abstract fun pairDao(): PairDao
    abstract fun ratePointDao(): RatePointDao
    abstract fun crossRefDao(): CrossRefDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "currency_tracker_database"
                )
                    .addMigrations(Migrations.FROM_1_TO_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}