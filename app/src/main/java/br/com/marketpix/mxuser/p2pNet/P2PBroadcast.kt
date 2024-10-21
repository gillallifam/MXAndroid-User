package br.com.marketpix.mxuser.p2pNet

import br.com.marketpix.mxuser.p2pNet.p2pApi
import br.com.marketpix.mxuser.p2pNet.shopLastUpdate
import br.com.marketpix.mxuser.types.CmdResp

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

