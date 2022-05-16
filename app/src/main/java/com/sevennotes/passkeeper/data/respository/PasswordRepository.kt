package com.sevennotes.passkeeper.data.respository

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sevennotes.passkeeper.models.Category
import com.sevennotes.passkeeper.models.Password

interface PasswordRepository {
    fun getAllPasswords(): LiveData<List<Password>>
    fun getAllCategories(): LiveData<List<Category>>
    suspend fun insertPassword(password: Password)
    suspend fun updatePassword(password: Password)
    suspend fun deletePassword(password: Password)
    suspend fun getAllPasswordNumbers(): Int
    suspend fun insertCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)
    suspend fun getCategoryNumbers(): Int
    suspend fun clearAll()
}