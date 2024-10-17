package com.example.myapplication1.p2pNet

import android.app.Service
import android.content.Intent
import android.os.IBinder

class P2PService2 : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        /*P2PAPI.instance
        if (p2pViewModel!!.p2pService == null) p2pViewModel!!.p2pService = this
        startP2P(this)
        if (localPeer == null) connectPeer()*/
        println("P2PService online!")
    }
}