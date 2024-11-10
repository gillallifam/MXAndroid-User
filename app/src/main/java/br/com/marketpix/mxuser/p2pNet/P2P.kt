package br.com.marketpix.mxuser.p2pNet

import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import br.com.marketpix.mxuser.MainActivity
import br.com.marketpix.mxuser.NetworkUtils.Companion.shopApi
import br.com.marketpix.mxuser.R
import br.com.marketpix.mxuser.genPid
import br.com.marketpix.mxuser.gson
import br.com.marketpix.mxuser.timeID
import br.com.marketpix.mxuser.types.Category
import br.com.marketpix.mxuser.types.Cmd
import br.com.marketpix.mxuser.types.CmdResp
import br.com.marketpix.mxuser.types.CustomSDPClass
import br.com.marketpix.mxuser.types.OfferExtras
import br.com.marketpix.mxuser.types.PackedOffer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.IceServer
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SessionDescription.Type
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

var dataChannel: DataChannel? = null
var localOffer: CustomSDPClass? = null
val iceServers: MutableList<IceServer> = ArrayList()
var deviceUUID = ""
var p2pViewModel: P2PViewModel? = null
var peerConnectionFactory: PeerConnectionFactory? = null
var mainContext: MainActivity? = null
var p2pApi: P2PApi? = null
const val peerAutoReconnect = true
var p2pPrefs: SharedPreferences? = null
var shopLastUpdate: Long = 0
var targetShop = "LojaExemplo1"
lateinit var mediaPlayer1: MediaPlayer
lateinit var mediaPlayer2: MediaPlayer

val promises: MutableMap<String, CompletableFuture<String>> = mutableMapOf()

val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
    throwable.printStackTrace()
}

fun startP2P(context: MainActivity) {
    PeerConnectionFactory.initialize(
        PeerConnectionFactory.InitializationOptions.builder(context).createInitializationOptions()
    )
    val options = PeerConnectionFactory.Options()
    options.disableNetworkMonitor = true
    //Logging.enableLogToDebugOutput(Logging.Severity.LS_VERBOSE)
    peerConnectionFactory =
        PeerConnectionFactory.builder().setOptions(options).createPeerConnectionFactory()
    /*,
    "stun:stun01.sipphone.com",
    "stun:stun.ideasip.com",
    "stun:stun.iptel.org",
    "stun:stun.softjoys.com",
    "stun:numb.viagenie.ca:3478",
    "stun:s2.taraba.net:3478",
    "stun:stun.alltel.com.au:3478",
    "stun:stun.avigora.com:3478",
    "stun:stun.budgetphone.nl:3478",
    "stun:stun.cbsys.net:3478",
     */
    val svAddress = arrayOf(
        //"stun:stun.l.google.com:19302",
        //"stun:devserver.marketpix.com.br:3478",
        "stun:global.stun.twilio.com:3478",
        //"stun:stun.iptel.org",
        //"stun:stun.ideasip.com",
        //"stun:stun01.sipphone.com",
    )
    svAddress.forEach {
        val peerIceServer1 = IceServer.builder(it)
            //.setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK)
            //.setUsername("x")
            //.setPassword("x")
            .createIceServer()
        iceServers.add(peerIceServer1)
    }
}

var localPeer: PeerConnection? = null
var connectTimer1 = 0
var connectTimer2= 0
var candCount = 0
var connected = false

fun connectPeer() {
    connectTimer1 = java.time.Instant.now().toEpochMilli().toInt()
    if (localPeer == null) {
        p2pViewModel!!.p2pState.value = "connecting"

        localPeer = peerConnectionFactory!!.createPeerConnection(iceServers, getPCObserver())

        dataChannel = localPeer!!.createDataChannel(
            "dataChannel-${timeID()}", DataChannel.Init()
        )
        dataChannel!!.registerObserver(getDataChannelObserver(dataChannel!!))

        val mediaConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("IceRestart", "true"))
        }
        localPeer!!.createOffer(
            object : SdpObserver {
                override fun onCreateSuccess(sdpOffer: SessionDescription) {
                    localPeer!!.setLocalDescription(getLocalSdpObserver(localPeer!!), sdpOffer)
                }

                override fun onSetSuccess() {
                    println("Offer set success")
                }

                override fun onCreateFailure(p0: String?) {
                    println("Offer create failed")
                }

                override fun onSetFailure(p0: String?) {
                    println("Offer set failed")
                }
            }, mediaConstraints
        )
    }
}

fun sendData(cmd: Cmd, timeout: Long = 5000): CompletableFuture<String> {
    val promise = CompletableFuture<String>()
    Handler(Looper.getMainLooper()).postDelayed({
        promises[cmd.pid]?.let {
            if (cmd.attempts == cmd.retry) {
                it.complete("")
                promises.remove(cmd.pid)
            } else {
                cmd.attempts += 1
                sendData(cmd)
            }
        }
    }, timeout)

    if (dataChannel != null && dataChannel!!.state().name == "OPEN") {
        val pid = genPid()
        cmd.pid = pid
        promises[pid] = promise
        val payload = gson.toJson(cmd)
        val buffer = ByteBuffer.wrap(payload.toByteArray())
        dataChannel!!.send(DataChannel.Buffer(buffer, false))
    } else {
        println(cmd.cmd)
        println("Data channel not open")
    }
    return promise
}

fun receiveData(buffer: DataChannel.Buffer?) {
    val data: ByteBuffer = buffer!!.data
    val bytes = ByteArray(data.remaining())
    data.get(bytes)
    val resp = String(bytes)
    val cmdResp = gson.fromJson(resp, CmdResp::class.java)
    if (cmdResp.status == "broadcast") {
        handleBroadcast(cmdResp)
        return
    }
    if (cmdResp.status != "wait") {
        promises[cmdResp.pid]?.let {
            it.complete(cmdResp.content)
            promises.remove(cmdResp.pid)
        }
    }
}

fun getDataChannelObserver(dataChannel: DataChannel): DataChannel.Observer {
    val dcO = object : DataChannel.Observer {
        override fun onBufferedAmountChange(amount: Long) {}

        override fun onStateChange() {
            val state = dataChannel.state()
            if (state.name == "OPEN") {
                p2pViewModel!!.viewModelScope.launch {
                    val updateTime = p2pApi!!.shopLastUpdate()
                    if (!updateTime.isNullOrEmpty()) {
                        val updateNum = updateTime.toLong()
                        if (shopLastUpdate < updateNum) {
                            p2pViewModel!!.viewModelScope.launch {
                                updateCaches(shopLastUpdate, updateTime)
                            }
                        }
                    }
                }
            }
            if (state.name == "CLOSED") {
                dataChannel.dispose()
            }
        }

        override fun onMessage(buffer: DataChannel.Buffer?) {
            receiveData(buffer)
        }
    }
    return dcO
}

fun getLocalSdpObserver(peer: PeerConnection): SdpObserver {
    val sdpObserver = object : SdpObserver {
        override fun onCreateSuccess(sessionDescription: SessionDescription) {
            peer.setLocalDescription(object : SdpObserver {
                override fun onCreateSuccess(p0: SessionDescription?) {
                    println("Local description create success")
                }

                override fun onSetSuccess() {
                    println("Local description set success")
                }

                override fun onCreateFailure(p0: String?) {
                    println("Local description create failure")
                }

                override fun onSetFailure(p0: String?) {
                    println("Local description set failure")
                }
            }, sessionDescription)
        }

        override fun onSetSuccess() {}

        override fun onCreateFailure(p0: String?) {
            println(p0)
        }

        override fun onSetFailure(p0: String?) {
            println(p0)
        }
    }
    return sdpObserver
}

fun getRemoteSdpObserver(peer: PeerConnection): SdpObserver {
    val sdpObserver = object : SdpObserver {
        override fun onCreateSuccess(sessionDescription: SessionDescription) {
            peer.setLocalDescription(object : SdpObserver {
                override fun onCreateSuccess(p0: SessionDescription?) {
                    println("Remote description create success")
                }

                override fun onSetSuccess() {
                    println("Remote description set success")
                }

                override fun onCreateFailure(p0: String?) {
                    println("Remote description create failed")
                }

                override fun onSetFailure(p0: String?) {
                    println("Remote description set failed")
                }
            }, sessionDescription)
        }

        override fun onSetSuccess() {}

        override fun onCreateFailure(p0: String?) {
            println(p0)
        }

        override fun onSetFailure(p0: String?) {
            println(p0)
        }
    }
    return sdpObserver
}

@OptIn(DelicateCoroutinesApi::class)
fun startConnection(){
    val extras = OfferExtras(
        peerOwner = "$deviceUUID>$deviceUUID@android.mktpix",
        shop = targetShop,
        userid = deviceUUID,
        deviceUUID = deviceUUID,
        username = "android",
        userpass = "mktpix",
        profile = "{name: 'User99999'}"
    )
    localOffer = CustomSDPClass(
        type = "offer",
        sdp = localPeer!!.localDescription.description,
        extras = extras
    )
    GlobalScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
        val packed = localOffer?.let { PackedOffer(offer = it) }
        try {
            /*val respUrl =
                shopApi.findServer("https://api2.marketpix.com.br:9510/shopServer/MarketPix1")
            val url = "https://${respUrl.body()}/makeOffer"
            println(url)
            val result = shopApi.makeOffer2(url, packed)*/
            val result = shopApi.makeOffer(packed)
            if (result.isSuccessful) {
                val optionsJson = result.body()!!.getAsJsonObject("OPTIONS")
                //println(optionsJson)
                if (optionsJson == null) {
                    mainContext!!.runOnUiThread {
                        Toast.makeText(mainContext, "Loja offline", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    //val compressed = optionsJson.get("compressed").asString
                    //println(compressed)
                    //val decompressed = decompressFromUTF16(compressed)
                    //println(decompressed)
                    val cats = optionsJson.getAsJsonObject("shopCats")
                    //println(cats.keySet())
                    val catMap = mutableMapOf<String, Category>().apply {
                        cats.keySet().forEach {
                            put(
                                it,
                                gson.fromJson(cats[it], Category::class.java)
                            )
                        }
                    }
                    //println(catMap)
                    p2pViewModel!!.filterCategories.clear()
                    p2pViewModel!!.filterCategories.addAll(catMap.values)
                    //val catsArr = catMap.values.map { it.name }
                    //println(catsArr)
                    //filterOptions = catsArr

                    val answerJson = result.body()!!.getAsJsonObject("answer")
                    if (answerJson != null) {
                        val customAnswer =
                            gson.fromJson(answerJson, CustomSDPClass::class.java)
                        localPeer!!.setRemoteDescription(
                            getRemoteSdpObserver(localPeer!!),
                            SessionDescription(Type.ANSWER, customAnswer.sdp)
                        )
                    }
                }
            }
        } catch (e: Exception) {
            p2pViewModel!!.p2pState.value = "offline"
            mainContext!!.runOnUiThread {
                Toast.makeText(mainContext, "Verify internet", Toast.LENGTH_SHORT)
                    .show()
            }
            println(e)
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun getPCObserver(): PeerConnection.Observer {
    val pcObserver: PeerConnection.Observer = object : PeerConnection.Observer {
        val TAG: String = "PEER_CONNECTION_FACTORY"

        override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
            Log.d(TAG, "onSignalingChange")
            val state = signalingState.name
            if (peerAutoReconnect) {
                if (state == "DISCONNECTED" || state == "CLOSED") {
                    mainContext!!.runOnUiThread {
                        Handler(Looper.getMainLooper()).postDelayed({
                            connectPeer()
                        }, 5000)
                    }
                }
            }
        }

        override fun onIceConnectionChange(onIceConnectionChange: PeerConnection.IceConnectionState) {
            val state = onIceConnectionChange.name
            Log.d(TAG, "onIceConnectionChange ${onIceConnectionChange.name}")
            //val localPeer = P2PFgService.instance!!.localPeer
            if (state == "DISCONNECTED") {
                val state1 = localPeer!!.connectionState().name
                //println(state1)
                println("Peer disconnected.")
                p2pViewModel!!.p2pState.value = "offline"
                localPeer!!.dispose()
                localPeer = null
            }
            if (state == "FAILED") {
                val state2 = localPeer!!.connectionState().name
                //println(state2)
                println("Peer failed.")
                p2pViewModel!!.p2pState.value = "offline"
                localPeer = null
            }
            if (state == "COMPLETED") {
                mediaPlayer1 = MediaPlayer.create(
                    mainContext,
                    R.raw.a2
                )
                mediaPlayer1.start()
                p2pViewModel!!.p2pState.value = "online"
                println(java.time.Instant.now().toEpochMilli().toInt() - connectTimer1)
                println(java.time.Instant.now().toEpochMilli().toInt() - connectTimer2)
            }
        }

        override fun onIceConnectionReceivingChange(b: Boolean) {
            Log.d(TAG, "onIceConnectionReceivingChange")
        }

        override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
            Log.d(TAG, "onIceGatheringChange ${iceGatheringState.name}")

            if (iceGatheringState.name == "COMPLETE") {
                startConnection()
            }
            /*if (iceGatheringState.name == "COMPLETE") {
                connectTimer2 = java.time.Instant.now().toEpochMilli().toInt()
                val extras = OfferExtras(
                    peerOwner = "$deviceUUID>$deviceUUID@android.mktpix",
                    shop = targetShop,
                    userid = deviceUUID,
                    deviceUUID = deviceUUID,
                    username = "android",
                    userpass = "mktpix",
                    profile = "{name: 'User99999'}"
                )
                localOffer = CustomSDPClass(
                    type = "offer",
                    sdp = localPeer!!.localDescription.description,
                    extras = extras
                )
                GlobalScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                    val packed = localOffer?.let { PackedOffer(offer = it) }
                    try {
                        *//*val respUrl =
                            shopApi.findServer("https://api2.marketpix.com.br:9510/shopServer/MarketPix1")
                        val url = "https://${respUrl.body()}/makeOffer"
                        println(url)
                        val result = shopApi.makeOffer2(url, packed)*//*
                        val result = shopApi.makeOffer(packed)
                        if (result.isSuccessful) {
                            val optionsJson = result.body()!!.getAsJsonObject("OPTIONS")
                            //println(optionsJson)
                            if (optionsJson == null) {
                                mainContext!!.runOnUiThread {
                                    Toast.makeText(mainContext, "Loja offline", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            } else {
                                //val compressed = optionsJson.get("compressed").asString
                                //println(compressed)
                                //val decompressed = decompressFromUTF16(compressed)
                                //println(decompressed)
                                val cats = optionsJson.getAsJsonObject("shopCats")
                                //println(cats.keySet())
                                val catMap = mutableMapOf<String, Category>().apply {
                                    cats.keySet().forEach {
                                        put(
                                            it,
                                            gson.fromJson(cats[it], Category::class.java)
                                        )
                                    }
                                }
                                //println(catMap)
                                p2pViewModel!!.filterCategories.clear()
                                p2pViewModel!!.filterCategories.addAll(catMap.values)
                                //val catsArr = catMap.values.map { it.name }
                                //println(catsArr)
                                //filterOptions = catsArr

                                val answerJson = result.body()!!.getAsJsonObject("answer")
                                if (answerJson != null) {
                                    val customAnswer =
                                        gson.fromJson(answerJson, CustomSDPClass::class.java)
                                    localPeer!!.setRemoteDescription(
                                        getRemoteSdpObserver(localPeer!!),
                                        SessionDescription(Type.ANSWER, customAnswer.sdp)
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        p2pViewModel!!.p2pState.value = "offline"
                        mainContext!!.runOnUiThread {
                            Toast.makeText(mainContext, "Verify internet", Toast.LENGTH_SHORT)
                                .show()
                        }
                        println(e)
                    }
                }
            }*/
        }

        override fun onIceCandidate(iceCandidate: IceCandidate) {
            try {
                if (!iceCandidate.sdp.contains("127.0.0.1") && !iceCandidate.sdp.contains("::1")) {
                    localPeer?.addIceCandidate(iceCandidate)
                    //println(localPeer?.localDescription)
                    candCount += 1
                    /*if (candCount == 2) {
                        onIceGatheringChange(PeerConnection.IceGatheringState.COMPLETE)
                        println("Force ice complete")
                        candCount = 0
                        startConnection()
                    }*/
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
            Log.d(TAG, "onIceCandidatesRemoved")
        }

        override fun onAddStream(mediaStream: MediaStream) {
            if (mediaStream.videoTracks.size == 0) {
                Log.d("onAddStream", "NO REMOTE STREAM")
                println("NO REMOTE STREAM (PRINTLN)")
            }
        }

        override fun onRemoveStream(mediaStream: MediaStream) {
            Log.d(TAG, "onRemoveStream")
        }

        override fun onDataChannel(dataChannel: DataChannel) {
            Log.d(TAG, "onDataChannel")
        }

        override fun onRenegotiationNeeded() {
            Log.d(TAG, "onRenegotiationNeeded")
        }

        override fun onAddTrack(rtpReceiver: RtpReceiver, mediaStreams: Array<MediaStream>) {
            Log.d(TAG, "onAddTrack")
        }
    }
    return pcObserver
}

