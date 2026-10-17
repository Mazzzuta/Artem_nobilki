package com.example.currencyapp.data.remote

import com.example.currencyapp.data.local.entity.CurrencyEntity
import com.example.currencyapp.data.local.entity.RatePointEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CbrClient {
    private val client = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS).callTimeout(35, TimeUnit.SECONDS).build()
    private fun fetch(path: String): ByteArray = client.newCall(Request.Builder().url("https://www.cbr.ru/scripts/$path").build()).execute().use { response ->
        check(response.isSuccessful) { "ЦБ: HTTP ${response.code}" }
        val body = response.body ?: error("ЦБ вернул пустой ответ")
        check(body.contentLength() <= 2_000_000) { "Ответ ЦБ слишком велик" }
        val source = body.source()
        source.request(2_000_001)
        check(source.buffer.size <= 2_000_000) { "Ответ ЦБ слишком велик" }
        source.readByteArray()
    }
    suspend fun latest(): List<CurrencyEntity> = withContext(Dispatchers.IO) { CbrParser.daily(fetch("XML_daily.asp")) }
    suspend fun history(currency: CurrencyEntity, start: Long, end: Long): List<RatePointEntity> = withContext(Dispatchers.IO) {
        if (currency.code == "RUB") listOf(RatePointEntity("RUB", start, 1.0), RatePointEntity("RUB", end, 1.0))
        else {
            require(currency.cbrId.matches(Regex("R[0-9A-Z]+"))) { "Нет идентификатора валюты ЦБ" }
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
            CbrParser.history(fetch("XML_dynamic.asp?date_req1=${format.format(Date(start))}&date_req2=${format.format(Date(end))}&VAL_NM_RQ=${currency.cbrId}"), currency.code)
        }
    }
}
