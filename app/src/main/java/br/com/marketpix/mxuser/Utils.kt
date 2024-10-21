package br.com.marketpix.mxuser

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.DecimalFormat
import android.icu.text.NumberFormat
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


val gson = Gson()


fun decodeBMP (encodedString: String): Bitmap {
    val clearStr = encodedString.replace("data:image/webp;base64,", "")
        .replace("data:image/jpeg;base64,", "")
        .replace("data:image/png;base64,", "")
    val imageBytes = Base64.decode(clearStr, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

fun timeID (): String {
    return Integer.toString(
        java.time.Instant.now().toEpochMilli().toInt(),
        Character.MAX_RADIX
    )
}

fun getRandomString(length: Int) : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun genPid (length: Int = 8): String {
    return "${timeID()}-${getRandomString(length)}"
}

fun internetConnectionAvailable(timeOut: Int): Boolean {
    var inetAddress: InetAddress? = null
    try {
        val future: Future<InetAddress?>? =
            Executors.newSingleThreadExecutor().submit(Callable<InetAddress?> {
                try {
                    InetAddress.getByName("google.com")
                } catch (e: UnknownHostException) {
                    null
                }
            })
        if (future != null) {
            inetAddress = future.get(timeOut.toLong(), TimeUnit.MILLISECONDS)
        }
        future?.cancel(true)
    } catch (e: InterruptedException) {
    } catch (e: ExecutionException) {
    } catch (e: TimeoutException) {
    }
    return inetAddress != null && inetAddress.toString() != ""
}
