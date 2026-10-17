package com.example.currencyapp.data.local.entity

import androidx.room.Entity

@Entity(tableName = "rate_points", primaryKeys = ["code", "date"])
data class RatePointEntity(val code: String, val date: Long, val rubPerUnit: Double)
