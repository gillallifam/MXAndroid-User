package com.example.myapplication1

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    //var p2pState  by mutableStateOf("offline")
    var p2pState  = MutableLiveData("offline")
    var logoImage  by mutableStateOf<Bitmap?>(null)
    var dateText  by mutableStateOf("")

    fun updateP2PSate (state: String){
        p2pState.postValue(state)
    }
}