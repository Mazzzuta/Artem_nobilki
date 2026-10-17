package com.example.currencyapp.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val FROM_1_TO_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS favorite_pairs (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, fromCode TEXT NOT NULL, toCode TEXT NOT NULL, defaultAmount REAL NOT NULL, note TEXT NOT NULL, createdAt INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS pair_categories (pairId INTEGER NOT NULL, categoryId INTEGER NOT NULL, PRIMARY KEY(pairId, categoryId), FOREIGN KEY(pairId) REFERENCES favorite_pairs(id) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(categoryId) REFERENCES categories(categoryId) ON UPDATE NO ACTION ON DELETE CASCADE)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_pair_categories_categoryId ON pair_categories(categoryId)")
            db.execSQL("CREATE TABLE IF NOT EXISTS rate_points (code TEXT NOT NULL, date INTEGER NOT NULL, rubPerUnit REAL NOT NULL, PRIMARY KEY(code, date))")
            // Rebuild the history to add a nullable FK without discarding old operations.
            db.execSQL("CREATE TABLE history_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, fromCurrency TEXT NOT NULL, toCurrency TEXT NOT NULL, amount REAL NOT NULL, result REAL NOT NULL, timestamp INTEGER NOT NULL, pairId INTEGER, rate REAL NOT NULL, rateDate INTEGER NOT NULL, FOREIGN KEY(pairId) REFERENCES favorite_pairs(id) ON UPDATE NO ACTION ON DELETE SET NULL)")
            db.execSQL("INSERT INTO history_new SELECT id, fromCurrency, toCurrency, amount, result, timestamp, NULL, CASE WHEN amount > 0 THEN result / amount ELSE 0 END, 0 FROM conversion_history")
            db.execSQL("DROP TABLE conversion_history")
            db.execSQL("ALTER TABLE history_new RENAME TO conversion_history")
            db.execSQL("CREATE INDEX index_conversion_history_pairId ON conversion_history(pairId)")
            // Old quotes used a different base; keep them, but never present them as CBR quotes.
            db.execSQL("ALTER TABLE currencies ADD COLUMN source TEXT NOT NULL DEFAULT 'LEGACY'")
            db.execSQL("ALTER TABLE currencies ADD COLUMN cbrId TEXT NOT NULL DEFAULT ''")
        }
    }
}
