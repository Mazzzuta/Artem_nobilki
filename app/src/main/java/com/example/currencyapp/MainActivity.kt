package com.example.currencyapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.currencyapp.databinding.ActivityMainBinding
import com.example.currencyapp.ui.main.HomeFragment
import com.example.currencyapp.ui.main.CurrenciesFragment
import com.example.currencyapp.ui.main.ConverterFragment
import com.example.currencyapp.ui.main.AnalyticsFragment
import com.example.currencyapp.ui.main.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = com.example.currencyapp.data.AppPreferences(application).values.value
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(prefs.theme.nightMode)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_currencies -> {
                    loadFragment(CurrenciesFragment())
                    true
                }
                R.id.nav_converter -> {
                    loadFragment(ConverterFragment())
                    true
                }
                R.id.nav_analytics -> {
                    loadFragment(AnalyticsFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}