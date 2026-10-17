package com.example.currencyapp

import android.content.Context
import com.example.currencyapp.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PreferencesTest {
    @Test fun allThreeThemesPersistAndOldSwitchMigrates() {
        val context = RuntimeEnvironment.getApplication()
        val store = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        store.edit().clear().commit()
        assertEquals(AppTheme.SYSTEM, AppPreferences(context).values.value.theme)
        AppTheme.values().forEach { theme ->
            val preferences = AppPreferences(context)
            preferences.save(preferences.values.value.copy(theme = theme))
            assertEquals(theme, AppPreferences(context).values.value.theme)
        }
        store.edit().clear().putBoolean("dark", true).commit()
        val migrated = AppPreferences(context)
        assertEquals(AppTheme.DARK, migrated.values.value.theme)
        migrated.save(migrated.values.value.copy(theme = AppTheme.SYSTEM))
        assertFalse(store.contains("dark"))
        assertEquals(AppTheme.SYSTEM, AppPreferences(context).values.value.theme)
        store.edit().clear().commit()
    }
}
