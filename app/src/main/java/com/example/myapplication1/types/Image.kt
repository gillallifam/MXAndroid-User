package com.example.myapplication1.types

import android.graphics.Bitmap
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity
data class Image(
    @PrimaryKey val cod: String,
    val img: String,
)

@Dao
interface ImageDao {
    @Query("SELECT * FROM image")
    fun getAll(): List<Image>

    @Query("SELECT * FROM image where cod = :cod")
    fun get(cod:String): Image

    @Insert( onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg img: Image)

    @Delete
    fun delete(img: Product)
}
