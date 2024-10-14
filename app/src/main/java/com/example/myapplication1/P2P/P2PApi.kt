package com.example.myapplication1.P2P

import Cmd
import CmdResp
import ImageModel
import Load
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import com.example.myapplication1.decodeBMP
import com.example.myapplication1.genPid
import com.example.myapplication1.gson
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
    //println(cmd)
    promises[cmd.pid]?.let {
        it.complete(cmd.content)
        promises.remove(cmd.pid)
    }
}

fun peerPing(): CompletableFuture<String> {
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

fun imageHandler(data: String): Bitmap? {
    if (data.isNotEmpty()) {
        try {
            val imgData = gson.fromJson(data, ImageModel::class.java)
            val b64 = imgData.img
                .replace("data:image/webp;base64,", "")
                .replace("data:image/jpeg;base64,", "")
                .replace("data:image/png;base64,", "");
            val bitmap = decodeBMP(b64)
            return bitmap
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
            age = 0,
            from = "$deviceUUID>tst@android.mktpix"
        )
    )
}