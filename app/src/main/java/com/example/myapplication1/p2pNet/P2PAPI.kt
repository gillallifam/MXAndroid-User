package com.example.myapplication1.p2pNet

import Cmd
import Load
import java.util.concurrent.CompletableFuture

class P2PAPI {
    fun peerPing2(): CompletableFuture<String> {
        return sendData(
            Cmd(
                cmd = "ping2",
                from = "$deviceUUID>tst@android.mktpix"
            )
        )
    }
    fun getProduct(productId: String): CompletableFuture<String> {
        return sendData(
            Cmd(
                cmd = "reqProd2",
                load = productId,
                from = "$deviceUUID>tst@android.mktpix"
            )
        )
    }
    fun getImage(imageId: String): CompletableFuture<String> {
        return sendData(
            Cmd(
                cmd = "reqImg2",
                from = "$deviceUUID>tst@android.mktpix",
                load = Load(cod = imageId)
            )
        )
    }
    fun updateProducts(): CompletableFuture<String> {
        return sendData(
            Cmd(
                cmd = "reqProducts2",
                age = 0,
                from = "$deviceUUID>tst@android.mktpix"
            )
        )
    }
    companion object {
        val instance = P2PAPI()
    }
}