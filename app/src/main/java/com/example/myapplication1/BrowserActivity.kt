package com.example.myapplication1

import Cmd
import Load
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication1.ui.theme.MyApplication1Theme


class BrowserActivity : ComponentActivity() {
    init{
        val cmd = Cmd(
            pid = genPid(),
            cmd = "reqProducts",
            age = 0,
            from = "${deviceUUID}>tst@android.mktpix"
        )
        val payload = gson.toJson(cmd)
        sendData(payload)
    }
    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        browserViewModel = ViewModelProvider(this)[BrowserViewModel::class.java]

        setContent {
            MyApplication1Theme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Browser Activity",
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        }
    }
}

