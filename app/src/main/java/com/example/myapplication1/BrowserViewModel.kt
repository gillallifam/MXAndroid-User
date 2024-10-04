package com.example.myapplication1

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class BrowserViewModel : ViewModel() {
    var p2pState  by mutableStateOf("offline")
    var logoImage  by mutableStateOf<Bitmap?>(null)
}