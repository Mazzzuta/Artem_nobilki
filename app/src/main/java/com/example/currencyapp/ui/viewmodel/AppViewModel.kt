package com.example.currencyapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.currencyapp.data.AppPreferences
import com.example.currencyapp.data.local.AppDatabase
import com.example.currencyapp.data.local.entity.*
import com.example.currencyapp.data.repository.AppRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Calendar

data class AppData(
    val currencies: List<CurrencyEntity> = emptyList(),
    val pairs: List<FavoritePairEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val links: List<PairCategoryCrossRef> = emptyList(),
    val history: List<ConversionHistoryEntity> = emptyList(),
    val loaded: Boolean = false
)
data class PairFilter(val query: String = "", val categoryId: Long? = null, val days: Int = 0)
data class LoadStatus(val loading: Boolean = false, val message: String = "")
data class ChartData(val points: List<RatePointEntity> = emptyList(), val status: LoadStatus = LoadStatus(), val code: String = "")

class AppViewModel(application: Application) : AndroidViewModel(application) {
    val repository = AppRepository(AppDatabase.getDatabase(application))
    val preferences = AppPreferences(application)
    val filter = MutableStateFlow(PairFilter())
    private val mutableStatus = MutableStateFlow(LoadStatus())
    val status = mutableStatus.asStateFlow()
    private val mutableChart = MutableStateFlow(ChartData())
    val chart = mutableChart.asStateFlow()
    private var chartJob: Job? = null
    val data = combine(repository.currencies, repository.pairs, repository.categories, repository.links, repository.history) { rates, pairs, categories, links, history ->
        AppData(rates.filter { it.source == "CBR" }, pairs, categories, links, history, loaded = true)
    }.catch { mutableStatus.value = LoadStatus(message = "Ошибка чтения базы: ${it.localizedMessage}") }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppData())

    val filteredPairs = combine(data, filter, preferences.values) { state, criteria, prefs ->
        filterPairs(state, criteria, prefs.sort)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init { refresh() }
    fun refresh() {
        if (mutableStatus.value.loading) return
        viewModelScope.launch {
            mutableStatus.value = LoadStatus(loading = true, message = "Загрузка курсов ЦБ…")
            try {
                repository.refresh()
                mutableStatus.value = LoadStatus(message = "Курсы ЦБ обновлены")
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) {
                mutableStatus.value = LoadStatus(message = "Не удалось обновить курсы ЦБ. Проверьте интернет и попробуйте ещё раз. Сохранённые данные доступны офлайн.")
            }
        }
    }

    fun loadChart(currency: CurrencyEntity, days: Int) {
        chartJob?.cancel()
        chartJob = viewModelScope.launch {
            mutableChart.value = ChartData(code = currency.code, status = LoadStatus(true, "Загрузка истории ЦБ…"))
            val end = System.currentTimeMillis()
            val start = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -days) }.timeInMillis
            try {
                val (points, warning) = repository.chart(currency, start, end)
                mutableChart.value = ChartData(points, LoadStatus(message = warning ?: "Исторические курсы ЦБ"), currency.code)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { mutableChart.value = ChartData(code = currency.code, status = LoadStatus(message = "Ошибка истории: ${error.localizedMessage}")) }
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(AppViewModel::class.java))
            return AppViewModel(application) as T
        }
    }
}

internal fun filterPairs(state: AppData, criteria: PairFilter, sort: Int, now: Long = System.currentTimeMillis()): List<FavoritePairEntity> {
        val cutoff = if (criteria.days == 0) 0 else now - criteria.days * 86_400_000L
        val selectedIds = criteria.categoryId?.let { id -> state.links.filter { it.categoryId == id }.map { it.pairId }.toSet() }
        val result = state.pairs.filter { pair ->
            (criteria.query.isBlank() || listOf(pair.title, pair.note, pair.fromCode, pair.toCode).any { it.contains(criteria.query, true) }) &&
                (selectedIds == null || pair.id in selectedIds) && pair.createdAt >= cutoff
        }
        return when (sort) {
            1 -> result.sortedBy { it.title.lowercase() }
            2 -> result.sortedBy { it.createdAt }
            else -> result.sortedByDescending { it.createdAt }
        }
}
