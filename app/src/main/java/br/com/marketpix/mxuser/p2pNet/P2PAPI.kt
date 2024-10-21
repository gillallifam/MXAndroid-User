package br.com.marketpix.mxuser.p2pNet

import br.com.marketpix.mxuser.types.Cmd
import br.com.marketpix.mxuser.types.ImgLoad
import java.util.concurrent.CompletableFuture

class P2PAPI {
    fun peerPing2(retry: Int = 1): CompletableFuture<String> {
        return sendData(
            Cmd(
                cmd = "ping2",
                retry = retry,
                from = "$deviceUUID>$deviceUUID@android.mktpix"
            )
        )
    }
    fun shopLastUpdate(): CompletableFuture<String> {
        return sendData(
            Cmd(
                cmd = "shopLastUpdate2",
                from = "$deviceUUID>$deviceUUID@android.mktpix"
            )
        )
    }
    fun getProduct(productId: String): CompletableFuture<String> {
        return sendData(
            Cmd(
                cmd = "reqProd2",
                load = productId,
                from = "$deviceUUID>$deviceUUID@android.mktpix"
            )
        )
    }
    fun getImage(imageId: String, retry: Int = 3): CompletableFuture<String> {
        return sendData(
            Cmd(
                cmd = "reqImg2",
                from = "$deviceUUID>$deviceUUID@android.mktpix",
                retry = retry,
                load = ImgLoad(cod = imageId)
            )
        )
    }
    fun updateProducts(age: Long = 0): CompletableFuture<String> {
        return sendData(
            Cmd(
                cmd = "reqProducts2",
                age = age,
                from = "$deviceUUID>$deviceUUID@android.mktpix"
            )
        )
    }
    companion object {
        val instance = P2PAPI()
    }
}