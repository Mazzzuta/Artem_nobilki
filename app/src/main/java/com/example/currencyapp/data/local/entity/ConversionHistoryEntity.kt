package com.example.currencyapp.data.local.entity

import androidx.room.*

@Entity(
    tableName = "conversion_history",
    foreignKeys = [ForeignKey(entity = FavoritePairEntity::class, parentColumns = ["id"], childColumns = ["pairId"], onDelete = ForeignKey.SET_NULL)],
    indices = [Index("pairId")]
)
data class ConversionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromCurrency: String,
    val toCurrency: String,
    val amount: Double,
    val result: Double,
    val timestamp: Long,
    val pairId: Long? = null,
    val rate: Double = 0.0,
    val rateDate: Long = 0
)
