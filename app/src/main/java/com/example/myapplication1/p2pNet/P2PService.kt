package com.example.myapplication1.p2pNet

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.myapplication1.MainActivity

class P2PService2 : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        P2PAPIClass.instance
        startP2P(this)
        connectPeer()
        println("P2PService online!")
    }
}