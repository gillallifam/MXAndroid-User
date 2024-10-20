package com.example.myapplication1

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication1.types.Image
import com.example.myapplication1.types.ImageDao
import com.example.myapplication1.types.Product
import com.example.myapplication1.types.ProductsDao

private var db: AppDatabase? = null
var productDao: ProductsDao? = null
var imageDao: ImageDao? = null
var prodCache = LinkedHashMap<String, Product>()
var imgCache = LinkedHashMap<String, Image>()

@Database(entities = [Product::class, Image::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
    abstract fun productDao(): ProductsDao
}

fun getDatabase(context: Context): AppDatabase? {
    if (db == null) {
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "MXUser"
        )
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
        productDao = db!!.productDao()
        imageDao = db!!.imageDao()
    }
    return db
}
