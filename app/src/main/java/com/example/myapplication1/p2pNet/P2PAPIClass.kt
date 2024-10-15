package com.example.myapplication1.p2pNet

import Cmd
import java.util.concurrent.CompletableFuture

class P2PAPIClass {
    fun peerPing2(): CompletableFuture<String> {
        return sendData(
            Cmd(
                cmd = "ping2",
                from = "$deviceUUID>tst@android.mktpix"
            )
        )
    }
    companion object {
        val instance = P2PAPIClass()
    }
}