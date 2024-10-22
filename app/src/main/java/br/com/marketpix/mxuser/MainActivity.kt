package br.com.marketpix.mxuser

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.Settings.Secure
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import br.com.marketpix.mxuser.p2pNet.P2PAPI
import br.com.marketpix.mxuser.p2pNet.P2PFgService
import br.com.marketpix.mxuser.p2pNet.P2PViewModel
import br.com.marketpix.mxuser.p2pNet.deviceUUID
import br.com.marketpix.mxuser.p2pNet.fillCaches
import br.com.marketpix.mxuser.p2pNet.mainContext
import br.com.marketpix.mxuser.p2pNet.p2pApi
import br.com.marketpix.mxuser.p2pNet.p2pPrefs
import br.com.marketpix.mxuser.p2pNet.p2pViewModel
import br.com.marketpix.mxuser.p2pNet.shopLastUpdate
import br.com.marketpix.mxuser.ui.theme.MXUserTheme

class MainActivity : ComponentActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var mediaPlayer: MediaPlayer

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
        mediaPlayer = MediaPlayer.create(
            this,
            R.raw.a1)
        /*val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId("361472114596-10etkcijb0lkps95kgjo5e5seks5eois.apps.googleusercontent.com")
            .setAutoSelectEnabled(true)
            //.setNonce(<nonce string to use when generating a Google ID token>)
            .build()*/
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
            P2PFgService.startService(this)
        } else {
            P2PFgService.instance!!.connectPeer()
        }

        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
            0
        )

        p2pApi = P2PAPI.instance

        setContent {
            MXUserTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MXUser",
                        modifier = Modifier.padding(20.dp)
                    )

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
                            mediaPlayer.start()
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
    MXUserTheme {
        Greeting("Android")
    }
}