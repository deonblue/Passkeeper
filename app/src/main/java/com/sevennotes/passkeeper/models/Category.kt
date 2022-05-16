package com.sevennotes.passkeeper.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "category")
data class Category(
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    @ColumnInfo var name: String = "",
    @ColumnInfo var img: Int = 0
): Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}