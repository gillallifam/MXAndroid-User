package com.example.myapplication1.p2pNet

import android.graphics.Bitmap
import com.example.myapplication1.types.ImageModel
import com.example.myapplication1.decodeBMP
import com.example.myapplication1.gson
import com.example.myapplication1.imageDao
import com.example.myapplication1.imgCache
import com.example.myapplication1.prodCache
import com.example.myapplication1.productDao
import com.example.myapplication1.types.Image
import com.example.myapplication1.types.Product

fun imageHandler2(data: String): Bitmap? {
    if (data.isNotEmpty()) {
        try {
            val imgData = gson.fromJson(data, ImageModel::class.java)
            val bitmap = decodeBMP(imgData.img)
            imageDao?.insertAll(Image(imgData.cod, imgData.img))
            return bitmap
        } catch (e: Exception) {
            println(e)
        }
    }
    return null
}

fun fillCaches() {
    prodCache = productDao!!.getAll().associateBy { it.cod } as LinkedHashMap<String, Product>
    imgCache = imageDao!!.getAll().associateBy { it.cod } as LinkedHashMap<String, Image>
    prodCache.forEach { prod ->
        val img = imgCache[prod.value.cod]
        val bmp = decodeBMP(img!!.img)
        prod.value.img = bmp
    }
    p2pViewModel!!.allProducts.clear()
    val ini = java.time.Instant.now().toEpochMilli().toInt()
    p2pViewModel!!.allProducts.addAll(prodCache.values)
    val end = java.time.Instant.now().toEpochMilli().toInt()
    val time = end - ini
    println(time)
}

fun updateCaches(localTimestamp: Long, remoteTimestamp: String) {
    p2pApi!!.updateProducts(localTimestamp).thenAccept { jsonString ->
        try {
            val products = gson.fromJson(jsonString, Array<Product>::class.java).asList()
            var successCount = 0
            for ((index, prod) in products.withIndex()) {
                productDao!!.insertAll(prod)
                p2pApi!!.getImage(prod.cod).thenAccept { imgData ->
                    if (imgData.isNotEmpty()) {
                        try {
                            val prodIdx = products[index]
                            prodIdx.img = imageHandler2(imgData)
                            successCount++
                            if (products.size - 1 == index) {
                                fillCaches()
                                if (successCount == products.size) {
                                    p2pPrefs!!.edit().putString("shopLastUpdate", remoteTimestamp)
                                        .apply()
                                }
                            }
                        } catch (e: Exception) {
                            println(e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println(e)
        }
    }
}

