package com.example.currencyapp.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.currencyapp.R
import com.example.currencyapp.databinding.FragmentConverterBinding
import com.example.currencyapp.ui.viewmodel.AppViewModel
import com.example.currencyapp.utils.CurrencyMath

class ConverterFragment : Fragment(R.layout.fragment_converter) {
    private val model: AppViewModel by activityViewModels { AppViewModel.Factory(requireActivity().application) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentConverterBinding.bind(view)
        val historyAdapter = com.example.currencyapp.ui.adapters.RecordAdapter()
        b.history.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        b.history.adapter = historyAdapter
        val pairId = arguments?.getLong("id")?.takeIf { it > 0 }
        var codes = emptyList<String>()
        var initialized = false
        b.swap.setOnClickListener { val from = b.from.selectedItemPosition; b.from.setSelection(b.to.selectedItemPosition); b.to.setSelection(from) }
        b.convert.setOnClickListener {
            val amount = CurrencyMath.amount(b.amount.text.toString())
            b.amountLayout.error = if (amount == null) "Введите положительную сумму до 1 трлн" else null
            if (amount == null) { message("Проверьте сумму"); return@setOnClickListener }
            val from = b.from.selectedItem?.toString()
            val to = b.to.selectedItem?.toString()
            if (from == null || to == null) { message("Обновите курсы во вкладке «Валюты»"); return@setOnClickListener }
            perform(b.convert, b.progress) {
                val operation = model.repository.convert(amount, from, to, pairId)
                val result = "${number(amount)} $from = ${number(operation.result)} $to\nКурс ЦБ на ${dateText(operation.rateDate)}\nСохранено в историю"
                b.result.text = result
                b.resultCard.visibility = View.VISIBLE
            }
        }
        observe(model.status) { status -> b.rateInfo.text = status.message }
        observe(model.data) { state ->
            if (!state.loaded) return@observe
            val pair = state.pairs.find { it.id == pairId }
            b.pairName.text = pair?.title ?: "Обмен по официальным курсам ЦБ"
            val newCodes = (state.currencies.map { it.code } + listOfNotNull(pair?.fromCode, pair?.toCode)).distinct().sorted()
            if (newCodes != codes) {
                val from = b.from.selectedItem?.toString() ?: pair?.fromCode ?: "USD"
                val to = b.to.selectedItem?.toString() ?: pair?.toCode ?: model.preferences.values.value.base
                codes = newCodes
                b.from.items(codes, codes.indexOf(from))
                b.to.items(codes, codes.indexOf(to))
            }
            if (!initialized) {
                if (savedInstanceState == null) b.amount.setText(amountText(pair?.defaultAmount ?: 1.0))
                initialized = true
            }
            b.convert.isEnabled = state.currencies.isNotEmpty()
            val lastDate = state.currencies.maxOfOrNull { it.lastUpdated }
            if (lastDate != null) b.rateInfo.text = "Курс ЦБ на ${dateText(lastDate)} · доступен офлайн"
            b.historyEmpty.visibility = if (state.history.isEmpty()) View.VISIBLE else View.GONE
            historyAdapter.submitList(state.history.take(20).map {
                com.example.currencyapp.ui.adapters.RecordRow(it.id.toString(),
                    "${number(it.amount)} ${it.fromCurrency} → ${number(it.result)} ${it.toCurrency}",
                    "${dateText(it.timestamp)}\nКурс: ${number(it.rate)} · ЦБ на ${dateText(it.rateDate)}")
            })
        }
    }
    companion object { fun newInstance(id: Long) = ConverterFragment().apply { arguments = Bundle().apply { putLong("id", id) } } }
}
