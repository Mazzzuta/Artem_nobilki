package com.example.currencyapp.utils

import java.math.BigDecimal
import java.math.RoundingMode

object CurrencyMath {
    fun amount(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()
        ?.takeIf { it.isFinite() && it > 0 && it <= 1_000_000_000_000.0 }

    // Курсы хранятся в рублях за единицу валюты, без номинала ЦБ.
    fun convert(amount: Double, fromRub: Double, toRub: Double): Double {
        require(amount.isFinite() && amount > 0 && amount <= 1_000_000_000_000.0)
        require(fromRub.isFinite() && fromRub > 0 && toRub.isFinite() && toRub > 0)
        val result = BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(fromRub))
            .divide(BigDecimal.valueOf(toRub), 8, RoundingMode.HALF_UP).toDouble()
        require(result.isFinite() && result > 0) { "Сумма слишком мала для расчёта" }
        return result
    }
}
