package com.example.currencyapp.ui.main

import android.os.Bundle
import android.view.View
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
        b.dark.isChecked = model.preferences.values.value.dark
        b.dark.setOnCheckedChangeListener { _, checked ->
            model.preferences.save(model.preferences.values.value.copy(dark = checked))
            AppCompatDelegate.setDefaultNightMode(if (checked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        }
        b.sort.items(listOf("Сначала новые", "По названию", "Сначала старые"), model.preferences.values.value.sort) {
            model.preferences.save(model.preferences.values.value.copy(sort = it))
        }
        observe(model.data) { state ->
            val codes = (state.currencies.map { it.code } + listOf("RUB", "USD", "EUR", "CNY", model.preferences.values.value.base)).distinct().sorted()
            b.base.items(codes, codes.indexOf(model.preferences.values.value.base)) {
                model.preferences.save(model.preferences.values.value.copy(base = codes[it]))
            }
        }
    }
}
