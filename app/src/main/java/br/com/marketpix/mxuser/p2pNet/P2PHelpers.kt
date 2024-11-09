package br.com.marketpix.mxuser.p2pNet

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import br.com.marketpix.mxuser.types.ImageModel
import br.com.marketpix.mxuser.decodeBMP
import br.com.marketpix.mxuser.gson
import br.com.marketpix.mxuser.imageDao
import br.com.marketpix.mxuser.productDao
import br.com.marketpix.mxuser.types.Image
import br.com.marketpix.mxuser.types.Product
import kotlinx.coroutines.launch

fun imageHandler(data: String): Bitmap? {
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
    p2pViewModel!!.imgCache = allImgData.associateBy { it.cod } as LinkedHashMap<String, Image>
    allProdData.forEach { prod ->
        val img = p2pViewModel!!.imgCache[prod.cod]
        val bmp = decodeBMP(img!!.img)
        prod.img = bmp
    }
    p2pViewModel!!.prodCache = allProdData.associateBy { it.cod } as LinkedHashMap<String, Product>
    updateFilter()
}

fun updateFilter() {
    val vm = p2pViewModel!!
    vm.selectedProducts.clear()

    when (vm.filterCategory.value.name) {
        "All" -> {
            vm.selectedProducts.addAll(vm.prodCache.values)
        }

        "Favorites" -> {
            vm.selectedProducts.addAll(vm.prodCache.values.take(10))
        }

        "Cart" -> {
            vm.itemsInCart.intValue = vm.cartItems.size
            vm.selectedProducts.addAll(vm.cartItems.values)
        }

        else -> {
            vm.selectedProducts.addAll(vm.prodCache.values.filter {
                it.categories.contains(vm.filterCategory.value.transId)
            })
        }
    }
}

suspend fun updateCaches(localTimestamp: Long, remoteTimestamp: String) {
    val resp = p2pApi!!.updateProducts(localTimestamp)
    if (!resp.isNullOrEmpty()) {
        try {
            val products = gson.fromJson(resp, Array<Product>::class.java).asList()
            var successCount = 0
            for ((index, prod) in products.withIndex()) {
                productDao!!.insertAll(prod)
                p2pViewModel!!.viewModelScope.launch {
                    val imgStr  = p2pApi!!.getImage(prod.cod)
                    if(!imgStr.isNullOrEmpty()){
                        try {
                            val prodIdx = products[index]
                            prodIdx.img = imageHandler(imgStr)
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

