package com.example.myapplication1.p2pNet

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class P2PViewModel : ViewModel() {
    private val _p2pState: MutableState<String> = mutableStateOf("offline")
    var p2pState: MutableState<String> = _p2pState
}