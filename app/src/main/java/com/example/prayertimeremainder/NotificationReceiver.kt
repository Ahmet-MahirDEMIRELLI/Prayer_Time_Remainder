package com.example.prayertimeremainder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            "STOP_MEDIA_PLAYER" -> {
                // Handle cancel action
                Log.d("NotificationReceiver","cancel intent received")
                val mediaPlayer = AlarmReceiver.getMediaPlayerInstance()
                if (mediaPlayer != null) {
                    Log.d("NotificationReceiver","media not null")
                    if (mediaPlayer.isPlaying) {
                        Log.d("NotificationReceiver","media playing")
                        try {
                            val durationInMillis = mediaPlayer.duration
                            mediaPlayer.seekTo(durationInMillis - 1000)
                            Log.d("NotificationReceiver","seek to " + durationInMillis/1000)
                            // İşlem başarılı olduysa bildirim gösterme işlemini iptal et
                            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.cancel(0)
                        } catch (e: IllegalStateException) {
                            Log.e("NotificationReceiver", "IllegalStateException during seekTo", e)
                        }
                    } else {
                        Log.e("NotificationReceiver", "MediaPlayer is not ready or playing")
                    }
                }
            }
        }
    }
}
