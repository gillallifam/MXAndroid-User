package br.com.marketpix.mxuser.p2pNet

import androidx.lifecycle.viewModelScope
import br.com.marketpix.mxuser.types.CmdResp
import kotlinx.coroutines.launch

fun handleBroadcast(cmdResp: CmdResp) {
    when (cmdResp.cmd) {
        "prodsUpdate" -> {
            p2pViewModel!!.viewModelScope.launch {
                val updateTime = p2pApi!!.shopLastUpdate()
                if(!updateTime.isNullOrEmpty()){
                    val updateNum = updateTime.toLong()
                    if (shopLastUpdate < updateNum) {
                        p2pViewModel!!.viewModelScope.launch {
                            updateCaches(shopLastUpdate, updateTime)
                        }
                    }
                }
            }
        }
    }
}

