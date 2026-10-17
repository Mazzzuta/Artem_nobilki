package com.example.currencyapp.data.local.dao

import androidx.room.*
import com.example.currencyapp.data.local.entity.CurrencyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {

    @Upsert
    suspend fun insertCurrency(currency: CurrencyEntity)

    @Upsert
    suspend fun insertCurrencies(currencies: List<CurrencyEntity>)

    @Update
    suspend fun updateCurrency(currency: CurrencyEntity)

    @Delete
    suspend fun deleteCurrency(currency: CurrencyEntity)
    @Query("SELECT * FROM currencies ORDER BY code ASC")
    fun getAllCurrencies(): Flow<List<CurrencyEntity>>
    @Query("SELECT * FROM currencies WHERE code = :code LIMIT 1")
    fun getCurrencyByCode(code: String): Flow<CurrencyEntity?>
    @Query("SELECT * FROM currencies WHERE code LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' ORDER BY code ASC")
    fun searchCurrencies(query: String): Flow<List<CurrencyEntity>>
    @Query("""
        SELECT c.* FROM currencies c
        INNER JOIN currency_category_cross_ref ref ON c.code = ref.currencyCode
        WHERE ref.categoryId = :categoryId
        ORDER BY c.code ASC
    """)
    fun getCurrenciesByCategory(categoryId: Long): Flow<List<CurrencyEntity>>
    @Query("SELECT * FROM currencies ORDER BY name ASC")
    fun getAllCurrenciesSortedByNameAsc(): Flow<List<CurrencyEntity>>
    @Query("SELECT * FROM currencies ORDER BY currentRate DESC")
    fun getAllCurrenciesSortedByRateDesc(): Flow<List<CurrencyEntity>>
    @Query("SELECT * FROM currencies ORDER BY currentRate ASC")
    fun getAllCurrenciesSortedByRateAsc(): Flow<List<CurrencyEntity>>
}