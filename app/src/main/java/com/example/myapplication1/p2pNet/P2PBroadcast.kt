package com.example.myapplication1.p2pNet

import com.example.myapplication1.types.CmdResp

fun handleBroadcast(cmdResp: CmdResp) {
    when (cmdResp.cmd) {
        "prodsUpdate" -> {
            p2pApi!!.shopLastUpdate().thenAccept { updateTime ->
                val updateNum = updateTime.toLong()
                if (shopLastUpdate < updateNum) {
                    updateCaches(shopLastUpdate, updateTime)
                }
            }
        }
    }
}

