package br.com.marketpix.mxuser.p2pNet

import br.com.marketpix.mxuser.gson
import br.com.marketpix.mxuser.types.Cmd
import br.com.marketpix.mxuser.types.ImgLoad
import br.com.marketpix.mxuser.types.TEvent
import com.google.gson.JsonElement
import kotlinx.coroutines.future.await
import java.util.concurrent.CompletableFuture

class P2PApi {
    suspend fun peerPing2(retry: Int = 1): JsonElement? {
        return sendData(
            Cmd(
                cmd = "ping2",
                retry = retry,
                from = "$deviceUUID>$deviceUUID@android.mktpix"
            )
        ).await()
    }

    suspend fun reqEvents(retry: Int = 1): Array<TEvent> {

        val resp = sendData(
            Cmd(
                cmd = "req-Events",
                retry = retry,
                from = "$deviceUUID>$deviceUUID@android.mktpix"
            )
        ).await()

        val events = gson.fromJson(resp, Array<TEvent>::class.java)

        return events
    }

    suspend fun shopLastUpdate(): JsonElement? {
        return sendData(
            Cmd(
                cmd = "shopLastUpdate2",
                from = "$deviceUUID>$deviceUUID@android.mktpix"
            )
        ).await()
    }

    fun getProduct(productId: String): CompletableFuture<JsonElement> {
        return sendData(
            Cmd(
                cmd = "reqProd2",
                load = productId,
                from = "$deviceUUID>$deviceUUID@android.mktpix"
            )
        )
    }

    suspend fun getImage(imageId: String, retry: Int = 3): JsonElement? {
        return sendData(
            Cmd(
                cmd = "reqImg2",
                from = "$deviceUUID>$deviceUUID@android.mktpix",
                retry = retry,
                load = ImgLoad(cod = imageId)
            )
        ).await()
    }

    suspend fun updateProducts(age: Long = 0): JsonElement? {
        return sendData(
            Cmd(
                cmd = "reqProducts2",
                age = age,
                from = "$deviceUUID>$deviceUUID@android.mktpix"
            )
        ).await()
    }

    companion object {
        val instance = P2PApi()
    }
}