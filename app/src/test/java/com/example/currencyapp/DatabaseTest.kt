package com.example.currencyapp

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.example.currencyapp.data.local.AppDatabase
import com.example.currencyapp.data.local.Migrations
import com.example.currencyapp.data.local.entity.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()
    @Test fun conversionPersistsActualRateAndWorksFromLocalCache() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            db.currencyDao().insertCurrencies(listOf(
                CurrencyEntity("USD", "Доллар", 90.0, "$", lastUpdated = 200),
                CurrencyEntity("RUB", "Рубль", 1.0, "₽", lastUpdated = 200)))
            val repository = com.example.currencyapp.data.repository.AppRepository(db)
            val id = repository.savePair(FavoritePairEntity(title = "Поездка", fromCode = "USD", toCode = "RUB"), emptyList())
            val operation = repository.convert(10.0, "USD", "RUB", id)
            assertEquals(900.0, operation.result, 0.0)
            assertEquals(90.0, operation.rate, 0.0)
            assertEquals(200L, operation.rateDate)
            assertEquals(id, operation.pairId)
            assertEquals(1, db.historyDao().getAllHistory().first().size)
            db.currencyDao().insertCurrency(CurrencyEntity("USD", "Доллар", 91.0, "$", lastUpdated = 300))
            try { repository.convert(10.0, "USD", "RUB", id); fail("Mixed quote dates must fail") }
            catch (expected: IllegalStateException) { }
            assertEquals(1, db.historyDao().getAllHistory().first().size)
        } finally { db.close() }
    }

    @Test fun migrationPreservesLegacyDataAndValidatesRoomSchema() = runBlocking {
        val name = "migration-test.db"
        context.deleteDatabase(name)
        val helper = FrameworkSQLiteOpenHelperFactory().create(SupportSQLiteOpenHelper.Configuration.builder(context).name(name)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE currencies (code TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, currentRate REAL NOT NULL, symbol TEXT NOT NULL, lastUpdated INTEGER NOT NULL)")
                    db.execSQL("CREATE TABLE categories (categoryId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, colorHex TEXT NOT NULL)")
                    db.execSQL("CREATE TABLE conversion_history (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, fromCurrency TEXT NOT NULL, toCurrency TEXT NOT NULL, amount REAL NOT NULL, result REAL NOT NULL, timestamp INTEGER NOT NULL)")
                    db.execSQL("CREATE TABLE currency_category_cross_ref (currencyCode TEXT NOT NULL, categoryId INTEGER NOT NULL, PRIMARY KEY(currencyCode, categoryId), FOREIGN KEY(currencyCode) REFERENCES currencies(code) ON DELETE CASCADE, FOREIGN KEY(categoryId) REFERENCES categories(categoryId) ON DELETE CASCADE)")
                    db.execSQL("CREATE INDEX index_currency_category_cross_ref_currencyCode ON currency_category_cross_ref(currencyCode)")
                    db.execSQL("CREATE INDEX index_currency_category_cross_ref_categoryId ON currency_category_cross_ref(categoryId)")
                    db.execSQL("INSERT INTO currencies VALUES ('USD', 'Dollar', 1, '$', 100)")
                    db.execSQL("INSERT INTO categories VALUES (1, 'Travel', '#FFFFFF')")
                    db.execSQL("INSERT INTO currency_category_cross_ref VALUES ('USD', 1)")
                    db.execSQL("INSERT INTO conversion_history VALUES (1, 'USD', 'EUR', 10, 9, 100)")
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            }).build())
        helper.writableDatabase
        helper.close()
        val db = Room.databaseBuilder(context, AppDatabase::class.java, name).addMigrations(Migrations.FROM_1_TO_2).build()
        try {
            assertEquals("LEGACY", db.currencyDao().getAllCurrencies().first().single().source)
            val old = db.historyDao().getAllHistory().first().single()
            assertEquals(9.0, old.result, 0.0)
            assertEquals(0.9, old.rate, 0.00001)
            assertNull(old.pairId)
            assertEquals(1, db.categoryDao().getAllCategories().first().size)
            assertTrue(db.crossRefDao().isCurrencyInCategory("USD", 1).first())
            db.currencyDao().insertCurrency(CurrencyEntity("USD", "Доллар", 90.0, "$", lastUpdated = 200))
            assertTrue("upsert must preserve category links", db.crossRefDao().isCurrencyInCategory("USD", 1).first())
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun pairCrudIsAtomicAndForeignKeysPreserveHistory() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val category = db.categoryDao().insertCategory(CategoryEntity(title = "Поездки", colorHex = "#123456"))
            val pair = FavoritePairEntity(title = "Доллары", fromCode = "USD", toCode = "RUB")
            val id = db.pairDao().save(pair, listOf(category))
            assertEquals("Доллары", db.pairDao().find(id)!!.title)
            db.pairDao().save(pair.copy(id = id, title = "Обновлено"), listOf(category))
            assertEquals("Обновлено", db.pairDao().find(id)!!.title)
            try {
                db.pairDao().save(pair.copy(id = id, title = "Не сохранять"), listOf(99999))
                fail("Invalid foreign key must reject the transaction")
            } catch (expected: android.database.sqlite.SQLiteConstraintException) { }
            assertEquals("Обновлено", db.pairDao().find(id)!!.title)
            assertEquals(1, db.pairDao().observeLinks().first().size)
            db.historyDao().insertHistory(ConversionHistoryEntity(fromCurrency = "USD", toCurrency = "RUB", amount = 1.0, result = 90.0, timestamp = 1, pairId = id, rate = 90.0, rateDate = 1))
            db.pairDao().delete(id)
            assertNull(db.pairDao().find(id))
            assertTrue(db.pairDao().observeLinks().first().isEmpty())
            assertNull(db.historyDao().getAllHistory().first().single().pairId)
        } finally { db.close() }
    }
}
