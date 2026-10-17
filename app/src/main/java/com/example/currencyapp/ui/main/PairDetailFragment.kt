package com.example.currencyapp.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.currencyapp.R
import com.example.currencyapp.databinding.FragmentPairDetailBinding
import com.example.currencyapp.ui.viewmodel.AppViewModel

class PairDetailFragment : Fragment(R.layout.fragment_pair_detail) {
    private val model: AppViewModel by activityViewModels { AppViewModel.Factory(requireActivity().application) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentPairDetailBinding.bind(view)
        val id = requireArguments().getLong("id")
        b.back.setOnClickListener { parentFragmentManager.popBackStack() }
        b.edit.setOnClickListener { open(PairEditFragment.newInstance(id)) }
        b.convert.setOnClickListener { open(ConverterFragment.newInstance(id)) }
        b.delete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext()).setTitle("Удалить пару?")
                .setMessage("Вы уверены, что хотите удалить? История конвертаций сохранится.")
                .setNegativeButton("Отмена", null).setPositiveButton("Удалить") { _, _ ->
                    perform(b.delete, b.progress) { model.repository.deletePair(id); parentFragmentManager.popBackStack() }
                }.show()
        }
        observe(model.data) { state ->
            if (!state.loaded) return@observe
            val pair = state.pairs.find { it.id == id }
            if (pair == null) { b.details.visibility = View.GONE; b.screenTitle.text = "Пара удалена"; b.edit.isEnabled = false; b.convert.isEnabled = false; b.delete.isEnabled = false }
            else {
                val categoryIds = state.links.filter { it.pairId == id }.map { it.categoryId }
                val categories = state.categories.filter { it.categoryId in categoryIds }.joinToString { it.title }.ifBlank { "Без категории" }
                b.details.visibility = View.VISIBLE
                b.pairTitle.text = pair.title
                b.pairRoute.text = "${pair.fromCode} → ${pair.toCode}"
                b.pairAmount.text = "${number(pair.defaultAmount)} ${pair.fromCode}"
                b.pairNoteGroup.visibility = if (pair.note.isBlank()) View.GONE else View.VISIBLE
                b.pairNote.text = pair.note
                b.pairCategories.text = categories
                b.pairCreated.text = dateText(pair.createdAt)
                b.pairConversions.text = state.history.count { it.pairId == id }.toString()
            }
        }
    }
    companion object { fun newInstance(id: Long) = PairDetailFragment().apply { arguments = Bundle().apply { putLong("id", id) } } }
}
