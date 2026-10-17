package com.example.currencyapp

import com.example.currencyapp.data.local.entity.*
import com.example.currencyapp.ui.viewmodel.*
import org.junit.Assert.*
import org.junit.Test

class PairFilterTest {
    @Test fun combinesSearchCategoryDateAndSort() {
        val now = 100 * 86_400_000L
        val pairs = listOf(
            FavoritePairEntity(1, "Доллары", "USD", "RUB", note = "Поездка", createdAt = now),
            FavoritePairEntity(2, "Евро", "EUR", "RUB", note = "Поездка", createdAt = now - 86_400_000L),
            FavoritePairEntity(3, "Старые", "USD", "RUB", note = "Поездка", createdAt = now - 40 * 86_400_000L)
        )
        val state = AppData(pairs = pairs, links = listOf(PairCategoryCrossRef(1, 1), PairCategoryCrossRef(2, 2), PairCategoryCrossRef(3, 1)))
        assertEquals(listOf(1L), filterPairs(state, PairFilter("поездка", 1, 7), 0, now).map { it.id })
        assertEquals(listOf(3L, 2L, 1L), filterPairs(state, PairFilter(), 2, now).map { it.id })
        assertTrue(filterPairs(state, PairFilter("missing", 1, 7), 0, now).isEmpty())
        assertEquals(listOf(1L, 3L), filterPairs(state, PairFilter("USD"), 0, now).map { it.id })
    }
}
