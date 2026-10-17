package com.example.currencyapp.ui.main

import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import com.example.currencyapp.R
import java.text.SimpleDateFormat
import java.util.*

fun <T> Fragment.observe(flow: Flow<T>, action: (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) { flow.collect { action(it) } }
    }
}
fun Spinner.items(values: List<String>, selected: Int = 0, action: ((Int) -> Unit)? = null) {
    onItemSelectedListener = null
    adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, values).apply {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }
    if (values.isNotEmpty()) setSelection(selected.coerceIn(values.indices), false)
    if (action != null) onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { action(position) }
        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
    }
}
fun Fragment.message(text: String) = Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
fun Fragment.perform(button: View, progress: View? = null, block: suspend () -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        button.isEnabled = false
        progress?.visibility = View.VISIBLE
        try { block() }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { if (isAdded) message(error.localizedMessage ?: "Не удалось сохранить изменения") }
        finally { button.isEnabled = true; progress?.visibility = View.GONE }
    }
}
fun Fragment.open(fragment: Fragment) {
    parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).addToBackStack(null).commit()
}
fun dateText(timestamp: Long): String = if (timestamp <= 0) "дата неизвестна" else SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(Date(timestamp))
fun number(value: Double): String = String.format(Locale("ru"), "%,.2f", value)

fun amountText(value: Double): String = String.format(Locale("ru"), "%.2f", value)
