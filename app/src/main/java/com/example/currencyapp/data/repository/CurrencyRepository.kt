package com.example.currencyapp.data.repository

import com.example.currencyapp.data.local.dao.*
import com.example.currencyapp.data.local.entity.*
import com.example.currencyapp.data.remote.api.CurrencyApiService
import com.example.currencyapp.data.remote.dto.HistoricalRatesDto
import com.example.currencyapp.data.remote.mapper.toEntityList
import com.example.currencyapp.data.remote.util.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Единый источник правды (Single Source of Truth).
 * Обширный репозиторий, связывающий локальную Room БД и сетевой API (Retrofit).
 */
class CurrencyRepository(
    private val apiService: CurrencyApiService,
    private val currencyDao: CurrencyDao,
    private val categoryDao: CategoryDao,
    private val historyDao: HistoryDao,
    private val crossRefDao: CrossRefDao
) {

    // ==========================================
    // --- СЕТЕВОЙ СЛОЙ И ОФЛАЙН-РЕЖИМ (SSOT) ---
    // ==========================================

    /**
     * Запрашивает свежие курсы с API и обновляет Room БД.
     * При сетевой ошибке достает локальный кэш из Room и возвращает статус Error.
     */
    fun refreshCurrencies(base: String = "USD"): Flow<NetworkResult<List<CurrencyEntity>>> = flow {
        emit(NetworkResult.Loading())
        try {
            val response = apiService.getLatestRates(base)
            if (response.isSuccessful && response.body() != null) {
                val entities = response.body()!!.toEntityList()
                currencyDao.insertCurrencies(entities)
                val updatedData = currencyDao.getAllCurrencies().first()
                emit(NetworkResult.Success(updatedData))
            } else {
                val cachedData = currencyDao.getAllCurrencies().first()
                emit(
                    NetworkResult.Error(
                        message = "Ошибка сервера: ${response.code()}",
                        data = cachedData
                    )
                )
            }
        } catch (e: Exception) {
            val cachedData = currencyDao.getAllCurrencies().first()
            emit(
                NetworkResult.Error(
                    message = "Отсутствует подключение к сети. Отображаются данные из кэша.",
                    data = cachedData
                )
            )
        }
    }

    /**
     * Получение исторических данных для графиков и аналитики.
     */
    suspend fun getHistoricalRates(
        startDate: String,
        endDate: String,
        base: String = "USD",
        target: String? = null
    ): NetworkResult<HistoricalRatesDto> {
        return try {
            val response = apiService.getHistoricalRates(startDate, endDate, base, target)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Ошибка получения исторических данных: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Сеть недоступна: ${e.localizedMessage ?: "Ошибка сети"}")
        }
    }

    // ==========================================
    // --- ЛОКАЛЬНАЯ РАБОТА С ВАЛЮТАМИ (ROOM) ---
    // ==========================================

    fun getAllCurrencies(): Flow<List<CurrencyEntity>> = currencyDao.getAllCurrencies()

    fun searchCurrencies(query: String): Flow<List<CurrencyEntity>> = currencyDao.searchCurrencies(query)

    fun getCurrenciesByCategory(categoryId: Long): Flow<List<CurrencyEntity>> =
        currencyDao.getCurrenciesByCategory(categoryId)

    fun getSortedCurrencies(sortBy: SortOrder): Flow<List<CurrencyEntity>> {
        return when (sortBy) {
            SortOrder.NAME_ASC -> currencyDao.getAllCurrenciesSortedByNameAsc()
            SortOrder.RATE_DESC -> currencyDao.getAllCurrenciesSortedByRateDesc()
            SortOrder.RATE_ASC -> currencyDao.getAllCurrenciesSortedByRateAsc()
            SortOrder.CODE_ASC -> currencyDao.getAllCurrencies()
        }
    }

    suspend fun saveCurrency(currency: CurrencyEntity) = currencyDao.insertCurrency(currency)

    suspend fun updateCurrency(currency: CurrencyEntity) = currencyDao.updateCurrency(currency)

    suspend fun deleteCurrency(currency: CurrencyEntity) = currencyDao.deleteCurrency(currency)

    // ==========================================
    // --- КАТЕГОРИИ И СВЯЗИ N:M ---
    // ==========================================

    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun addCategory(category: CategoryEntity): Long = categoryDao.insertCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)

    suspend fun addCurrencyToCategory(currencyCode: String, categoryId: Long) {
        crossRefDao.insertCrossRef(CurrencyCategoryCrossRef(currencyCode, categoryId))
    }

    suspend fun removeCurrencyFromCategory(currencyCode: String, categoryId: Long) {
        crossRefDao.removeCurrencyFromCategory(currencyCode, categoryId)
    }

    // ==========================================
    // --- ИСТОРИЯ КОНВЕРТАЦИЙ ---
    // ==========================================

    fun getConversionHistory(): Flow<List<ConversionHistoryEntity>> = historyDao.getAllHistory()

    suspend fun addConversionHistory(history: ConversionHistoryEntity): Long = historyDao.insertHistory(history)

    suspend fun clearHistory() = historyDao.clearAllHistory()
}

/**
 * Варианты сортировки списка валют
 */
enum class SortOrder {
    CODE_ASC,
    NAME_ASC,
    RATE_DESC,
    RATE_ASC
}