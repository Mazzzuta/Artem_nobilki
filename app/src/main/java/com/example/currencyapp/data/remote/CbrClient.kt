package com.example.currencyapp.data.remote

import com.example.currencyapp.data.local.entity.CurrencyEntity
import com.example.currencyapp.data.local.entity.RatePointEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory

class CbrClient {
    private val client = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS).callTimeout(35, TimeUnit.SECONDS).build()

    private suspend fun xml(path: String) = withContext(Dispatchers.IO) {
        client.newCall(Request.Builder().url("https://www.cbr.ru/scripts/$path").build()).execute().use { response ->
            check(response.isSuccessful) { "ЦБ: HTTP ${response.code}" }
            val bytes = response.body?.bytes() ?: error("ЦБ вернул пустой ответ")
            val factory = DocumentBuilderFactory.newInstance().apply {
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                isExpandEntityReferences = false
            }
            factory.newDocumentBuilder().parse(ByteArrayInputStream(bytes)).documentElement
        }
    }

    suspend fun latest(): List<CurrencyEntity> {
        val root = xml("XML_daily.asp")
        check(root.tagName == "ValCurs") { "Неизвестный формат ответа ЦБ" }
        val date = parseDate(root.getAttribute("Date"))
        val nodes = root.getElementsByTagName("Valute")
        check(nodes.length > 0) { "Нет курсов в ответе ЦБ" }
        val rates = (0 until nodes.length).map { index ->
            val item = nodes.item(index) as Element
            val code = item.text("CharCode")
            require(code.matches(Regex("[A-Z]{3}"))) { "Некорректный код валюты" }
            val value = decimal(item.text("Value")) / decimal(item.text("Nominal"))
            require(value.isFinite() && value > 0) { "Некорректный курс ЦБ" }
            CurrencyEntity(code = code, name = item.text("Name"), currentRate = value,
                symbol = code, lastUpdated = date, source = "CBR", cbrId = item.getAttribute("ID"))
        }
        return rates + CurrencyEntity(code = "RUB", name = "Российский рубль", currentRate = 1.0,
            symbol = "₽", lastUpdated = date, source = "CBR")
    }

    suspend fun history(currency: CurrencyEntity, start: Long, end: Long): List<RatePointEntity> {
        if (currency.code == "RUB") return listOf(RatePointEntity("RUB", start, 1.0), RatePointEntity("RUB", end, 1.0))
        require(currency.cbrId.matches(Regex("R[0-9A-Z]+"))) { "Нет идентификатора валюты ЦБ" }
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
        val root = xml("XML_dynamic.asp?date_req1=${format.format(Date(start))}&date_req2=${format.format(Date(end))}&VAL_NM_RQ=${currency.cbrId}")
        check(root.tagName == "ValCurs") { "Неизвестный формат истории ЦБ" }
        val nodes = root.getElementsByTagName("Record")
        return (0 until nodes.length).map { index ->
            val item = nodes.item(index) as Element
            val value = decimal(item.text("Value")) / decimal(item.text("Nominal"))
            require(value.isFinite() && value > 0)
            RatePointEntity(currency.code, parseDate(item.getAttribute("Date")), value)
        }.sortedBy { it.date }
    }

    private fun Element.text(tag: String) = getElementsByTagName(tag).item(0)?.textContent?.trim()
        ?: error("В ответе ЦБ отсутствует $tag")
    private fun decimal(value: String) = value.replace(" ", "").replace("\u00a0", "").replace(',', '.')
        .toDouble().also { require(it.isFinite() && it > 0) }
    private fun parseDate(value: String): Long = SimpleDateFormat("dd.MM.yyyy", Locale.ROOT).apply {
        isLenient = false
    }.parse(value)?.time ?: error("Некорректная дата ЦБ")
}
