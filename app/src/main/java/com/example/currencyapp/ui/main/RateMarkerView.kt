package com.example.currencyapp.ui.main

import android.content.Context
import android.widget.TextView
import com.example.currencyapp.R
import com.example.currencyapp.data.local.entity.RatePointEntity
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class RateMarkerView(context: Context, private val points: List<RatePointEntity>, private val code: String) : MarkerView(context, R.layout.view_rate_marker) {
    override fun refreshContent(entry: Entry?, highlight: Highlight?) {
        val point = entry?.let { points.getOrNull(it.x.toInt()) } ?: return
        findViewById<TextView>(R.id.markerText).text = "${dateText(point.date)}\n1 $code = ${number(point.rubPerUnit)} RUB"
        super.refreshContent(entry, highlight)
    }
    override fun getOffset() = MPPointF(-width / 2f, -height.toFloat() - 8 * resources.displayMetrics.density)
}
