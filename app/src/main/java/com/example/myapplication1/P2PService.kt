package com.example.myapplication1

import android.app.Service
import android.content.Intent
import android.os.IBinder

class P2PService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        println("P2PService online!")
    }

}