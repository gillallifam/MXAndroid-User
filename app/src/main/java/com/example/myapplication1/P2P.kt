package com.example.myapplication1

import Cmd
import CustomSDPClass
import OfferExtras
import PackedOffer
import android.util.Log
import android.widget.Toast
import com.example.myapplication1.NetworkUtils.Companion.shopApi
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


var localPeer: PeerConnection? = null
var dataChannel: DataChannel? = null
var localOffer: CustomSDPClass? = null
val iceServers: MutableList<IceServer> = ArrayList()
var deviceUUID = ""
var mainViewModel: MainViewModel? = null
var browserViewModel: BrowserViewModel? = null
var peerConnectionFactory: PeerConnectionFactory? = null
var mainContext: MainActivity? = null

fun prepareP2P(context: MainActivity) {
    mainContext = context
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
        Toast.makeText(mainContext, "Peer start", Toast.LENGTH_SHORT).show()
        mainViewModel!!.p2pState.value = "connecting"
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
                    TODO("Not yet implemented")
                }

                override fun onCreateFailure(p0: String?) {
                    TODO("Not yet implemented")
                }

                override fun onSetFailure(p0: String?) {
                    TODO("Not yet implemented")
                }
            }, mediaConstraints
        )
    } else {
        Toast.makeText(
            mainContext,
            "Peer status ${localPeer!!.connectionState().name}",
            Toast.LENGTH_SHORT
        ).show()
    }
}

fun sendData(data: String) {
    if (dataChannel!!.state().name == "OPEN") {
        val buffer = ByteBuffer.wrap(data.toByteArray())
        dataChannel!!.send(DataChannel.Buffer(buffer, false))
    } else {
        println("Data channel not open")
    }
}

fun handleCmd(cmd: Cmd) {
    when (cmd.cmd) {
        "reqImg" -> {
            val b64 = cmd.load!!.img!!.replace("data:image/webp;base64,", "")
                .replace("data:image/jpeg;base64,", "").replace("data:image/png;base64,", "");
            val bitmap = decodePicString(b64)
            mainViewModel!!.logoImage = bitmap
        }

        else -> println(cmd)
    }
}

fun getDataChannelObserver(dataChannel: DataChannel): DataChannel.Observer {
    val dcO = object : DataChannel.Observer {
        override fun onBufferedAmountChange(amount: Long) {
            println("Bytes received $amount")
        }

        override fun onStateChange() {
            val state = dataChannel.state()
            println("DataChannel: onStateChange: $state")
            if (state.name == "CLOSED") {
                println(state)
                dataChannel.dispose()
            }
        }

        override fun onMessage(buffer: DataChannel.Buffer?) {
            val data: ByteBuffer = buffer!!.data
            val bytes = ByteArray(data.remaining())
            data.get(bytes);
            val resp = String(bytes)
            val cmd = gson.fromJson(resp, Cmd::class.java)
            println(cmd)
            handleCmd(cmd)
        }
    }
    return dcO
}

fun getLocalSdpObserver(peer: PeerConnection): SdpObserver {
    val sdpObserver = object : SdpObserver {
        override fun onCreateSuccess(sessionDescription: SessionDescription) {
            peer.setLocalDescription(object : SdpObserver {
                override fun onCreateSuccess(p0: SessionDescription?) {
                    TODO("Not yet implemented")
                }

                override fun onSetSuccess() {
                    TODO("Not yet implemented")
                }

                override fun onCreateFailure(p0: String?) {
                    TODO("Not yet implemented")
                }

                override fun onSetFailure(p0: String?) {
                    TODO("Not yet implemented")
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
                    TODO("Not yet implemented")
                }

                override fun onSetSuccess() {
                    TODO("Not yet implemented")
                }

                override fun onCreateFailure(p0: String?) {
                    TODO("Not yet implemented")
                }

                override fun onSetFailure(p0: String?) {
                    TODO("Not yet implemented")
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

    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }
    val pcObserver: PeerConnection.Observer = object : PeerConnection.Observer {
        val TAG: String = "PEER_CONNECTION_FACTORY"

        override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
            Log.d(TAG, "onSignalingChange")
        }

        override fun onIceConnectionChange(onIceConnectionChange: PeerConnection.IceConnectionState) {
            val state = onIceConnectionChange.name
            Log.d(TAG, "onIceConnectionChange ${onIceConnectionChange.name}")
            if (state == "DISCONNECTED") {
                val state1 = localPeer!!.connectionState().name
                println(state1)
                println("Peer disconnected.")
                mainViewModel!!.p2pState.postValue("offline")
                localPeer = null
            }
            if (state == "FAILED") {
                val state2 = localPeer!!.connectionState().name
                println(state2)
                println("Peer failed.")
                mainViewModel!!.p2pState.postValue("offline")
                localPeer = null
            }
            if (state == "COMPLETED") {
                mainViewModel!!.p2pState.postValue("online")
            }
        }

        override fun onIceConnectionReceivingChange(b: Boolean) {
            Log.d(TAG, "onIceConnectionReceivingChange")
        }

        override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
            Log.d(TAG, "onIceGatheringChange")
            if (iceGatheringState.name == "COMPLETE") {
                val extras = OfferExtras(
                    peerOwner = "${deviceUUID}>tst@android.mktpix",
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
                    Log.d("offer result: ", result.body().toString())
                }
            }
        }

        override fun onIceCandidate(iceCandidate: IceCandidate) {
            //println("AN ICE CANDIDATE HAS BEEN DISCOVERED")
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

