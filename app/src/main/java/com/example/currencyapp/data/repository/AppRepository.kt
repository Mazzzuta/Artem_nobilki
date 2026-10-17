package com.example.currencyapp.data.repository

import androidx.room.withTransaction
import com.example.currencyapp.data.local.AppDatabase
import com.example.currencyapp.data.local.entity.*
import com.example.currencyapp.data.remote.CbrClient
import com.example.currencyapp.utils.CurrencyMath
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

class AppRepository(private val db: AppDatabase, private val cbr: CbrClient = CbrClient()) {
    val currencies = db.currencyDao().getAllCurrencies()
    val pairs = db.pairDao().observePairs()
    val categories = db.categoryDao().getAllCategories()
    val links = db.pairDao().observeLinks()
    val history = db.historyDao().getAllHistory()

    suspend fun refresh() {
        val rates = cbr.latest()
        db.withTransaction {
            db.currencyDao().insertCurrencies(rates)
            db.ratePointDao().save(rates.map { RatePointEntity(it.code, it.lastUpdated, it.currentRate) })
        }
    }

    suspend fun savePair(pair: FavoritePairEntity, categoryIds: List<Long>): Long {
        require(pair.title.isNotBlank() && pair.title.length <= 100) { "Введите название до 100 символов" }
        require(pair.note.length <= 1000) { "Заметка должна быть не длиннее 1000 символов" }
        require(pair.fromCode.matches(Regex("[A-Z]{3}")) && pair.toCode.matches(Regex("[A-Z]{3}"))) { "Выберите валюты" }
        require(pair.defaultAmount.isFinite() && pair.defaultAmount > 0 && pair.defaultAmount <= 1_000_000_000_000.0) { "Введите положительную сумму до 1 трлн" }
        return db.pairDao().save(pair.copy(title = pair.title.trim(), note = pair.note.trim()), categoryIds)
    }

    suspend fun deletePair(id: Long) = db.pairDao().delete(id)
    suspend fun addCategory(title: String) {
        val name = title.trim()
        require(name.isNotBlank() && name.length <= 50) { "Введите название категории до 50 символов" }
        db.withTransaction {
            require(db.categoryDao().getAllCategories().first().none { it.title.equals(name, true) }) { "Такая категория уже есть" }
            db.categoryDao().insertCategory(CategoryEntity(title = name, colorHex = "#2563EB"))
        }
    }
    suspend fun deleteCategory(category: CategoryEntity) = db.categoryDao().deleteCategory(category)

    suspend fun convert(amount: Double, from: String, to: String, pairId: Long?): ConversionHistoryEntity {
        return db.withTransaction {
            val rates = db.currencyDao().getAllCurrencies().first().filter { it.source == "CBR" }.associateBy { it.code }
            val fromRate = rates[from] ?: error("Нет курса $from. Обновите курсы во вкладке «Валюты»")
            val toRate = rates[to] ?: error("Нет курса $to. Обновите курсы во вкладке «Валюты»")
            check(fromRate.lastUpdated == toRate.lastUpdated) { "Даты курсов различаются. Обновите курсы" }
            val result = CurrencyMath.convert(amount, fromRate.currentRate, toRate.currentRate)
            val operation = ConversionHistoryEntity(fromCurrency = from, toCurrency = to, amount = amount,
                result = result, timestamp = System.currentTimeMillis(),
                pairId = pairId?.takeIf { db.pairDao().find(it) != null },
                rate = CurrencyMath.convert(1.0, fromRate.currentRate, toRate.currentRate), rateDate = fromRate.lastUpdated)
            operation.copy(id = db.historyDao().insertHistory(operation))
        }
    }

    suspend fun chart(currency: CurrencyEntity, start: Long, end: Long): Pair<List<RatePointEntity>, String?> {
        return try {
            val data = cbr.history(currency, start, end)
            check(data.isNotEmpty()) { "ЦБ не вернул историю за этот период" }
            db.ratePointDao().save(data)
            data to null
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            val cached = db.ratePointDao().get(currency.code, start, end)
            cached to if (cached.isEmpty()) "История недоступна: ${error.localizedMessage}" else "Нет связи с ЦБ. Показана сохранённая история"
        }
    }
}
