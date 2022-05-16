package com.sevennotes.passkeeper.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sevennotes.passkeeper.models.Category

@Dao
interface CategoryDao {
    @Insert
    fun insertCategory(category: Category): Long

    @Update
    fun updateCategory(category: Category)

    @Delete
    fun deleteCategory(category: Category)

    @Query("DELETE FROM category")
    fun deleteAllCategories()

    @Query("SELECT * FROM category")
    fun getAllCategories(): LiveData<List<Category>>

    @Query("SELECT count(*) FROM category")
    fun getCategoryNumbers(): Int
}