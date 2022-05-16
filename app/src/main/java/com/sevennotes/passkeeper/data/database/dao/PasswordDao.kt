package com.sevennotes.passkeeper.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sevennotes.passkeeper.models.Password

@Dao
interface PasswordDao {
    @Insert
    fun insertPassword(password: Password)

    @Update
    fun updatePassword(password: Password)

    @Delete
    fun deletePassword(password: Password)

    @Query("DELETE  FROM password WHERE categoryId = :categoryId")
    fun deletePasswordWithCategoryId(categoryId: Int)

    @Query("DELETE FROM password")
    fun deleteAllPasswords()

    @Query("SELECT * FROM password")
    fun getAllPasswords(): LiveData<List<Password>>

    @Query("SELECT count(*) FROM password")
    fun getAllPasswordNumbers(): Int
}