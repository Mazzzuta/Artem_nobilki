package com.example.currencyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность для хранения информации о валюте.
 * Таблица: currencies
 */
@Entity(tableName = "currencies")
data class CurrencyEntity(
    @PrimaryKey
    val code: String, // Первичный ключ (например, "USD", "EUR", "RUB")
    val name: String, // Полное название (например, "US Dollar")
    val currentRate: Double, // Текущий курс к базовой валюте
    val symbol: String, // Символ валюты (например, "$", "€", "₽")
    @androidx.room.ColumnInfo(defaultValue = "'LEGACY'") val source: String = "CBR",
    @androidx.room.ColumnInfo(defaultValue = "''") val cbrId: String = "",
    val lastUpdated: Long // Время последнего обновления (Unix timestamp, ms)
)