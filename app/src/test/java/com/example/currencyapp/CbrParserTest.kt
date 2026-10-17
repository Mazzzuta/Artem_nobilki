package com.example.currencyapp

import com.example.currencyapp.data.remote.CbrParser
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CbrParserTest {
    @Test fun normalizesNominalAndHonorsWindows1251Encoding() {
        val xml = """<?xml version="1.0" encoding="windows-1251"?><ValCurs Date="17.10.2026"><Valute ID="R01820"><CharCode>JPY</CharCode><Nominal>100</Nominal><Name>Японских иен</Name><Value>60,5000</Value></Valute></ValCurs>"""
        val rates = CbrParser.daily(xml.toByteArray(charset("windows-1251")))
        assertEquals("Японских иен", rates.first().name)
        assertEquals(0.605, rates.first().currentRate, 0.0000001)
        assertEquals("R01820", rates.first().cbrId)
        assertEquals(1.0, rates.last().currentRate, 0.0)
        assertEquals("RUB", rates.last().code)
    }
    @Test fun historyUsesActualDatesAndNominals() {
        val xml = """<ValCurs><Record Date="16.10.2026"><Nominal>100</Nominal><Value>62,0</Value></Record><Record Date="15.10.2026"><Nominal>100</Nominal><Value>60,0</Value></Record></ValCurs>"""
        val points = CbrParser.history(xml.toByteArray(), "JPY")
        assertTrue(points[0].date < points[1].date)
        assertEquals(0.6, points[0].rubPerUnit, 0.0)
        assertEquals(0.62, points[1].rubPerUnit, 0.0)
    }
    @Test fun rejectsDtdHtmlEmptyAndInvalidNominal() {
        val inputs = listOf("<html/>", "<ValCurs Date=\"17.10.2026\"/>", "<!DOCTYPE ValCurs [<!ENTITY secret SYSTEM \"file:///private\">]><ValCurs/>", "<ValCurs Date=\"17.10.2026\"><Valute><CharCode>USD</CharCode><Nominal>0</Nominal><Name>Dollar</Name><Value>90</Value></Valute></ValCurs>")
        inputs.forEach { xml -> assertThrows(Exception::class.java) { CbrParser.daily(xml.toByteArray()) } }
    }
}
