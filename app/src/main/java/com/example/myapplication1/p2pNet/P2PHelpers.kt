package com.example.myapplication1.p2pNet

import android.graphics.Bitmap
import com.example.myapplication1.ImageModel
import com.example.myapplication1.decodeBMP
import com.example.myapplication1.gson

fun imageHandler2(data: String): Bitmap? {
    if (data.isNotEmpty()) {
        try {
            val imgData = gson.fromJson(data, ImageModel::class.java)
            val b64 = imgData.img
                .replace("data:image/webp;base64,", "")
                .replace("data:image/jpeg;base64,", "")
                .replace("data:image/png;base64,", "")
            val bitmap = decodeBMP(b64)
            return bitmap
        } catch (e: Exception) {
            println(e)
        }
    }
    return null
}