package com.example.currencyapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO для ответа Frankfurter API по историческим курсам за период для графиков[cite: 1].
 */
data class HistoricalRatesDto(
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("base")
    val base: String,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("rates")
    val rates: Map<String, Map<String, Double>>
)