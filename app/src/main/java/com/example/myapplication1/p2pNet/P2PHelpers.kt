package com.example.myapplication1.p2pNet

import android.graphics.Bitmap
import com.example.myapplication1.types.ImageModel
import com.example.myapplication1.decodeBMP
import com.example.myapplication1.gson
import com.example.myapplication1.imageDao
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
    val allProdData = productDao!!.getAll()
    val allImgData = imageDao!!.getAll()
    p2pViewModel!!.imgCache2 = allImgData.associateBy { it.cod } as LinkedHashMap<String, Image>
    allProdData.forEach { prod ->
        val img = p2pViewModel!!.imgCache2[prod.cod]
        val bmp = decodeBMP(img!!.img)
        prod.img = bmp
    }
    p2pViewModel!!.prodCache2 = allProdData.associateBy { it.cod } as LinkedHashMap<String, Product>
    updateFilter()
}

fun updateFilter() {
    p2pViewModel!!.selectedProducts.clear()
    if (p2pViewModel!!.filter == "*") {
        p2pViewModel!!.selectedProducts.addAll(p2pViewModel!!.prodCache2.values)
    } else {
        p2pViewModel!!.selectedProducts.addAll(p2pViewModel!!.prodCache2.values.filter {
            it.nameSho.startsWith(
                p2pViewModel!!.filter
            )
        })
    }
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

