package com.example.currencyapp.data.local.dao

import androidx.room.*
import com.example.currencyapp.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PairDao {
    @Query("SELECT * FROM favorite_pairs ORDER BY createdAt DESC")
    abstract fun observePairs(): Flow<List<FavoritePairEntity>>
    @Query("SELECT * FROM favorite_pairs WHERE id = :id")
    abstract suspend fun find(id: Long): FavoritePairEntity?
    @Insert abstract suspend fun insert(pair: FavoritePairEntity): Long
    @Update abstract suspend fun update(pair: FavoritePairEntity): Int
    @Query("DELETE FROM favorite_pairs WHERE id = :id")
    abstract suspend fun delete(id: Long)
    @Query("SELECT * FROM pair_categories")
    abstract fun observeLinks(): Flow<List<PairCategoryCrossRef>>
    @Query("DELETE FROM pair_categories WHERE pairId = :id")
    abstract suspend fun clearLinks(id: Long)
    @Insert abstract suspend fun insertLinks(links: List<PairCategoryCrossRef>)

    @Transaction
    open suspend fun save(pair: FavoritePairEntity, categories: List<Long>): Long {
        val id = if (pair.id == 0L) insert(pair) else {
            check(update(pair) == 1) { "Пара уже удалена" }
            pair.id
        }
        clearLinks(id)
        insertLinks(categories.distinct().map { PairCategoryCrossRef(id, it) })
        return id
    }
}
