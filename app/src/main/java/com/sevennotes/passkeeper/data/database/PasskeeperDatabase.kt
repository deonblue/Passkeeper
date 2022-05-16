package com.sevennotes.passkeeper.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sevennotes.passkeeper.data.database.dao.CategoryDao
import com.sevennotes.passkeeper.data.database.dao.PasswordDao
import com.sevennotes.passkeeper.models.Category
import com.sevennotes.passkeeper.models.Password

@Database(
    entities = [Category::class, Password::class],
    version = 1,
    exportSchema = false
)
abstract class PasskeeperDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun passwordDao(): PasswordDao
}