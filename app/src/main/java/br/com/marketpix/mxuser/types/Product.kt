package br.com.marketpix.mxuser.types

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
data class Product(
    @PrimaryKey val cod: String,
    val nameSho: String,
    val nameLon: String?,
    val qnt: Int?,
    val min: Int?,
    val cost: Double?,
    val price: Double?,
    val expiration: String?,
    val expAlert: String?,
    val provider: String?,
    val categories: List<String>,
    val notify: String?,
    val active: Boolean?,
    val showStock: Boolean?,
    val hasOptionals: Boolean?,
    val hasSizes: Boolean?,
    val hasAdditionals: Boolean?,
    //val optionals: Any? = null,
    //val additionals: Any? = null,
    //val sizes: Any? = null,
    val fractionable: Boolean?,
    val translatable: Boolean?,
    val description: String?,
    val fractionCalc: String?,
    val lastUpdate: Long?,
    var visible: Boolean = true,
) {
    @Ignore
    var img: Bitmap? = null
}

@Dao
interface ProductsDao {
    @Query("SELECT * FROM product")
    fun getAll(): List<Product>

    @Query("SELECT * FROM product where cod = :cod")
    fun get(cod:String): Product

    @Insert( onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg prod: Product)

    @Delete
    fun delete(prod: Product)
}
