package com.example.myapplication1.p2pNet

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.myapplication1.MainActivity
import com.example.myapplication1.R

class MusicPlayerService : Service() {
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate() {
        super.onCreate()
        // Initialize MediaPlayer with an audio file (e.g., from raw resources)
        //mediaPlayer = MediaPlayer.create(this, R.raw.my_audio_file)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start playing the audio
        mediaPlayer.start()

        // Create a notification for the foreground service
        val notification = createNotification()
        startForeground(1, notification)

        return START_STICKY // Service will be restarted if killed by the system
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release MediaPlayer resources
        mediaPlayer.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not a bound service
    }

    private fun createNotification(): Notification {
        // Create a notification channel (for Android 8.0 and higher)
        // ...

        // Create a pending intent for the notification action (e.g., open the app)
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        // Build the notification
        return NotificationCompat.Builder(this, "99333")
            .setContentTitle("Music Player")
            .setContentText("Playing music")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}