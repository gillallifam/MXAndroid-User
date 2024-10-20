package com.example.myapplication1.p2pNet

import android.annotation.SuppressLint
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.myapplication1.types.Product

class P2PViewModel : ViewModel() {
    var p2pState: MutableState<String> =  mutableStateOf("connecting")
    var allProducts = mutableStateListOf<Product>()
}