package com.example.ezanokuyucu

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.speech)
        mediaPlayer.start()
    }
}
