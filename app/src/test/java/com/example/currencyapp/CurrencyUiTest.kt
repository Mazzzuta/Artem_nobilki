package com.example.currencyapp

import android.content.Context
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import com.example.currencyapp.data.AppPreferences
import com.example.currencyapp.ui.main.*
import com.github.mikephil.charting.charts.BarChart
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CurrencyUiTest {
    @Test fun searchMatchesRussianNamesCodesAndWords() {
        val labels = listOf(currencyLabel("EUR", "Евро"), currencyLabel("RUB", "Российский рубль"), currencyLabel("USD", "Доллар США"))
        assertEquals(listOf(2), matchingCurrencyIndices(labels, "долл"))
        assertEquals(listOf(2), matchingCurrencyIndices(labels, " usd "))
        assertEquals(listOf(1), matchingCurrencyIndices(labels, "РОССИЙСК руб"))
        assertEquals(listOf(0, 1, 2), matchingCurrencyIndices(labels, " "))
        assertTrue(matchingCurrencyIndices(labels, "несуществующая").isEmpty())
        assertEquals(listOf(0), matchingCurrencyIndices(listOf("Ёмкая"), "емк"))
        assertTrue(currencyLabel("USD").lowercase().contains("доллар"))
        assertTrue(currencyLabel("RUB").lowercase().contains("руб"))
    }

    @Test fun selectingFilteredCurrencyKeepsOriginalCode() {
        val controller = org.robolectric.Robolectric.buildActivity(android.app.Activity::class.java).setup().visible()
        try {
            val spinner = DropdownSpinner(ContextThemeWrapper(controller.get(), R.style.Theme_CurrencyApp))
            spinner.currencyItems(listOf("EUR", "RUB", "USD"), emptyList())
            controller.get().setContentView(spinner)
            spinner.measure(View.MeasureSpec.makeMeasureSpec(900, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(160, View.MeasureSpec.EXACTLY))
            spinner.layout(0, 0, 900, 160)
            assertTrue(spinner.performClick())
            val popup = org.robolectric.Shadows.shadowOf(RuntimeEnvironment.getApplication() as android.app.Application).latestPopupWindow
            fun descendants(view: View): List<View> = listOf(view) + if (view is android.view.ViewGroup) (0 until view.childCount).flatMap { descendants(view.getChildAt(it)) } else emptyList()
            val views = descendants(popup.contentView)
            val search = views.filterIsInstance<android.widget.EditText>().single()
            val list = views.filterIsInstance<android.widget.ListView>().single()
            search.setText("доллар")
            assertEquals(1, list.adapter.count)
            assertTrue(list.adapter.getItem(0).toString().startsWith("USD"))
            list.performItemClick(list.adapter.getView(0, null, list), 0, 0)
            assertEquals("USD", spinner.selectedItem)
        } finally { controller.pause().stop().destroy() }
    }

    @Test fun chartKeepsAllDataButOnlySixColumnsVisible() {
        val context = ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_CurrencyApp)
        for (count in listOf(1, 6, 12, 24)) {
            val chart = BarChart(context)
            chart.measure(View.MeasureSpec.makeMeasureSpec(900, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(780, View.MeasureSpec.EXACTLY))
            chart.layout(0, 0, 900, 780)
            chart.showConversions(List(count) { "USD→X$it" to (it + 1) }, 0xffaa88cc.toInt(), 0xffffffff.toInt())
            assertEquals(count, chart.data.entryCount)
            assertEquals("2", chart.data.getDataSetByIndex(0).valueFormatter.getFormattedValue(2f))
            assertTrue(chart.visibleXRange <= 6.01f)
            assertEquals("USD→X0", chart.xAxis.valueFormatter.getFormattedValue(0f))
            assertEquals(1f, chart.xAxis.granularity)
        }
    }

    @Test fun profileSurvivesRestartAndRegistrationDateDoesNotChange() {
        val context = RuntimeEnvironment.getApplication()
        val store = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        store.edit().clear().commit()
        val before = System.currentTimeMillis()
        val prefs = AppPreferences(context)
        val registration = prefs.registeredAt
        assertTrue(registration in before..System.currentTimeMillis())
        val birthday = java.util.Calendar.getInstance().apply { clear(); set(1965, 4, 12) }.timeInMillis
        prefs.save(prefs.values.value.copy(fullName = "Иванов Иван Иванович", birthDate = birthday))
        val restored = AppPreferences(context)
        assertEquals(registration, restored.registeredAt)
        assertEquals("Иванов Иван Иванович", restored.values.value.fullName)
        assertEquals(birthday, restored.values.value.birthDate)
        restored.save(restored.values.value.copy(base = "USD"))
        assertEquals(registration, AppPreferences(context).registeredAt)
        store.edit().clear().commit()
    }
}