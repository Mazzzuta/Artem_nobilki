package com.example.currencyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_pairs")
data class FavoritePairEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val fromCode: String,
    val toCode: String,
    val defaultAmount: Double = 1.0,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
