package br.com.marketpix.mxuser.p2pNet

import br.com.marketpix.mxuser.decodeBMP
import br.com.marketpix.mxuser.imageDao
import br.com.marketpix.mxuser.productDao
import br.com.marketpix.mxuser.types.Image
import br.com.marketpix.mxuser.types.Product

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
