package com.example.currencyapp.data.local.dao

import androidx.room.*
import com.example.currencyapp.data.local.entity.RatePointEntity

@Dao
interface RatePointDao {
    @Upsert suspend fun save(points: List<RatePointEntity>)
    @Query("SELECT * FROM rate_points WHERE code = :code AND date BETWEEN :start AND :end ORDER BY date")
    suspend fun get(code: String, start: Long, end: Long): List<RatePointEntity>
}
