package com.example.prayertimeremainder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var notificationManager: NotificationManager? = null
    private var notificationId: Int = 1
    private var notificationChannelId = "media_player_channel"
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "received")
        val requestCode = intent.getIntExtra("REQUEST_CODE", 0)

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        setMediaPlayerInstance(mediaPlayer!!)
        createNotificationChannel(context)

        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
            .setOnAudioFocusChangeListener(afChangeListener)
            .build()

        val result = audioManager?.requestAudioFocus(focusRequest)

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            try {
                var rawUri :Uri =Uri.parse("android.resource://" + context.packageName + "/" + R.raw.morning_glory_samsung)
                when(requestCode){
                    1-> rawUri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.morning)
                    2-> rawUri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.noon)
                    3-> rawUri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.afternoon)
                    4-> rawUri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.evening)
                    5-> rawUri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.night)
                }

                mediaPlayer?.setDataSource(context, rawUri)

                mediaPlayer?.prepareAsync()

                mediaPlayer?.setOnPreparedListener {
                    Log.d("AlarmReceiver", "MediaPlayer prepared, starting playback")
                    showNotification(context)
                    mediaPlayer?.start()
                }

                mediaPlayer?.setOnCompletionListener {
                    Log.d("AlarmReceiver", "MediaPlayer playback completed")
                    mediaPlayer?.release()
                    mediaPlayer = null

                    audioManager?.abandonAudioFocusRequest(focusRequest)
                    cancelNotification()
                }
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error setting data source", e)
                e.printStackTrace()
            }
        } else {
            Log.e("AlarmReceiver", "Failed to gain audio focus")
        }
    }

    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }
    }
    private fun createNotificationChannel(context: Context) {
        val channelName = "Media Player"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(notificationChannelId, channelName, importance)
        notificationManager?.createNotificationChannel(channel)
    }

    private fun showNotification(context: Context) {
        val notificationIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "STOP_MEDIA_PLAYER"
        }
        val pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, notificationChannelId)
            .setContentTitle("Prayer Time Remainder")
            .setContentText("Ezan Okunuyor/Call to Prayer")
            .setSmallIcon(R.drawable.notification_icon)
            .addAction(R.drawable.ic_media_pause, "Kapat/Cancel", pendingIntent)
            .build()

        notificationManager?.notify(notificationId, notification)
    }

    private fun cancelNotification() {
        notificationManager?.cancel(notificationId)
    }
    fun getMediaPlayerInstance(): MediaPlayer? {
        return mediaPlayer
    }
    companion object {
        private var mediaPlayer: MediaPlayer? = null

        fun getMediaPlayerInstance(): MediaPlayer? {
            return mediaPlayer
        }

        fun setMediaPlayerInstance(mp: MediaPlayer) {
            mediaPlayer = mp
        }
    }

}
