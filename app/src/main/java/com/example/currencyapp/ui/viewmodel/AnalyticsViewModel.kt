package com.example.currencyapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyapp.data.remote.dto.HistoricalRatesDto
import com.example.currencyapp.data.remote.util.NetworkResult
import com.example.currencyapp.data.repository.CurrencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsViewModel(
    private val repository: CurrencyRepository
) : ViewModel() {

    private val _historicalData = MutableStateFlow<NetworkResult<HistoricalRatesDto>?>(null)
    val historicalData: StateFlow<NetworkResult<HistoricalRatesDto>?> = _historicalData.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow("MONTH") // "WEEK", "MONTH", "YEAR"
    val selectedTimeframe: StateFlow<String> = _selectedTimeframe.asStateFlow()

    // Вычисленные статистические показатели
    private val _minValue = MutableStateFlow(0.0)
    val minValue: StateFlow<Double> = _minValue.asStateFlow()

    private val _maxValue = MutableStateFlow(0.0)
    val maxValue: StateFlow<Double> = _maxValue.asStateFlow()

    private val _avgValue = MutableStateFlow(0.0)
    val avgValue: StateFlow<Double> = _avgValue.asStateFlow()

    // Настоящие точки для графика на основе исторических котировок
    private val _chartPoints = MutableStateFlow<List<Float>>(emptyList())
    val chartPoints: StateFlow<List<Float>> = _chartPoints.asStateFlow()

    fun loadChartData(base: String = "USD", target: String = "RUB", timeframe: String = "MONTH") {
        _selectedTimeframe.value = timeframe
        viewModelScope.launch {
            _historicalData.value = NetworkResult.Loading()

            val endDate = getCurrentDateFormatted()
            val startDate = getStartDateForTimeframe(timeframe)

            // Запрашиваем историю относительно USD (базовой валюты API)
            val result = repository.getHistoricalRates(startDate, endDate, "USD", "RUB")
            _historicalData.value = result

            if (result is NetworkResult.Success && result.data != null) {
                calculateCrossRateStatisticsAndPoints(result.data, base)
            }
        }
    }

    private fun calculateCrossRateStatisticsAndPoints(data: HistoricalRatesDto, baseCurrency: String) {
        val ratesList = mutableListOf<Double>()
        val pointsList = mutableListOf<Float>()

        // Сортируем даты по хронологии
        val sortedRates = data.rates.toSortedMap()

        sortedRates.forEach { (_, map) ->
            val rubRate = map["RUB"] ?: 1.0
            val baseRate = map[baseCurrency] ?: 1.0

            // Корректный кросс-курс выбранной валюты к рублю
            val crossRate = if (baseCurrency == "USD") {
                rubRate
            } else if (baseRate > 0) {
                rubRate / baseRate
            } else {
                0.0
            }

            if (crossRate > 0.0) {
                ratesList.add(crossRate)
                pointsList.add(crossRate.toFloat())
            }
        }

        if (ratesList.isNotEmpty()) {
            _minValue.value = ratesList.minOrNull() ?: 0.0
            _maxValue.value = ratesList.maxOrNull() ?: 0.0
            _avgValue.value = ratesList.average()
            _chartPoints.value = pointsList
        } else {
            _minValue.value = 0.0
            _maxValue.value = 0.0
            _avgValue.value = 0.0
            _chartPoints.value = listOf(0f, 0f)
        }
    }

    private fun getCurrentDateFormatted(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getStartDateForTimeframe(timeframe: String): String {
        val calendar = Calendar.getInstance()
        when (timeframe) {
            "WEEK" -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            "MONTH" -> calendar.add(Calendar.MONTH, -1)
            "YEAR" -> calendar.add(Calendar.YEAR, -1)
            else -> calendar.add(Calendar.MONTH, -1)
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(calendar.time)
    }
}