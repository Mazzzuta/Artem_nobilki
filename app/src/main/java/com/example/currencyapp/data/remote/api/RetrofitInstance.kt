package com.example.currencyapp.data.remote.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Синглтон для инициализации Retrofit клиента.
 */
object RetrofitInstance {
    private const val BASE_URL = "https://api.frankfurter.app/"

    val api: CurrencyApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CurrencyApiService::class.java)
    }
}