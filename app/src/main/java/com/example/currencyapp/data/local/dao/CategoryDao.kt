package com.example.currencyapp.data.local.dao

import androidx.room.*
import com.example.currencyapp.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY title ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE categoryId = :categoryId LIMIT 1")
    suspend fun getCategoryById(categoryId: Long): CategoryEntity?
    @Query("""
        SELECT cat.* FROM categories cat
        INNER JOIN currency_category_cross_ref ref ON cat.categoryId = ref.categoryId
        WHERE ref.currencyCode = :currencyCode
    """)
    fun getCategoriesForCurrency(currencyCode: String): Flow<List<CategoryEntity>>
}