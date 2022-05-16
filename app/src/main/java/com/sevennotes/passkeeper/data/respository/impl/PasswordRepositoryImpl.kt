package com.sevennotes.passkeeper.data.respository.impl

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.sevennotes.passkeeper.data.database.PasskeeperDatabase
import com.sevennotes.passkeeper.data.respository.PasswordRepository
import com.sevennotes.passkeeper.models.Category
import com.sevennotes.passkeeper.models.Password

class PasswordRepositoryImpl private constructor(context: Context) : PasswordRepository {

    companion object {
        private var instance: PasswordRepository? = null
        fun getInstance(context: Context): PasswordRepository {
            if (instance == null) {
                instance = PasswordRepositoryImpl(context)
            }
            return instance as PasswordRepository
        }
    }

    private val db =
        Room.databaseBuilder(context, PasskeeperDatabase::class.java, "passkeeper")
            .allowMainThreadQueries()
            .build()

    override fun getAllPasswords(): LiveData<List<Password>> {
        return db.passwordDao().getAllPasswords()
    }

    override fun getAllCategories(): LiveData<List<Category>> {
        return db.categoryDao().getAllCategories()
    }

    override suspend fun insertPassword(password: Password) {
        Log.d("test", "insert password: $password")
        db.passwordDao().insertPassword(password)
    }

    override suspend fun updatePassword(password: Password) {
        db.passwordDao().updatePassword(password)
    }

    override suspend fun deletePassword(password: Password) {
        db.passwordDao().deletePassword(password)
    }

    override suspend fun getAllPasswordNumbers(): Int {
        return db.passwordDao().getAllPasswordNumbers()
    }

    override suspend fun insertCategory(category: Category): Long {
        return db.categoryDao().insertCategory(category)
    }

    override suspend fun updateCategory(category: Category) {
        db.categoryDao().updateCategory(category)
    }

    override suspend fun deleteCategory(category: Category) {
        db.categoryDao().deleteCategory(category)
        category.id?.let { db.passwordDao().deletePasswordWithCategoryId(it) }
    }

    override suspend fun getCategoryNumbers(): Int {
        return db.categoryDao().getCategoryNumbers()
    }

    override suspend fun clearAll() {
        db.categoryDao().deleteAllCategories()
        db.passwordDao().deleteAllPasswords()
    }
}