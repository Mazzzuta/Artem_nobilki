package com.example.currencyapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO для ответа Frankfurter API по актуальным курсам (/latest)[cite: 1].
 */
data class CurrencyResponseDto(
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("base")
    val base: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("rates")
    val rates: Map<String, Double>
)