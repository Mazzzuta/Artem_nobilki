package com.example.currencyapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyapp.data.local.entity.CurrencyEntity
import com.example.currencyapp.data.repository.CurrencyRepository
import com.example.currencyapp.data.repository.SortOrder
import com.example.currencyapp.data.remote.util.NetworkResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: CurrencyRepository) : ViewModel() {

    // Состояние загрузки (для отображения ProgressBar или статуса)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Состояние ошибки / уведомлений
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Текущий поисковый запрос
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Текущий порядок сортировки
    private val _sortOrder = MutableStateFlow(SortOrder.CODE_ASC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // Основной список валют с поддержкой поиска и сортировки в реальном времени
    val currencies: StateFlow<List<CurrencyEntity>> = combine(
        repository.getAllCurrencies(),
        _searchQuery,
        _sortOrder
    ) { list, query, sort ->
        // Фильтрация по поисковому запросу
        val filtered = if (query.isBlank()) {
            list
        } else {
            list.filter {
                it.code.contains(query, ignoreCase = true) ||
                        it.name.contains(query, ignoreCase = true)
            }
        }

        // Сортировка списка
        when (sort) {
            SortOrder.CODE_ASC -> filtered.sortedBy { it.code }
            SortOrder.NAME_ASC -> filtered.sortedBy { it.name }
            SortOrder.RATE_DESC -> filtered.sortedByDescending { it.currentRate }
            SortOrder.RATE_ASC -> filtered.sortedBy { it.currentRate }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // При запуске обновляем данные с сервера
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            repository.refreshCurrencies().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                    }
                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        _errorMessage.value = null
                    }
                    is NetworkResult.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                }
            }
        }
    }

    // Метод для обновления поискового запроса из UI
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Метод для изменения сортировки
    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }
}