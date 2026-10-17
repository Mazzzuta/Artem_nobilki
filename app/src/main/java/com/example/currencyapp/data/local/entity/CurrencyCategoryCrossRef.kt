package com.example.currencyapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "currency_category_cross_ref",
    primaryKeys = ["currencyCode", "categoryId"],
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