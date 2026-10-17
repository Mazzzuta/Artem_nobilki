package com.example.currencyapp.data.local.entity

import androidx.room.*

@Entity(
    tableName = "pair_categories",
    primaryKeys = ["pairId", "categoryId"],
    foreignKeys = [
        ForeignKey(entity = FavoritePairEntity::class, parentColumns = ["id"], childColumns = ["pairId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CategoryEntity::class, parentColumns = ["categoryId"], childColumns = ["categoryId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("categoryId")]
)
data class PairCategoryCrossRef(val pairId: Long, val categoryId: Long)
