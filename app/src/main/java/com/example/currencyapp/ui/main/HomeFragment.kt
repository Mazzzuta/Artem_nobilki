package com.example.currencyapp.ui.main

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.currencyapp.R
import com.example.currencyapp.databinding.FragmentHomeBinding
import com.example.currencyapp.ui.adapters.*
import com.example.currencyapp.ui.viewmodel.AppViewModel

class HomeFragment : Fragment(R.layout.fragment_home) {
    private val model: AppViewModel by activityViewModels { AppViewModel.Factory(requireActivity().application) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = FragmentHomeBinding.bind(view)
        val adapter = RecordAdapter { open(PairDetailFragment.newInstance(it.toLong())) }
        b.list.layoutManager = LinearLayoutManager(requireContext())
        b.list.adapter = adapter
        b.search.setText(model.filter.value.query)
        b.search.doAfterTextChanged { model.filter.value = model.filter.value.copy(query = it.toString()) }
        b.filters.setOnClickListener { showFilters() }
        b.add.setOnClickListener { open(PairEditFragment()) }
        b.categories.setOnClickListener { manageCategories() }
        observe(model.data) { state ->
            b.progress.visibility = if (state.loaded) View.GONE else View.VISIBLE
        }
        observe(model.filteredPairs) { pairs ->
            adapter.submitList(pairs.map { RecordRow(it.id.toString(), it.title, "${it.fromCode} → ${it.toCode} · ${number(it.defaultAmount)}\n${it.note}") })
            b.empty.visibility = if (pairs.isEmpty()) View.VISIBLE else View.GONE
            b.empty.text = if (model.data.value.pairs.isEmpty()) "Нет избранных пар. Нажмите «Добавить пару», чтобы создать первую." else "Ничего не найдено. Измените поиск или фильтры."
        }
    }

    private fun showFilters() {
        val b = com.example.currencyapp.databinding.DialogFiltersBinding.inflate(layoutInflater)
        val state = model.data.value
        val ids = listOf<Long?>(null) + state.categories.map { it.categoryId }
        b.category.items(listOf("Все категории") + state.categories.map { it.title }, ids.indexOf(model.filter.value.categoryId))
        b.period.items(listOf("За всё время", "За 7 дней", "За 30 дней"), listOf(0, 7, 30).indexOf(model.filter.value.days))
        b.sort.items(listOf("Сначала новые", "По названию", "Сначала старые"), model.preferences.values.value.sort)
        MaterialAlertDialogBuilder(requireContext()).setTitle("Фильтры и сортировка").setView(b.root)
            .setPositiveButton("Применить") { _, _ ->
                model.filter.value = model.filter.value.copy(categoryId = ids[b.category.selectedItemPosition], days = listOf(0, 7, 30)[b.period.selectedItemPosition])
                model.preferences.save(model.preferences.values.value.copy(sort = b.sort.selectedItemPosition))
            }.setNeutralButton("Сбросить") { _, _ -> model.filter.value = model.filter.value.copy(categoryId = null, days = 0) }
            .setNegativeButton("Отмена", null).show()
    }

    private fun manageCategories() {
        val categories = model.data.value.categories
        MaterialAlertDialogBuilder(requireContext()).setTitle("Категории")
            .setItems((categories.map { "Удалить: ${it.title}" } + "Создать категорию").toTypedArray()) { _, index ->
                if (index == categories.size) {
                    val input = EditText(requireContext()).apply { hint = "Название категории"; maxLines = 1 }
                    val dialog = MaterialAlertDialogBuilder(requireContext()).setTitle("Новая категория").setView(input)
                        .setNegativeButton("Отмена", null).setPositiveButton("Создать", null).create()
                    dialog.setOnShowListener {
                        val button = dialog.getButton(-1)
                        button.setOnClickListener {
                            if (input.text.isBlank() || input.text.length > 50) {
                                input.error = "Введите название до 50 символов"; message("Проверьте название")
                            } else perform(button) { model.repository.addCategory(input.text.toString()); dialog.dismiss() }
                        }
                    }
                    dialog.show()
                } else MaterialAlertDialogBuilder(requireContext()).setTitle("Удалить категорию?")
                    .setMessage("Пары и история сохранятся. Будет удалена только категория и её связи.")
                    .setNegativeButton("Отмена", null).setPositiveButton("Удалить") { _, _ ->
                        perform(requireView()) { model.repository.deleteCategory(categories[index]); model.filter.value = model.filter.value.copy(categoryId = null) }
                    }.show()
            }.setNegativeButton("Закрыть", null).show()
    }
}
