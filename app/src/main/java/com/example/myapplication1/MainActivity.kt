package com.example.myapplication1

import Cmd
import Load
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
import com.example.myapplication1.ui.theme.MyApplication1Theme


class MainActivity : ComponentActivity() {

    private var sharedPref: SharedPreferences? = null

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        prepareP2P(this)
        val serviceIntent = Intent(this, P2PService::class.java)
        startService(serviceIntent)

        setContent {
            MyApplication1Theme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Android webRTC",
                        modifier = Modifier.padding(20.dp)
                    )
                    mainViewModel!!.p2pState.value?.let {
                        Text(
                            text = it,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                    Button(
                        //enabled = viewModel!!.p2pState == "offline",
                        onClick = {
                            connectPeer()
                        }) {
                        Text("Connect")
                    }
                    Button(onClick = {
                        //val cmd = Cmd("testingUser", "${deviceUUID}>tst@android.mktpix")
                        val cmd = Cmd(
                            pid = genPid(),
                            cmd = "reqImg",
                            from = "${deviceUUID}>tst@android.mktpix",
                            load = Load(cod = "00000000")
                        )
                        val payload = gson.toJson(cmd)
                        sendData(payload)
                    }) {
                        Text("Msg")
                    }
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
            if (stateStr == "online") this.startActivity(Intent(this, BrowserActivity::class.java))
        }
        mainViewModel!!.p2pState.observe(this, p2pStateObserver)
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