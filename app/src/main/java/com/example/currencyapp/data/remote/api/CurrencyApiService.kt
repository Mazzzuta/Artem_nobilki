package com.example.currencyapp.data.remote.api

import com.example.currencyapp.data.remote.dto.CurrencyResponseDto
import com.example.currencyapp.data.remote.dto.HistoricalRatesDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit интерфейс запросов к Frankfurter API (https://api.frankfurter.app/)[cite: 1].
 */
interface CurrencyApiService {

    /**
     * Запрос актуальных курсов валют[cite: 1].
     * Пример: GET /latest?from=USD
     */
    @GET("latest")
    suspend fun getLatestRates(
        @Query("from") base: String = "USD"
    ): Response<CurrencyResponseDto>

    /**
     * Запрос исторических данных за период для аналитики/графиков[cite: 1].
     * Пример: GET /2026-01-01..2026-01-31?from=USD&to=EUR
     */
    @GET("{start_date}..{end_date}")
    suspend fun getHistoricalRates(
        @Path("start_date") startDate: String,
        @Path("end_date") endDate: String,
        @Query("from") base: String = "USD",
        @Query("to") target: String? = null
    ): Response<HistoricalRatesDto>
}