package br.com.marketpix.mxuser.p2pNet

import android.graphics.Bitmap
import br.com.marketpix.mxuser.p2pNet.p2pApi
import br.com.marketpix.mxuser.p2pNet.p2pPrefs
import br.com.marketpix.mxuser.p2pNet.p2pViewModel
import br.com.marketpix.mxuser.types.ImageModel
import br.com.marketpix.mxuser.decodeBMP
import br.com.marketpix.mxuser.gson
import br.com.marketpix.mxuser.imageDao
import br.com.marketpix.mxuser.productDao
import br.com.marketpix.mxuser.types.Image
import br.com.marketpix.mxuser.types.Product

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

