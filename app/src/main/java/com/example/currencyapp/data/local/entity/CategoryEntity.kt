package com.example.currencyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность для пользовательских категорий / списков валют (например, "Избранное", "Поездки").
 * Таблица: categories
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val categoryId: Long = 0, // Автогенерируемый ID
    val title: String, // Название категории
    val colorHex: String // HEX-код цвета категории (например, "#FF5722")
)