package br.com.marketpix.mxuser.p2pNet

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import br.com.marketpix.mxuser.p2pNet.filterOptions
import br.com.marketpix.mxuser.types.Image
import br.com.marketpix.mxuser.types.Product

class P2PViewModel : ViewModel() {
    var p2pState: MutableState<String> = mutableStateOf("connecting")
    var prodCache2 = mutableMapOf<String, Product>()
    var imgCache2 = mutableMapOf<String, Image>()
    var filter by mutableStateOf(filterOptions[0])
    var selectedProducts = mutableStateListOf<Product>()
}