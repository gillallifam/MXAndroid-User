package com.example.myapplication1

import Cmd
import CmdResp
import ImageModel
import Load
import android.os.Handler
import android.os.Looper
import org.webrtc.DataChannel
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

const val timeout: Long = 5000

fun sendData(cmd: Cmd): CompletableFuture<String> {
    val promise = CompletableFuture<String>()
    Handler(Looper.getMainLooper()).postDelayed({
        promises[cmd.pid]?.let {
            it.complete("")
            promises.remove(cmd.pid)
        }
    }, timeout)

    if (dataChannel!!.state().name == "OPEN") {
        val pid = genPid()
        cmd.pid = pid
        promises[pid] = promise
        val payload = gson.toJson(cmd)
        val buffer = ByteBuffer.wrap(payload.toByteArray())
        dataChannel!!.send(DataChannel.Buffer(buffer, false))
    } else {
        println("Data channel not open")
    }
    return promise
}

fun receiveData(buffer: DataChannel.Buffer?) {
    val data: ByteBuffer = buffer!!.data
    val bytes = ByteArray(data.remaining())
    data.get(bytes);
    val resp = String(bytes)
    val cmd = gson.fromJson(resp, CmdResp::class.java)
    println(cmd)
    promises[cmd.pid]?.let {
        it.complete(cmd.content)
        promises.remove(cmd.pid)
    }
}

fun getProduct(productId: String): CompletableFuture<String> {
    return sendData(
        Cmd(
            cmd = "reqProd3",
            load = productId,
            from = "${deviceUUID}>tst@android.mktpix"
        )
    )
}

fun getImage(imageId: String): CompletableFuture<String> {
    return sendData(
        Cmd(
            cmd = "reqImg2",
            from = "${deviceUUID}>tst@android.mktpix",
            load = Load(cod = imageId)
        )
    )
}

fun imageHandler(data: String): ImageModel? {
    if (data.isNotEmpty()) {
        try {
            val img = gson.fromJson(data, ImageModel::class.java)
            println(img)
            return img
        } catch (e: Exception) {
            println(e)
        }
    }
    return null
}

fun updateProducts(): CompletableFuture<String> {
    return sendData(
        Cmd(
            cmd = "reqProducts2",
            from = "${deviceUUID}>tst@android.mktpix"
        )
    )
}