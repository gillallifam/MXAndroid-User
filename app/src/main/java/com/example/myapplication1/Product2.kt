package com.example.myapplication1

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity
data class Product2(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?
)

@Dao
interface Products2Dao {
    @Query("SELECT * FROM product2")
    fun getAll(): List<Product2>

    @Insert
    fun insertAll(vararg prods: Product2)

    @Delete
    fun delete(prods: Product2)
}
