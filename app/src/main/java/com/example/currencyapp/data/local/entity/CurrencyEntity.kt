package com.example.currencyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currencies")
data class CurrencyEntity(
    @PrimaryKey
    val code: String,
    val name: String,
    val currentRate: Double,
    val symbol: String,
    @androidx.room.ColumnInfo(defaultValue = "'LEGACY'") val source: String = "CBR",
    @androidx.room.ColumnInfo(defaultValue = "''") val cbrId: String = "",
    val lastUpdated: Long
)