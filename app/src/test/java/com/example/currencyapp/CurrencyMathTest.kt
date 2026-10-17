package com.example.currencyapp

import com.example.currencyapp.utils.CurrencyMath
import org.junit.Assert.*
import org.junit.Test

class CurrencyMathTest {
    @Test fun moneyIsDisplayedWithTwoDecimalsWithoutReducingCalculationPrecision() {
        assertTrue(com.example.currencyapp.ui.main.number(12542.655).endsWith(",66"))
        assertEquals("150,00", com.example.currencyapp.ui.main.amountText(150.0))
        assertEquals(0.01234568, CurrencyMath.convert(1.0, 1.0, 81.0), 0.000000001)
    }
    @Test fun convertsViaRublesAndSupportsSameCurrency() {
        assertEquals(900.0, CurrencyMath.convert(10.0, 90.0, 1.0), 0.00000001)
        assertEquals(10.0, CurrencyMath.convert(900.0, 1.0, 90.0), 0.00000001)
        assertEquals(9.0, CurrencyMath.convert(10.0, 90.0, 100.0), 0.00000001)
        assertEquals(123.45, CurrencyMath.convert(123.45, 90.0, 90.0), 0.00000001)
    }
    @Test fun acceptsCommaAndRejectsInvalidOrUnboundedAmounts() {
        assertEquals(12.5, CurrencyMath.amount("12,5")!!, 0.0)
        listOf("", "word", "NaN", "Infinity", "0", "-1", "1e30").forEach { assertNull(it, CurrencyMath.amount(it)) }
    }
    @Test fun invalidRatesCannotProduceAnOperation() {
        listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY).forEach { invalid ->
            assertThrows(IllegalArgumentException::class.java) { CurrencyMath.convert(1.0, invalid, 1.0) }
            assertThrows(IllegalArgumentException::class.java) { CurrencyMath.convert(1.0, 1.0, invalid) }
        }
    }
}
