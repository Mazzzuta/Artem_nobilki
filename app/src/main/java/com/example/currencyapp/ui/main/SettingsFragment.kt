package com.example.currencyapp.ui.main

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.currencyapp.R
import com.example.currencyapp.databinding.FragmentSettingsBinding
import com.example.currencyapp.ui.viewmodel.AppViewModel

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private val model: AppViewModel by activityViewModels { AppViewModel.Factory(requireActivity().application) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentSettingsBinding.bind(view)
        b.fullName.setText(model.preferences.values.value.fullName)
        b.fullName.doAfterTextChanged { name ->
            model.preferences.save(model.preferences.values.value.copy(fullName = name.toString().trim()))
        }
        b.registeredAt.text = dateText(model.preferences.registeredAt)
        fun updateBirthday() {
            b.birthDate.text = model.preferences.values.value.birthDate.takeIf { it != Long.MIN_VALUE }?.let {
                java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale("ru")).format(java.util.Date(it))
            } ?: "Указать дату рождения"
        }
        updateBirthday()
        b.birthDate.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val birthday = model.preferences.values.value.birthDate
            if (birthday != Long.MIN_VALUE) calendar.timeInMillis = birthday else calendar.add(java.util.Calendar.YEAR, -18)
            val picker = android.app.DatePickerDialog(requireContext(), { _, year, month, day ->
                val date = java.util.Calendar.getInstance().apply { clear(); set(year, month, day) }.timeInMillis
                model.preferences.save(model.preferences.values.value.copy(birthDate = date))
                updateBirthday()
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))
            picker.datePicker.maxDate = System.currentTimeMillis()
            picker.datePicker.minDate = java.util.Calendar.getInstance().apply { clear(); set(1900, 0, 1) }.timeInMillis
            picker.setButton(android.content.DialogInterface.BUTTON_NEUTRAL, "Очистить") { _, _ ->
                model.preferences.save(model.preferences.values.value.copy(birthDate = Long.MIN_VALUE))
                updateBirthday()
            }
            picker.show()
        }
        val themes = com.example.currencyapp.data.AppTheme.values().toList()
        b.theme.items(listOf("Светлая", "Тёмная", "Системная"), themes.indexOf(model.preferences.values.value.theme)) { index ->
            val theme = themes[index]
            if (model.preferences.values.value.theme != theme) {
                model.preferences.save(model.preferences.values.value.copy(theme = theme))
                AppCompatDelegate.setDefaultNightMode(theme.nightMode)
            }
        }
        b.sort.items(listOf("Сначала новые", "По названию", "Сначала старые"), model.preferences.values.value.sort) {
            model.preferences.save(model.preferences.values.value.copy(sort = it))
        }
        observe(model.data) { state ->
            val codes = (state.currencies.map { it.code } + listOf("RUB", "USD", "EUR", "CNY", model.preferences.values.value.base)).distinct().sorted()
            b.base.currencyItems(codes, state.currencies, codes.indexOf(model.preferences.values.value.base)) {
                model.preferences.save(model.preferences.values.value.copy(base = codes[it]))
            }
        }
    }
}
