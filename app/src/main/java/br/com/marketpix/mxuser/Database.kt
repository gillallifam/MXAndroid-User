package br.com.marketpix.mxuser

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import br.com.marketpix.mxuser.p2pNet.targetShop
import br.com.marketpix.mxuser.types.Image
import br.com.marketpix.mxuser.types.ImageDao
import br.com.marketpix.mxuser.types.Product
import br.com.marketpix.mxuser.types.ProductsDao

private var db: AppDatabase? = null
var productDao: ProductsDao? = null
var imageDao: ImageDao? = null

@Database(entities = [Product::class, Image::class], version = 3)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
    abstract fun productDao(): ProductsDao
}

fun getDatabase(context: Context): AppDatabase? {
    if (db == null) {
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "MXUser-$targetShop"
        )
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
        productDao = db!!.productDao()
        imageDao = db!!.imageDao()
    }
    return db
}

class Converters {
    @TypeConverter
    fun fromListString(list: List<String>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun toListString(data: String): List<String> {
        return data.split(",")
    }

}
