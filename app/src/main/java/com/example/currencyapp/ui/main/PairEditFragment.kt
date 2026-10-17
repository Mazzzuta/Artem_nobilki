package com.example.currencyapp.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.checkbox.MaterialCheckBox
import com.example.currencyapp.R
import com.example.currencyapp.data.local.entity.FavoritePairEntity
import com.example.currencyapp.databinding.FragmentPairEditBinding
import com.example.currencyapp.ui.viewmodel.AppViewModel
import com.example.currencyapp.utils.CurrencyMath

class PairEditFragment : Fragment(R.layout.fragment_pair_edit) {
    private val model: AppViewModel by activityViewModels { AppViewModel.Factory(requireActivity().application) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentPairEditBinding.bind(view)
        val id = arguments?.getLong("id") ?: 0L
        var original: FavoritePairEntity? = null
        var initialized = false
        val codes = (model.data.value.currencies.map { it.code } + model.data.value.pairs.flatMap { listOf(it.fromCode, it.toCode) } + listOf("RUB", "USD", "EUR", "CNY", "GBP", "JPY", "KZT")).distinct().sorted()
        b.from.items(codes, codes.indexOf("USD"))
        b.to.items(codes, codes.indexOf(model.preferences.values.value.base))
        b.cancel.setOnClickListener { parentFragmentManager.popBackStack() }
        observe(model.data) { state ->
            if (!state.loaded || initialized) return@observe
            initialized = true
            original = state.pairs.find { it.id == id }
            if (id != 0L && original == null) { message("Пара уже удалена"); parentFragmentManager.popBackStack(); return@observe }
            val pair = original
            if (savedInstanceState == null) {
                b.name.setText(pair?.title.orEmpty())
                b.amount.setText(amountText(pair?.defaultAmount ?: 1.0))
                b.note.setText(pair?.note.orEmpty())
                pair?.let { b.from.setSelection(codes.indexOf(it.fromCode)); b.to.setSelection(codes.indexOf(it.toCode)) }
            }
            val selected = savedInstanceState?.getLongArray("categories")?.toSet()
                ?: state.links.filter { it.pairId == id }.map { it.categoryId }.toSet()
            state.categories.forEach { category ->
                b.categoryChecks.addView(MaterialCheckBox(requireContext()).apply {
                    text = category.title; tag = category.categoryId; isChecked = category.categoryId in selected
                })
            }
            b.categoryHint.visibility = if (state.categories.isEmpty()) View.VISIBLE else View.GONE
        }
        b.save.setOnClickListener {
            val amount = CurrencyMath.amount(b.amount.text.toString())
            b.nameLayout.error = if (b.name.text.isNullOrBlank()) "Введите название" else null
            b.amountLayout.error = if (amount == null) "Введите положительную сумму до 1 трлн" else null
            if (amount == null || b.name.text.isNullOrBlank()) { message("Проверьте выделенные поля"); return@setOnClickListener }
            if (!initialized) return@setOnClickListener
            val selected = (0 until b.categoryChecks.childCount).map { b.categoryChecks.getChildAt(it) as MaterialCheckBox }
                .filter { it.isChecked }.map { it.tag as Long }
            val pair = FavoritePairEntity(id, b.name.text.toString(), b.from.selectedItem.toString(), b.to.selectedItem.toString(), amount,
                b.note.text.toString(), original?.createdAt ?: System.currentTimeMillis())
            perform(b.save, b.progress) {
                model.repository.savePair(pair, selected)
                parentFragmentManager.popBackStack()
            }
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        view?.let { view ->
            val checks = FragmentPairEditBinding.bind(view).categoryChecks
            outState.putLongArray("categories", (0 until checks.childCount).map { checks.getChildAt(it) as MaterialCheckBox }
                .filter { it.isChecked }.map { it.tag as Long }.toLongArray())
        }
        super.onSaveInstanceState(outState)
    }
    companion object { fun newInstance(id: Long) = PairEditFragment().apply { arguments = Bundle().apply { putLong("id", id) } } }
}
