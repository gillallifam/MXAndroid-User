package br.com.marketpix.mxuser.p2pNet

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import br.com.marketpix.mxuser.MainActivity
import br.com.marketpix.mxuser.R
import br.com.marketpix.mxuser.timeID
import org.webrtc.DataChannel
import org.webrtc.Logging
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import java.util.Timer
import kotlin.concurrent.timerTask

class P2PFgService : Service() {

    var localPeer: PeerConnection? = null
    private lateinit var notificationManager: NotificationManager
    private var isStarted = false

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        startP2P(this)
        instance = this
        connectPeer()
        if (peerAutoReconnect) {
            Timer().schedule(
                timerTask()
                {
                    if (p2pViewModel!!.p2pState.value != "online") {
                        disconnectPeer()
                        instance!!.connectPeer()
                    }
                }, 30 * 1000, 30 * 1000
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        isStarted = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isStarted) {
            makeForeground()
            isStarted = true
        }
        return START_STICKY
    }

    private fun makeForeground() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        createServiceNotificationChannel()

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Marketpix service")
            .setContentText("Gerenciando conexões")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    private fun createServiceNotificationChannel() {

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Marketpix Service channel",
            NotificationManager.IMPORTANCE_LOW
        )
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager.createNotificationChannel(channel)
    }

    fun connectPeer() {
        if (localPeer == null) {
            p2pViewModel!!.p2pState.value = "connecting"

            val rtcConfig = PeerConnection.RTCConfiguration(
                arrayListOf(
                    // adding google's standard server
                    PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
                )
            ).apply {
                // it's very important to use new unified sdp semantics PLAN_B is deprecated
                sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            }
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

    companion object {
        private const val ONGOING_NOTIFICATION_ID = 100123
        private const val CHANNEL_ID = "100123"
        var instance: P2PFgService? = null

        fun startService(context: Context) {
            val intent = Intent(context, P2PFgService::class.java)
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, P2PFgService::class.java)
            context.stopService(intent)
        }

    }
}