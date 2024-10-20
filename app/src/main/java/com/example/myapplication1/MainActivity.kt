package com.example.myapplication1

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings.Secure
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
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
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication1.p2pNet.P2PAPI
import com.example.myapplication1.p2pNet.P2PViewModel
import com.example.myapplication1.p2pNet.P2PFgService
import com.example.myapplication1.p2pNet.deviceUUID
import com.example.myapplication1.p2pNet.fillCaches
import com.example.myapplication1.p2pNet.mainContext
import com.example.myapplication1.p2pNet.p2pApi
import com.example.myapplication1.p2pNet.p2pPrefs
import com.example.myapplication1.p2pNet.p2pViewModel
import com.example.myapplication1.p2pNet.peerAutoReconnect
import com.example.myapplication1.p2pNet.shopLastUpdate
import com.example.myapplication1.ui.theme.MyApplication1Theme
import java.util.Timer
import kotlin.concurrent.timerTask

class MainActivity : ComponentActivity() {

    private lateinit var mainViewModel: MainViewModel

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainContext = this
        p2pPrefs = getSharedPreferences("p2pPrefs", MODE_PRIVATE)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        p2pViewModel = ViewModelProvider(this)[P2PViewModel::class.java]
        try {
            getDatabase(applicationContext)
            fillCaches()
        } catch (e: Exception) {
            println(e)
        }

        if (!isMyServiceRunning(P2PFgService::class.java)) {
            P2PFgService.startService(this, "some string you want to pass into the service")
        } else {
            P2PFgService.instance!!.connectPeer()
            //p2pViewModel!!.p2pState.value = "online"
        }

        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
            0
        )


        p2pApi = P2PAPI.instance

        setContent {
            MyApplication1Theme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Android webRTC",
                        modifier = Modifier.padding(20.dp)
                    )

                    /*Text(
                        text = p2pViewModel!!.p2pState.value,
                        modifier = Modifier.padding(20.dp)
                    )*/

                    Button(
                        onClick = {
                            if (p2pViewModel!!.p2pState.value == "offline") {
                                P2PFgService.instance!!.connectPeer()
                            }
                            if (p2pViewModel!!.p2pState.value == "online") {
                                P2PFgService.instance!!.disconnectPeer()
                            }
                        }) {
                        Text(p2pViewModel!!.p2pState.value)
                    }

                    /*if (p2pViewModel!!.p2pState.value == "offline") {
                        Button(
                            onClick = {
                                P2PFgService.instance!!.connectPeer()
                            }) {
                            Text("Connect")
                        }
                    }
                    if (p2pViewModel!!.p2pState.value == "online") {
                        Button(
                            onClick = {
                                P2PFgService.instance!!.disconnectPeer()
                            }) {
                            Text("Disconnect")
                        }
                    }*/

                    Button(
                        onClick = {
                            if (p2pViewModel!!.prodCache2.values.isNotEmpty()) {
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        BrowserActivity::class.java
                                    )
                                )
                            }
                        }) {
                        Text("Browse")
                    }
                    Button(onClick = {
                        p2pApi!!.peerPing2().thenApply { result ->
                            if (result.isNotEmpty()) mainViewModel.dateText = result
                        }
                    }) {
                        Text("Msg")
                    }
                    Text(mainViewModel.dateText)
                }
            }
        }

        val hasId = p2pPrefs!!.getString("deviceUUID", "")
        shopLastUpdate = p2pPrefs!!.getString("shopLastUpdate", "1")!!.toLong()
        if (hasId!!.isNotEmpty()) {
            deviceUUID = hasId
        } else {
            val baseId = timeID()
            val androidId = Secure.getString(this.contentResolver, Secure.ANDROID_ID)
            deviceUUID = "${baseId}-${androidId}"
            p2pPrefs!!.edit().putString("deviceUUID", deviceUUID).apply()
        }
        val p2pStateObserver = Observer<String> { stateStr ->
            //if (stateStr == "online") this.startActivity(Intent(this, BrowserActivity::class.java))
        }
        mainViewModel.p2pState.observe(this, p2pStateObserver)
    }

    /*override fun onDestroy() {
        super.onDestroy()

        if (P2PFgService.instance!!.localPeer != null) {
            P2PFgService.instance!!.localPeer!!.dispose()
        }
    }*/
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