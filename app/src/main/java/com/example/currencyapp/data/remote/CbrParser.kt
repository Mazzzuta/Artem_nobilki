package com.example.currencyapp.data.remote

import com.example.currencyapp.data.local.entity.CurrencyEntity
import com.example.currencyapp.data.local.entity.RatePointEntity
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.util.Locale

object CbrParser {
    private data class Row(val id: String, val date: String, val values: Map<String, String>) {
        fun text(name: String) = values[name] ?: error("В ответе ЦБ отсутствует $name")
        fun rate(): Double = (decimal(text("Value")) / decimal(text("Nominal"))).also { require(it.isFinite() && it > 0) }
    }

    fun daily(bytes: ByteArray): List<CurrencyEntity> {
        val (dateText, rows) = records(bytes, "Valute")
        val date = date(dateText)
        require(rows.isNotEmpty()) { "ЦБ вернул пустой список валют" }
        val rates = rows.map { row ->
            val code = row.text("CharCode")
            require(code.matches(Regex("[A-Z]{3}"))) { "Некорректный код валюты" }
            CurrencyEntity(code = code, name = row.text("Name"), currentRate = row.rate(), symbol = code,
                lastUpdated = date, source = "CBR", cbrId = row.id)
        }
        require(rates.map { it.code }.distinct().size == rates.size) { "Повторяющиеся валюты в ответе ЦБ" }
        return rates + CurrencyEntity(code = "RUB", name = "Российский рубль", currentRate = 1.0, symbol = "₽", lastUpdated = date)
    }

    fun history(bytes: ByteArray, code: String): List<RatePointEntity> = records(bytes, "Record").second.map {
        RatePointEntity(code, date(it.date), it.rate())
    }.sortedBy { it.date }

    private fun records(bytes: ByteArray, recordName: String): Pair<String, List<Row>> {
        require(bytes.size <= 2_000_000) { "Ответ ЦБ слишком велик" }
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(ByteArrayInputStream(bytes), null)
        var rootDate = ""
        var validRoot = false
        var id = ""
        var recordDate = ""
        var field = ""
        var value = StringBuilder()
        var values = mutableMapOf<String, String>()
        val rows = mutableListOf<Row>()
        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                // DOM-парсер Android не поддерживает отключение DTD через настройку Apache.
                XmlPullParser.DOCDECL -> error("DTD в ответе ЦБ запрещён")
                XmlPullParser.START_TAG -> when (parser.depth) {
                    1 -> {
                        require(parser.name == "ValCurs") { "Неизвестный формат ответа ЦБ" }
                        validRoot = true
                        rootDate = parser.getAttributeValue(null, "Date").orEmpty()
                    }
                    2 -> {
                        require(parser.name == recordName) { "Неожиданная запись в ответе ЦБ" }
                        id = parser.getAttributeValue(null, "ID").orEmpty()
                        recordDate = parser.getAttributeValue(null, "Date").orEmpty()
                        values = mutableMapOf()
                    }
                    3 -> { field = parser.name; value = StringBuilder() }
                }
                XmlPullParser.TEXT, XmlPullParser.CDSECT, XmlPullParser.ENTITY_REF -> {
                    if (parser.eventType == XmlPullParser.ENTITY_REF) require(parser.name in listOf("amp", "lt", "gt", "apos", "quot")) { "XML-сущности запрещены" }
                    if (parser.depth == 3) value.append(parser.text.orEmpty())
                }
                XmlPullParser.END_TAG -> when (parser.depth) {
                    3 -> values[field] = value.toString().trim()
                    2 -> rows.add(Row(id, recordDate, values.toMap()))
                }
            }
            parser.nextToken()
        }
        require(validRoot) { "Пустой ответ ЦБ" }
        return rootDate to rows
    }

    private fun decimal(value: String) = value.replace(" ", "").replace("\u00a0", "").replace(',', '.').toDouble().also {
        require(it.isFinite() && it > 0) { "Некорректное значение курса или номинала" }
    }
    private fun date(value: String): Long = SimpleDateFormat("dd.MM.yyyy", Locale.ROOT).apply { isLenient = false }
        .parse(value)?.time ?: error("Некорректная дата ЦБ")
}
