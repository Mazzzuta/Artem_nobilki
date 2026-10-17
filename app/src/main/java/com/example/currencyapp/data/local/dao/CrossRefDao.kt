package com.example.currencyapp.data.local.dao

import androidx.room.*
import com.example.currencyapp.data.local.entity.CurrencyCategoryCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface CrossRefDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: CurrencyCategoryCrossRef)

    @Delete
    suspend fun deleteCrossRef(crossRef: CurrencyCategoryCrossRef)

    @Query("DELETE FROM currency_category_cross_ref WHERE currencyCode = :currencyCode AND categoryId = :categoryId")
    suspend fun removeCurrencyFromCategory(currencyCode: String, categoryId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM currency_category_cross_ref WHERE currencyCode = :currencyCode AND categoryId = :categoryId)")
    fun isCurrencyInCategory(currencyCode: String, categoryId: Long): Flow<Boolean>
}