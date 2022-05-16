package com.sevennotes.passkeeper.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "password")
data class Password(
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    @ColumnInfo var name: String = "",
    @ColumnInfo var url: String = "",
    @ColumnInfo var account: String = "",
    @ColumnInfo var password: String = "",
    @ColumnInfo var note: String = "",
    @ColumnInfo var categoryId: Int = 0
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}