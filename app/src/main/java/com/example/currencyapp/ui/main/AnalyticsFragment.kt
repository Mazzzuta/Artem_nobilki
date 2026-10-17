package com.example.currencyapp.ui.main

import android.os.Bundle
import android.graphics.Color
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.color.MaterialColors
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.example.currencyapp.R
import com.example.currencyapp.databinding.FragmentAnalyticsBinding
import com.example.currencyapp.ui.viewmodel.AppViewModel

class AnalyticsFragment : Fragment(R.layout.fragment_analytics) {
    private val model: AppViewModel by activityViewModels { AppViewModel.Factory(requireActivity().application) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentAnalyticsBinding.bind(view)
        var selectedCode = savedInstanceState?.getString("code") ?: model.chart.value.code.ifBlank { "USD" }
        var selectedDays = savedInstanceState?.getInt("days") ?: 30
        val textColor = MaterialColors.getColor(view, com.google.android.material.R.attr.colorOnSurface)
        val primary = MaterialColors.getColor(view, com.google.android.material.R.attr.colorPrimary)
        fun style(chart: Chart<*>) {
            chart.description.isEnabled = false
            chart.legend.textColor = textColor
            chart.setNoDataText("Данных пока нет")
            chart.setNoDataTextColor(textColor)
        }
        style(b.usageChart); style(b.rateChart)
        b.rateChart.legend.isEnabled = false
        b.rateChart.setExtraOffsets(12f, 8f, 24f, 16f)
        b.rateChart.xAxis.setAvoidFirstLastClipping(true)
        b.rateChart.xAxis.yOffset = 8f
        listOf(b.usageChart.axisLeft, b.usageChart.axisRight, b.rateChart.axisLeft, b.rateChart.axisRight).forEach { it.textColor = textColor }
        b.usageChart.axisLeft.axisMinimum = 0f
        b.usageChart.axisLeft.granularity = 1f
        b.usageChart.axisRight.isEnabled = false
        b.rateChart.axisRight.isEnabled = false
        b.rateChart.axisLeft.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float) = number(value.toDouble())
        }
        listOf(b.usageChart.xAxis, b.rateChart.xAxis).forEach { it.textColor = textColor; it.position = XAxis.XAxisPosition.BOTTOM; it.granularity = 1f }
        fun loadChart() {
            model.data.value.currencies.find { it.code == selectedCode }?.let { model.loadChart(it, selectedDays) }
        }
        b.period.items(listOf("7 дней", "30 дней", "365 дней"), listOf(7, 30, 365).indexOf(selectedDays)) { index ->
            val days = listOf(7, 30, 365)[index]
            if (days != selectedDays) { selectedDays = days; loadChart() }
        }
        b.reload.setOnClickListener { loadChart() }
        var codes = emptyList<String>()
        observe(model.data) { state ->
            b.summary.text = "Конвертаций: ${state.history.size}\nИзбранных пар: ${state.pairs.size}\nИспользовано направлений: ${state.history.map { it.fromCurrency to it.toCurrency }.distinct().size}"
            b.empty.visibility = if (state.history.isEmpty()) View.VISIBLE else View.GONE
            b.usageChart.visibility = if (state.history.isEmpty()) View.GONE else View.VISIBLE
            val grouped = state.history.groupingBy { "${it.fromCurrency}→${it.toCurrency}" }.eachCount().toList().sortedByDescending { it.second }
            val labels = grouped.map { it.first }
            b.usageChart.data = BarData(BarDataSet(grouped.mapIndexed { index, entry -> BarEntry(index.toFloat(), entry.second.toFloat()) }, "Конвертации").apply { color = primary; valueTextColor = textColor; valueTextSize = 12f })
            b.usageChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            b.usageChart.xAxis.labelRotationAngle = -35f
            b.usageChart.invalidate()
            b.usageTable.text = grouped.joinToString("\n") { "${it.first}: ${it.second}" }
            val newCodes = state.currencies.map { it.code }.sorted()
            if (newCodes != codes) {
                codes = newCodes
                if (selectedCode !in codes && codes.isNotEmpty()) selectedCode = codes.first()
                b.currency.items(codes, codes.indexOf(selectedCode)) { index ->
                    if (codes[index] != selectedCode) { selectedCode = codes[index]; loadChart() }
                }
                if (codes.isNotEmpty() && model.chart.value.points.isEmpty() && !model.chart.value.status.loading) loadChart()
            }
            if (codes.isEmpty()) b.chartStatus.text = "Обновите курсы во вкладке «Валюты», чтобы загрузить историю"
            b.reload.isEnabled = codes.isNotEmpty()
        }
        observe(model.chart) { chart ->
            b.progress.visibility = if (chart.status.loading) View.VISIBLE else View.GONE
            b.chartStatus.text = chart.status.message
            b.rateChart.clear()
            b.rateStats.visibility = View.GONE
            if (chart.points.isNotEmpty()) {
                val points = chart.points
                b.rateChart.data = LineData(LineDataSet(points.mapIndexed { index, point -> Entry(index.toFloat(), point.rubPerUnit.toFloat()) }, "1 ${chart.code} в RUB").apply {
                    color = primary; setCircleColor(primary); lineWidth = 2f; setDrawValues(false); setDrawCircles(points.size <= 31)
                })
                b.rateChart.xAxis.valueFormatter = IndexAxisValueFormatter(points.map { dateText(it.date) })
                b.rateChart.xAxis.setLabelCount(3, true)
                b.rateChart.marker = RateMarkerView(requireContext(), points, chart.code).apply { chartView = b.rateChart }
                b.rateChart.setHighlightPerTapEnabled(true)
                val values = points.map { it.rubPerUnit }
                b.rateStats.visibility = View.VISIBLE
                b.ratePair.text = "1 ${chart.code} в RUB"
                b.ratePeriod.text = "${dateText(points.first().date)} — ${dateText(points.last().date)}"
                b.minRate.text = number(values.minOrNull()!!)
                b.maxRate.text = number(values.maxOrNull()!!)
                b.averageRate.text = number(values.average())
                b.pointCount.text = points.size.toString()
            }
            b.rateChart.invalidate()
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        view?.let {
            val b = FragmentAnalyticsBinding.bind(it)
            outState.putString("code", b.currency.selectedItem?.toString())
            outState.putInt("days", listOf(7, 30, 365).getOrElse(b.period.selectedItemPosition) { 30 })
        }
        super.onSaveInstanceState(outState)
    }
}
