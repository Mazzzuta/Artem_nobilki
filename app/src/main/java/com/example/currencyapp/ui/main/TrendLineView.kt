package com.example.currencyapp.ui.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.currencyapp.R

class TrendLineView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.holo_blue_dark)
        strokeWidth = 6f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val fillPaint = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.holo_blue_light)
        alpha = 40
        style = Paint.Style.FILL
    }

    private var points: List<Float> = listOf(20f, 45f, 30f, 60f, 50f, 75f, 65f, 90f)

    fun setData(newPoints: List<Float>) {
        points = newPoints
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (points.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()
        val stepX = width / (points.size - 1).coerceAtLeast(1)

        val minVal = points.minOrNull() ?: 0f
        val maxVal = points.maxOrNull() ?: 1f
        val range = (maxVal - minVal).coerceAtLeast(1f)

        val path = Path()
        val fillPath = Path()

        fillPath.moveTo(0f, height)

        points.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value - minVal) / range * (height * 0.8f) + height * 0.1f)

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(path, paint)
    }
}