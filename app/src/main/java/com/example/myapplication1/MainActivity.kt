package com.example.myapplication1

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
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
import com.example.myapplication1.p2pNet.P2PAPI
import com.example.myapplication1.p2pNet.P2PService2
import com.example.myapplication1.p2pNet.P2PViewModel
import com.example.myapplication1.p2pNet.connectPeer
import com.example.myapplication1.p2pNet.deviceUUID
import com.example.myapplication1.p2pNet.disconnectPeer
import com.example.myapplication1.p2pNet.localPeer
import com.example.myapplication1.p2pNet.mainContext
import com.example.myapplication1.p2pNet.p2pApi
import com.example.myapplication1.p2pNet.p2pViewModel
import com.example.myapplication1.ui.theme.MyApplication1Theme


class MainActivity : ComponentActivity() {

    private var sharedPref: SharedPreferences? = null
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

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        p2pViewModel = ViewModelProvider(this)[P2PViewModel::class.java]
        val servRunning = isMyServiceRunning(P2PService2::class.java)
        val serviceIntent = Intent(this, P2PService2::class.java)
        val serv = startService(serviceIntent)
        p2pApi = P2PAPI.instance
        mainContext = this

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

                    Text(
                        text = p2pViewModel!!.p2pState.value,
                        modifier = Modifier.padding(20.dp)
                    )

                    if (p2pViewModel!!.p2pState.value != "online") {
                        Button(
                            onClick = {
                                connectPeer()
                            }) {
                            Text("Connect")
                        }
                    }else{
                        Button(
                            onClick = {
                                disconnectPeer()
                            }) {
                            Text("Disconnect")
                        }
                    }

                    Button(
                        onClick = {
                            if (p2pViewModel!!.p2pState.value === "online") {
                                startActivity(Intent(this@MainActivity, BrowserActivity::class.java))
                            }
                        }) {
                        Text("Browse")
                    }
                    Button(onClick = {
                        p2pApi!!.peerPing2().thenApply { result ->
                            if (result.isNotEmpty()) mainViewModel!!.dateText = result
                        }
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