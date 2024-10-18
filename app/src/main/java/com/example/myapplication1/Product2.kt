package com.example.myapplication1

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity
data class Product2(
    @PrimaryKey val cod: String,
    val nameSho: String,
    val nameLon: String?,
    val qnt: Int,
    val min: Int,
    val cost: Int,
    val price: Double,
    val expiration: String,
    val expAlert: String,
    val provider: String,
    //val categories: Array<String>,
    val notify: String,
    val active: Boolean,
    val showStock: Boolean,
    val hasOptionals: Boolean,
    val hasSizes: Boolean,
    val hasAdditionals: Boolean,
    //val optionals: Any? = null,
    //val additionals: Any? = null,
    //val sizes: Any? = null,
    val fractionable: Boolean,
    val translatable: Boolean,
    val description: String,
    val fractionCalc: String,
    val lastUpdate: Long,
    var visible: Boolean = true,
) {
    @Ignore
    var img: Bitmap? = null
}

@Dao
interface Products2Dao {
    @Query("SELECT * FROM product2")
    fun getAll(): List<Product2>

    @Insert
    fun insertAll(vararg prods: Product2)

    @Delete
    fun delete(prods: Product2)
}
