package com.example.currencyapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Промежуточная связующая таблица для реализации связи «Многие-ко-Многим» (N:M)
 * между Валютами и Категориями.
 * Таблица: currency_category_cross_ref
 */
@Entity(
    tableName = "currency_category_cross_ref",
    primaryKeys = ["currencyCode", "categoryId"], // Составной первичный ключ
    foreignKeys = [
        ForeignKey(
            entity = CurrencyEntity::class,
            parentColumns = ["code"],
            childColumns = ["currencyCode"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["currencyCode"]),
        Index(value = ["categoryId"])
    ]
)
data class CurrencyCategoryCrossRef(
    val currencyCode: String,
    val categoryId: Long
)