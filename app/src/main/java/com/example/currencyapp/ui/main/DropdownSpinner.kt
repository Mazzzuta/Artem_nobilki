package com.example.currencyapp.ui.main

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.ListPopupWindow
import kotlin.math.min

class DropdownSpinner @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatSpinner(context, attrs) {
    private var dropdownPopup: ListPopupWindow? = null

    override fun performClick(): Boolean {
        val choices = adapter ?: return false
        val count = choices.count
        if (count == 0) return false
        dropdownPopup?.dismiss()
        val popupAdapter = ArrayAdapter(context, com.example.currencyapp.R.layout.item_currency_option, List(count) { choices.getItem(it).toString() })
        val popupWidth = width.coerceAtMost((200 * resources.displayMetrics.density).toInt())
        val dividerHeight = resources.displayMetrics.density.toInt().coerceAtLeast(1)
        val row = popupAdapter.getView(0, null, this)
        row.measure(View.MeasureSpec.makeMeasureSpec(popupWidth, View.MeasureSpec.EXACTLY), View.MeasureSpec.UNSPECIFIED)
        val rowHeight = row.measuredHeight.coerceAtLeast((48 * resources.displayMetrics.density).toInt())
        val popup = ListPopupWindow(context).apply {
            anchorView = this@DropdownSpinner
            setAdapter(popupAdapter)
            width = popupWidth
            val padding = Rect()
            background?.getPadding(padding)
            height = rowHeight * min(6, count) + dividerHeight * (min(6, count) - 1) + padding.top + padding.bottom
            isModal = true
            setOnItemClickListener { _, _, position, _ ->
                this@DropdownSpinner.setSelection(position)
                dismiss()
            }
        }
        dropdownPopup = popup
        popup.show()
        popup.listView?.apply {
            divider = android.graphics.drawable.ColorDrawable(com.google.android.material.color.MaterialColors.getColor(this@DropdownSpinner, com.google.android.material.R.attr.colorOutlineVariant))
            this.dividerHeight = dividerHeight
            setSelection(selectedItemPosition)
        }
        return true
    }

    override fun onDetachedFromWindow() {
        dropdownPopup?.dismiss()
        dropdownPopup = null
        super.onDetachedFromWindow()
    }
}



