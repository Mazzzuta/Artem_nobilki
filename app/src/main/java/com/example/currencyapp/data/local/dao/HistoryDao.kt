package com.example.currencyapp.data.local.dao

import androidx.room.*
import com.example.currencyapp.data.local.entity.ConversionHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ConversionHistoryEntity): Long

    @Delete
    suspend fun deleteHistoryItem(history: ConversionHistoryEntity)

    @Query("DELETE FROM conversion_history")
    suspend fun clearAllHistory()

    @Query("SELECT * FROM conversion_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ConversionHistoryEntity>>

    @Query("SELECT * FROM conversion_history WHERE fromCurrency = :code OR toCurrency = :code ORDER BY timestamp DESC")
    fun getHistoryByCurrency(code: String): Flow<List<ConversionHistoryEntity>>
}