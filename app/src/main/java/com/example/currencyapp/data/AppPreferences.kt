package com.example.currencyapp.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Preferences(val dark: Boolean = false, val base: String = "RUB", val sort: Int = 0)

class AppPreferences(context: Context) {
    private val store = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    private val mutable = MutableStateFlow(Preferences(store.getBoolean("dark", false), store.getString("base", "RUB") ?: "RUB", store.getInt("sort", 0)))
    val values = mutable.asStateFlow()
    fun save(value: Preferences) {
        store.edit().putBoolean("dark", value.dark).putString("base", value.base).putInt("sort", value.sort).apply()
        mutable.value = value
    }
}
