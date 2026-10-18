package com.example.currencyapp

import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import com.example.currencyapp.databinding.FragmentPairEditBinding
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ScreenTest {
    @Test fun keyboardInsetsAreAppliedOnceWithoutExpandingNavigation() {
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup().visible()
        try {
            val activity = controller.get()
            val root = activity.findViewById<android.view.View>(R.id.main)
            val navigation = activity.findViewById<android.view.View>(R.id.bottomNavigation)
            val insets = androidx.core.view.WindowInsetsCompat.Builder()
                .setInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars(), androidx.core.graphics.Insets.of(0, 24, 0, 0))
                .setInsets(androidx.core.view.WindowInsetsCompat.Type.navigationBars(), androidx.core.graphics.Insets.of(0, 0, 0, 24))
                .setInsets(androidx.core.view.WindowInsetsCompat.Type.ime(), androidx.core.graphics.Insets.of(0, 0, 0, 360))
                .build()
            val result = androidx.core.view.ViewCompat.dispatchApplyWindowInsets(root, insets)
            root.measure(android.view.View.MeasureSpec.makeMeasureSpec(1080, android.view.View.MeasureSpec.EXACTLY), android.view.View.MeasureSpec.makeMeasureSpec(2340, android.view.View.MeasureSpec.EXACTLY))
            root.layout(0, 0, 1080, 2340)
            assertTrue(result.isConsumed)
            assertEquals(360, root.paddingBottom)
            assertTrue(navigation.paddingBottom < 100)
            val keyboardHeight = navigation.height
            androidx.core.view.ViewCompat.dispatchApplyWindowInsets(root, androidx.core.view.WindowInsetsCompat.Builder(insets)
                .setInsets(androidx.core.view.WindowInsetsCompat.Type.ime(), androidx.core.graphics.Insets.NONE).build())
            root.measure(android.view.View.MeasureSpec.makeMeasureSpec(1080, android.view.View.MeasureSpec.EXACTLY), android.view.View.MeasureSpec.makeMeasureSpec(2340, android.view.View.MeasureSpec.EXACTLY))
            root.layout(0, 0, 1080, 2340)
            assertEquals(24, root.paddingBottom)
            assertEquals(keyboardHeight, navigation.height)
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun markerDisplaysTheOriginalQuoteWithTwoDecimals() {
        val context = ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_CurrencyApp)
        val marker = com.example.currencyapp.ui.main.RateMarkerView(context,
            listOf(com.example.currencyapp.data.local.entity.RatePointEntity("USD", 1000L, 83.6177)), "USD")
        marker.refreshContent(com.github.mikephil.charting.data.Entry(0f, 83f), null)
        val text = marker.findViewById<android.widget.TextView>(R.id.markerText).text.toString()
        assertTrue(text.endsWith("1 USD = 83,62 RUB"))
    }

    @Test fun tappingRecordDispatchesSelection() {
        val context = ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_CurrencyApp)
        var selected = ""
        val adapter = com.example.currencyapp.ui.adapters.RecordAdapter { selected = it }
        adapter.submitList(listOf(com.example.currencyapp.ui.adapters.RecordRow("42", "TEST", "USD → RUB")))
        val parent = androidx.recyclerview.widget.RecyclerView(context).apply { layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context) }
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val row = holder.itemView
        val controller = org.robolectric.Robolectric.buildActivity(android.app.Activity::class.java).setup().visible()
        controller.get().setContentView(row)
        row.measure(android.view.View.MeasureSpec.makeMeasureSpec(800, android.view.View.MeasureSpec.EXACTLY), android.view.View.MeasureSpec.makeMeasureSpec(240, android.view.View.MeasureSpec.EXACTLY))
        row.layout(0, 0, 800, 240)
        val time = android.os.SystemClock.uptimeMillis()
        row.dispatchTouchEvent(android.view.MotionEvent.obtain(time, time, android.view.MotionEvent.ACTION_DOWN, 50f, 50f, 0))
        row.dispatchTouchEvent(android.view.MotionEvent.obtain(time, time + 40, android.view.MotionEvent.ACTION_UP, 50f, 50f, 0))
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        assertEquals("42", selected)
        controller.pause().stop().destroy()
    }

    @Test fun formRejectsBlankNameAndInvalidAmount() {
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            val activity = controller.get()
            activity.supportFragmentManager.executePendingTransactions()
            val fragment = com.example.currencyapp.ui.main.PairEditFragment()
            activity.supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commitNow()
            val form = FragmentPairEditBinding.bind(fragment.requireView())
            form.name.setText("")
            form.amount.setText("-1")
            form.save.performClick()
            assertEquals("Введите название", form.nameLayout.error.toString())
            assertNotNull(form.amountLayout.error)
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun allScreensInflateWithApplicationTheme() {
        val context = ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_CurrencyApp)
        val inflater = LayoutInflater.from(context)
        listOf(R.layout.activity_main, R.layout.fragment_home, R.layout.fragment_pair_edit, R.layout.fragment_pair_detail,
            R.layout.fragment_currencies, R.layout.fragment_converter, R.layout.fragment_analytics, R.layout.fragment_settings, R.layout.dialog_filters)
            .forEach { assertNotNull(inflater.inflate(it, null)) }

    }
}



