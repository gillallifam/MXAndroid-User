package com.example.myapplication1

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings.Secure
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication1.P2P.P2PService
import com.example.myapplication1.P2P.connectPeer
import com.example.myapplication1.P2P.deviceUUID
import com.example.myapplication1.P2P.getImage
import com.example.myapplication1.P2P.localPeer
import com.example.myapplication1.P2P.mainViewModel
import com.example.myapplication1.P2P.peerPing
import com.example.myapplication1.P2P.sharedViewModel
import com.example.myapplication1.P2P.startP2P
import com.example.myapplication1.ui.theme.MyApplication1Theme
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.concurrent.CompletableFuture

suspend fun getTst(): CompletableFuture<String>? {
    // makes a request and suspends the coroutine
    val x = getImage("98740002").thenApply { result ->
        return@thenApply result
    }
    return x
}

class MainActivity : ComponentActivity() {

    private var sharedPref: SharedPreferences? = null


    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        startP2P(this)
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        val serviceIntent = Intent(this, P2PService::class.java)
        startService(serviceIntent)
        //val items by mainViewModel!!.p2pState.collectAsState()
        //val p2pstate = mainViewModel!!.p2pState.value

        setContent {
            MyApplication1Theme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //val items by mainViewModel!!.p2pState.observeAsState()
                    Text(
                        text = "Android webRTC",
                        modifier = Modifier.padding(20.dp)
                    )

                    Text(
                        text = sharedViewModel!!.p2pSte.value,
                        modifier = Modifier.padding(20.dp)
                    )

                    Button(
                        //enabled = viewModel!!.p2pState == "offline",
                        onClick = {
                            connectPeer()
                        }) {
                        Text("Connect")
                    }
                    Button(
                        //enabled = viewModel!!.p2pState == "offline",
                        onClick = {
                            startActivity(Intent(this@MainActivity, BrowserActivity::class.java))
                        }) {
                        Text("Browse")
                    }
                    Button(onClick = {
                        peerPing().thenApply { result ->
                            mainViewModel!!.dateText = result
                        }
                        /*getImage("98740002").thenApply { result ->
                            val bitmap = imageHandler(result)
                            if (bitmap != null) {
                                mainViewModel!!.logoImage = bitmap
                            } else {
                                println("Img no data")
                            }
                        }*/

                        /*GlobalScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                            val x = getTst()
                            println(x)
                        }*/

                        /*getProduct("98740002").thenAccept { result ->
                            if (result.isNotEmpty()) {
                                try {
                                    val resp = gson.fromJson(result, Product::class.java)
                                    println(resp)
                                } catch (e: Exception) {
                                    println(e)
                                }
                            } else {
                                println("getProduct no data")
                            }
                        }*/
                    }) {
                        Text("Msg")
                    }
                    Text(mainViewModel!!.dateText)
                    if (mainViewModel!!.logoImage != null) {
                        Image(
                            bitmap = mainViewModel!!.logoImage!!.asImageBitmap(),
                            contentDescription = "img logo"
                        )
                    }

                }
            }
        }
        sharedPref = getSharedPreferences("appPrefs", MODE_PRIVATE)
        val hasId = sharedPref!!.getString("deviceUUID", "")
        if (hasId!!.isNotEmpty()) {
            deviceUUID = hasId
        } else {
            val baseId = timeID()
            val androidId = Secure.getString(this.contentResolver, Secure.ANDROID_ID)
            deviceUUID = "${baseId}-${androidId}"
            sharedPref!!.edit().putString("deviceUUID", deviceUUID).apply()
        }
        connectPeer()
        val p2pStateObserver = Observer<String> { stateStr ->
            //if (stateStr == "online") this.startActivity(Intent(this, BrowserActivity::class.java))
        }
        mainViewModel!!.p2pState.observe(this, p2pStateObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (localPeer != null) {
            localPeer!!.dispose()
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplication1Theme {
        Greeting("Android")
    }
}