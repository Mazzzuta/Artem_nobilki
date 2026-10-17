package com.example.currencyapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyapp.data.local.entity.CategoryEntity
import com.example.currencyapp.data.repository.CurrencyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: CurrencyRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createCategory(title: String, colorHex: String) {
        viewModelScope.launch {
            repository.addCategory(CategoryEntity(title = title, colorHex = colorHex))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun clearConversionHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}