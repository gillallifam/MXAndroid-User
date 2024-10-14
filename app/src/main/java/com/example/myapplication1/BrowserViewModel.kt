package com.example.myapplication1

import Product
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class BrowserViewModel : ViewModel() {
    var filter by mutableStateOf("Pizza")
    var allProducts = mutableStateListOf<Product>()
    var selectedProducts = mutableStateListOf<Product>()
}