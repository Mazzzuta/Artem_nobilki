package com.example.currencyapp.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme(val nightMode: Int) {
    LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
    DARK(AppCompatDelegate.MODE_NIGHT_YES),
    SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
}
data class Preferences(val theme: AppTheme = AppTheme.SYSTEM, val base: String = "RUB", val sort: Int = 0)

class AppPreferences(context: Context) {
    private val store = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    // Сохраняем выбор темы при переходе со старого переключателя.
    private val previousTheme = if (store.contains("dark")) {
        if (store.getBoolean("dark", false)) AppTheme.DARK else AppTheme.LIGHT
    } else AppTheme.SYSTEM
    private val mutable = MutableStateFlow(Preferences(
        AppTheme.values().firstOrNull { it.name == store.getString("theme", previousTheme.name) } ?: previousTheme,
        store.getString("base", "RUB") ?: "RUB", store.getInt("sort", 0)))
    val values = mutable.asStateFlow()
    fun save(value: Preferences) {
        store.edit().putString("theme", value.theme.name).remove("dark")
            .putString("base", value.base).putInt("sort", value.sort).apply()
        mutable.value = value
    }
}
