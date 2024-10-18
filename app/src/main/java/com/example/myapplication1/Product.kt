package com.example.myapplication1

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


@Dao
interface ProductDao {
    @Query("SELECT * FROM product2")
    fun getAll(): List<Product2>

    @Insert
    fun insertAll(vararg prods: Product2)

    @Delete
    fun delete(prod: Product2)
}