package com.example.currencyapp.ui.main

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currencyapp.R
import com.example.currencyapp.databinding.FragmentCurrenciesBinding
import com.example.currencyapp.ui.adapters.*
import com.example.currencyapp.ui.viewmodel.AppViewModel

class CurrenciesFragment : Fragment(R.layout.fragment_currencies) {
    private val model: AppViewModel by activityViewModels { AppViewModel.Factory(requireActivity().application) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentCurrenciesBinding.bind(view)
        val adapter = RecordAdapter()
        b.list.layoutManager = LinearLayoutManager(requireContext())
        b.list.adapter = adapter
        fun render() {
            val state = model.data.value
            val baseCode = model.preferences.values.value.base
            val base = state.currencies.find { it.code == baseCode }
            val query = b.search.text.toString()
            val rows = state.currencies.filter { it.code.contains(query, true) || it.name.contains(query, true) }
            adapter.submitList(rows.map { currency ->
                val quote = if (base != null && base.lastUpdated == currency.lastUpdated) "1 ${currency.code} = ${number(currency.currentRate / base.currentRate)} $baseCode" else "1 ${currency.code} = ${number(currency.currentRate)} RUB"
                RecordRow(currency.code, "${currency.code} · ${currency.name}", "$quote\nЦБ на ${dateText(currency.lastUpdated)}")
            })
            b.empty.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
            b.empty.text = if (state.currencies.isEmpty()) "Нет сохранённых курсов. Подключитесь к интернету и нажмите «Обновить курсы». Избранные пары доступны без сети." else "Валюта не найдена"
        }
        b.search.doAfterTextChanged { render() }
        b.refresh.setOnClickListener { model.refresh() }
        observe(model.data) { render() }
        observe(model.preferences.values) { render() }
        observe(model.status) { status ->
            b.progress.visibility = if (status.loading) View.VISIBLE else View.GONE
            b.refresh.isEnabled = !status.loading
            b.rateInfo.text = status.message
        }
    }
}
