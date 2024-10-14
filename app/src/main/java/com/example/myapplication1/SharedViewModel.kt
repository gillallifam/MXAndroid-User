package com.example.myapplication1

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    private val _viewState: MutableState<String> = mutableStateOf("offline")
    var p2pSte: MutableState<String> = _viewState
}