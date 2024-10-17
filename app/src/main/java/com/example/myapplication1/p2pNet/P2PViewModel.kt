package com.example.myapplication1.p2pNet

import android.annotation.SuppressLint
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

@SuppressLint("StaticFieldLeak")
class P2PViewModel : ViewModel() {
    private val _p2pState: MutableState<String> = mutableStateOf("online")
    var p2pState: MutableState<String> = _p2pState
}