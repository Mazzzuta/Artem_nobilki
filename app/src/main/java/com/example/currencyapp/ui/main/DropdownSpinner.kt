package com.example.currencyapp.ui.main

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.widget.doAfterTextChanged
import com.example.currencyapp.R
import com.example.currencyapp.data.local.entity.CurrencyEntity
import java.util.Currency
import java.util.Locale
import kotlin.math.min

fun currencyLabel(code: String, name: String? = null): String {
    val russianName = name?.takeIf { it.any { letter -> letter in 'А'..'я' || letter == 'ё' || letter == 'Ё' } }
        ?: runCatching { Currency.getInstance(code).getDisplayName(Locale("ru")) }.getOrDefault(code)
    return "$code — $russianName"
}

fun matchingCurrencyIndices(labels: List<String>, query: String): List<Int> {
    fun normalize(value: String) = value.lowercase(Locale.ROOT).replace('ё', 'е')
    val words = normalize(query).trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return labels.indices.filter { index -> words.all { normalize(labels[index]).contains(it) } }
}

fun DropdownSpinner.currencyItems(codes: List<String>, currencies: List<CurrencyEntity>, selected: Int = 0, action: ((Int) -> Unit)? = null) {
    currencyLabels = codes.map { code -> currencyLabel(code, currencies.find { it.code == code }?.name) }
    items(codes, selected, action)
}

class DropdownSpinner @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatSpinner(context, attrs) {
    var currencyLabels: List<String>? = null
    private var dropdownPopup: ListPopupWindow? = null

    override fun performClick(): Boolean {
        val choices = adapter ?: return false
        if (choices.count == 0) return false
        dropdownPopup?.dismiss()
        val labels = currencyLabels ?: List(choices.count) { choices.getItem(it).toString() }
        var indices = labels.indices.toList()
        val density = resources.displayMetrics.density
        val frame = Rect().also { getWindowVisibleDisplayFrame(it) }
        val popupWidth = min(width, ((if (currencyLabels != null) 320 else 200) * density).toInt()).coerceAtMost(frame.width())
        val dividerHeight = density.toInt().coerceAtLeast(1)
        var rowHeight = 0
        val popupAdapter = object : ArrayAdapter<String>(context, R.layout.item_currency_option, labels.toMutableList()) {
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View =
                super.getView(position, convertView, parent).apply {
                    layoutParams = android.widget.AbsListView.LayoutParams(android.widget.AbsListView.LayoutParams.MATCH_PARENT, rowHeight.takeIf { it > 0 } ?: android.widget.AbsListView.LayoutParams.WRAP_CONTENT)
                }
        }
        rowHeight = labels.indices.maxOf { position ->
            popupAdapter.getView(position, null, this).run {
                measure(View.MeasureSpec.makeMeasureSpec((popupWidth - 16 * density).toInt().coerceAtLeast(1), View.MeasureSpec.EXACTLY), View.MeasureSpec.UNSPECIFIED)
                measuredHeight
            }
        }
        val popup = ListPopupWindow(context).apply {
            anchorView = this@DropdownSpinner
            setAdapter(popupAdapter)
            width = popupWidth
            isModal = true
            inputMethodMode = if (currencyLabels != null) ListPopupWindow.INPUT_METHOD_NEEDED else ListPopupWindow.INPUT_METHOD_NOT_NEEDED
            setOnItemClickListener { _, _, position, _ ->
                this@DropdownSpinner.setSelection(indices[position])
                dismiss()
            }
        }
        val search = if (currencyLabels != null) EditText(context).apply {
            hint = "Название или код валюты"
            contentDescription = "Поиск валюты"
            isSingleLine = true
            textSize = 16f
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            setPadding((16 * density).toInt(), 0, (16 * density).toInt(), 0)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (56 * density).toInt())
        } else null
        val empty = android.widget.TextView(context).apply {
            text = "Валюты не найдены"
            gravity = android.view.Gravity.CENTER
        }
        fun resize() {
            val padding = Rect()
            popup.background?.getPadding(padding)
            popup.height = rowHeight * min(4, indices.size.coerceAtLeast(1)) + dividerHeight * (min(4, indices.size) - 1).coerceAtLeast(0) + padding.top + padding.bottom + if (search != null) (56 * density).toInt() else 0
        }
        if (search != null) {
            popup.setOnDismissListener {
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager).hideSoftInputFromWindow(search.windowToken, 0)
            }
            popup.setPromptView(search)
            popup.promptPosition = ListPopupWindow.POSITION_PROMPT_ABOVE
            search.doAfterTextChanged { query ->
                indices = matchingCurrencyIndices(labels, query.toString())
                popupAdapter.setNotifyOnChange(false)
                popupAdapter.clear()
                popupAdapter.addAll(indices.map { labels[it] })
                popupAdapter.notifyDataSetChanged()
                resize()
                popup.show()
                popup.listView?.setSelection(0)
            }
        }
        dropdownPopup = popup
        resize()
        popup.show()
        popup.listView?.apply {
            divider = android.graphics.drawable.ColorDrawable(com.google.android.material.color.MaterialColors.getColor(this@DropdownSpinner, com.google.android.material.R.attr.colorOutlineVariant))
            this.dividerHeight = dividerHeight
            if (search != null) {
                (parent as? android.view.ViewGroup)?.addView(empty, android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, rowHeight))
                emptyView = empty
            }
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