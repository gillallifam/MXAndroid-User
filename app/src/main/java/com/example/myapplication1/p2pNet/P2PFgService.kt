package com.example.myapplication1.p2pNet

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.myapplication1.MainActivity
import com.example.myapplication1.R
import com.example.myapplication1.timeID
import org.webrtc.DataChannel
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

class P2PFgService : Service() {

    var localPeer: PeerConnection? = null
    private lateinit var notificationManager: NotificationManager

    // onStartCommand can be called multiple times, so we keep track of "started" state manually
    private var isStarted = false

    override fun onCreate() {
        super.onCreate()
        // initialize dependencies here (e.g. perform dependency injection)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        startP2P(this)
        instance = this
        connectPeer()
    }

    override fun onDestroy() {
        super.onDestroy()
        isStarted = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // bound Service is a different story
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isStarted) {
            makeForeground()
            // place here any logic that should run just once when the Service is started
            isStarted = true
        }

        // process the command here (e.g. retrieve extras from the Intent and act accordingly)
        val demoString = intent?.getStringExtra(EXTRA_DEMO) ?: ""

        return START_STICKY // makes sense for a Foreground Service, or even START_REDELIVER_INTENT
    }

    private fun makeForeground() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // before calling startForeground, we must create a notification and a corresponding
        // notification channel

        createServiceNotificationChannel()

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Marketpix service")
            .setContentText("Gerenciando conexoes")
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
            mainContext!!.runOnUiThread {
                Toast.makeText(mainContext, "Connecting", Toast.LENGTH_SHORT).show()
            }
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

    companion object {
        private const val ONGOING_NOTIFICATION_ID = 101
        private const val CHANNEL_ID = "1001"
        var instance: P2PFgService? = null
        private const val EXTRA_DEMO = "EXTRA_DEMO"

        fun startService(context: Context, demoString: String) {
            val intent = Intent(context, P2PFgService::class.java)
            intent.putExtra(EXTRA_DEMO, demoString)
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, P2PFgService::class.java)
            context.stopService(intent)
        }

    }
}