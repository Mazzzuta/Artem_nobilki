package com.example.currencyapp.data.remote.mapper

import com.example.currencyapp.data.local.entity.CurrencyEntity
import com.example.currencyapp.data.remote.dto.CurrencyResponseDto

/**
 * Локальный словарь наименований и символов валют для заполнения полей сущности Room[cite: 1, 2].
 */
private val currencyMetadata = mapOf(
    "USD" to Pair("US Dollar", "$"),
    "EUR" to Pair("Euro", "€"),
    "RUB" to Pair("Russian Ruble", "₽"),
    "GBP" to Pair("British Pound", "£"),
    "JPY" to Pair("Japanese Yen", "¥"),
    "CNY" to Pair("Chinese Yuan", "¥"),
    "CHF" to Pair("Swiss Franc", "CHF"),
    "CAD" to Pair("Canadian Dollar", "C$"),
    "AUD" to Pair("Australian Dollar", "A$"),
    "TRY" to Pair("Turkish Lira", "₺"),
    "KZT" to Pair("Kazakhstani Tenge", "₸")
)

/**
 * Преобразование CurrencyResponseDto в список сущностей CurrencyEntity для базы данных Room[cite: 1, 2].
 */
fun CurrencyResponseDto.toEntityList(): List<CurrencyEntity> {
    val currentTime = System.currentTimeMillis()
    val list = mutableListOf<CurrencyEntity>()

    // Добавляем базовую валюту (например, USD со значением 1.0)
    val baseMeta = currencyMetadata[base] ?: Pair(base, base)
    list.add(
        CurrencyEntity(
            code = base,
            name = baseMeta.first,
            currentRate = amount,
            symbol = baseMeta.second,
            lastUpdated = currentTime
        )
    )

    // Преобразуем карту курсов котируемых валют
    rates.forEach { (code, rate) ->
        val meta = currencyMetadata[code] ?: Pair(code, code)
        list.add(
            CurrencyEntity(
                code = code,
                name = meta.first,
                currentRate = rate,
                symbol = meta.second,
                lastUpdated = currentTime
            )
        )
    }

    return list
}