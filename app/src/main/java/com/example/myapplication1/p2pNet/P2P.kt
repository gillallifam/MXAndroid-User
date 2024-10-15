package com.example.myapplication1.p2pNet

import Cmd
import CmdResp
import CustomSDPClass
import OfferExtras
import PackedOffer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.myapplication1.BrowserViewModel
import com.example.myapplication1.MainActivity
import com.example.myapplication1.MainViewModel
import com.example.myapplication1.NetworkUtils.Companion.shopApi
import com.example.myapplication1.genPid
import com.example.myapplication1.gson
import com.example.myapplication1.timeID
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
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

var localPeer: PeerConnection? = null
var dataChannel: DataChannel? = null
var localOffer: CustomSDPClass? = null
val iceServers: MutableList<IceServer> = ArrayList()
var deviceUUID = ""
var p2pViewModel: P2PViewModel? = null
var peerConnectionFactory: PeerConnectionFactory? = null
var mainContext: MainActivity? = null
var p2pApi: P2PAPI? = null
const val peerAutoReconnect = false

val promises: MutableMap<String, CompletableFuture<String>> = mutableMapOf()

val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
    throwable.printStackTrace()
}

fun startP2P(context: P2PService2) {
    PeerConnectionFactory.initialize(
        PeerConnectionFactory.InitializationOptions.builder(context).createInitializationOptions()
    )
    val options = PeerConnectionFactory.Options()
    options.disableNetworkMonitor = true
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
        "stun:stun01.sipphone.com",
        "stun:stun.ideasip.com",
        "stun:stun.iptel.org",
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

fun connectPeer() {
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

fun disconnectPeer() {
    if (localPeer != null) {
        localPeer!!.dispose()
        localPeer = null
        p2pViewModel!!.p2pState.value = "offline"
    }
}

fun sendData(cmd: Cmd, timeout: Long = 5000): CompletableFuture<String> {
    val promise = CompletableFuture<String>()
    Handler(Looper.getMainLooper()).postDelayed({
        promises[cmd.pid]?.let {
            it.complete("")
            promises.remove(cmd.pid)
        }
    }, timeout)

    if (dataChannel!!.state().name == "OPEN") {
        val pid = genPid()
        cmd.pid = pid
        promises[pid] = promise
        val payload = gson.toJson(cmd)
        val buffer = ByteBuffer.wrap(payload.toByteArray())
        dataChannel!!.send(DataChannel.Buffer(buffer, false))
    } else {
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
    //println(cmd)
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
            println("DataChannel: onStateChange: $state")
            if (state.name == "CLOSED") {
                println(state)
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
            if (state == "DISCONNECTED") {
                val state1 = localPeer!!.connectionState().name
                println(state1)
                println("Peer disconnected.")
                p2pViewModel!!.p2pState.value = "offline"
                localPeer!!.dispose()
                localPeer = null
                /*mainContext!!.runOnUiThread(Runnable {
                    Handler(Looper.getMainLooper()).postDelayed({
                        connectPeer()
                    }, 5000)
                })*/
            }
            if (state == "FAILED") {
                val state2 = localPeer!!.connectionState().name
                println(state2)
                println("Peer failed.")
                p2pViewModel!!.p2pState.value = "offline"
                localPeer = null
            }
            if (state == "COMPLETED") {
                p2pViewModel!!.p2pState.value = "online"
            }
        }

        override fun onIceConnectionReceivingChange(b: Boolean) {
            Log.d(TAG, "onIceConnectionReceivingChange")
        }

        override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
            Log.d(TAG, "onIceGatheringChange")
            if (iceGatheringState.name == "COMPLETE") {
                val extras = OfferExtras(
                    peerOwner = "$deviceUUID>tst@android.mktpix",
                    shop = "LojaExemplo1",
                    userid = deviceUUID,
                    deviceUUID = deviceUUID,
                    username = "android",
                    userpass = "mktpix",
                    profile = "{name: 'User99999'}"
                )
                localOffer = CustomSDPClass(
                    type = "offer", sdp = localPeer!!.localDescription.description, extras = extras
                )
                GlobalScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                    val packed = localOffer?.let { PackedOffer(offer = it) }
                    val result = shopApi.makeOffer(packed)
                    if (result.isSuccessful) {
                        val sdpJson = result.body()!!.getAsJsonObject("answer")
                        val customAnswer = gson.fromJson(sdpJson, CustomSDPClass::class.java)
                        localPeer!!.setRemoteDescription(
                            getRemoteSdpObserver(localPeer!!),
                            SessionDescription(Type.ANSWER, customAnswer.sdp)
                        )
                    }
                }
            }
        }

        override fun onIceCandidate(iceCandidate: IceCandidate) {
            try {
                val payload = JSONObject()
                payload.put("sdpMLineIndex", iceCandidate.sdpMLineIndex)
                payload.put("sdpMid", iceCandidate.sdpMid)
                payload.put("candidate", iceCandidate.sdp)
                val candidateObject = JSONObject()
                candidateObject.put("ice", iceCandidate.toString())
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

