package br.com.marketpix.mxuser.p2pNet

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import br.com.marketpix.mxuser.types.Category
import br.com.marketpix.mxuser.types.Image
import br.com.marketpix.mxuser.types.Product

class P2PViewModel : ViewModel() {
    var p2pState = mutableStateOf("connecting")
    var renderAge = mutableIntStateOf(0)
    //var renderTable = mutableStateOf(false)
    var dialogProdState = mutableStateOf(false)
    var dialogCatState = mutableStateOf(false)
    var dialogCartState = mutableStateOf(false)
    var dialogUserState = mutableStateOf(false)
    var dialogPaymentState = mutableStateOf(false)
    var dialogTmpState = mutableStateOf(false)
    var prodCache = mutableMapOf<String, Product>()
    var cartItems = mutableMapOf<String, Product>()
    var itemsInCart = mutableIntStateOf(0)
    var imgCache = mutableMapOf<String, Image>()
    var filterCategory =
        mutableStateOf(Category(name = "All", transId = "", selected = false, fractionable = false))
    var filterCategories = mutableStateListOf<Category>()
    var selectedProducts = mutableStateListOf<Product>()
    lateinit var selectedProd: Product
}