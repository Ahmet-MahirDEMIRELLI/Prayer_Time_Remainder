package com.example.prayertimeremainder

import android.app.AlarmManager
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
import androidx.core.app.NotificationCompat
//import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var notificationManager: NotificationManager? = null
    private var notificationId: Int = 1
    private var notificationChannelId = "media_player_channel"
    private var language: String = ""
    private var type: String = ""
    private var audio: String = ""
    private var requestCode: Int = 0
    override fun onReceive(context: Context, intent: Intent) {
        //Log.d("AlarmReceiver", "received")
        requestCode = intent.getIntExtra("REQUEST_CODE", 0)
        language = intent.getStringExtra("Language").toString()
        type = intent.getStringExtra("Type").toString()
        audio = intent.getStringExtra("Audio").toString()
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        setMediaPlayerInstance(mediaPlayer!!)
        createNotificationChannel(context)
        //Log.d("Alarm Receiver", "$type $language $requestCode")
        if(type == "alarm"){
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
                    if(audio == "Azan"){
                        when(requestCode){
                            1-> rawUri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.morning)
                            2-> rawUri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.noon)
                            3-> rawUri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.afternoon)
                            4-> rawUri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.evening)
                            5-> rawUri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.night)
                        }
                    }

                    mediaPlayer?.setDataSource(context, rawUri)

                    mediaPlayer?.prepareAsync()

                    mediaPlayer?.setOnPreparedListener {
                        //Log.d("AlarmReceiver", "MediaPlayer prepared, starting playback")
                        showNotificationAlarm(context)
                        mediaPlayer?.start()
                    }

                    mediaPlayer?.setOnCompletionListener {
                        //Log.d("AlarmReceiver", "MediaPlayer playback completed")
                        mediaPlayer?.release()
                        mediaPlayer = null

                        audioManager?.abandonAudioFocusRequest(focusRequest)
                        cancelNotification()
                        deleteAlarm(context, requestCode)
                        showNotification(context)
                    }
                } catch (e: Exception) {
                    //Log.e("AlarmReceiver", "Error setting data source", e)
                    e.printStackTrace()
                }
            } else {
                //Log.e("AlarmReceiver", "Failed to gain audio focus")
            }
        }
        else{
            showNotification(context)
            deleteAlarm(context,requestCode)
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
    private fun deleteAlarm(context: Context,requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val sharedPref = context.getSharedPreferences("ALARMS", Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                remove("time_$requestCode")
                //Log.d("Alarm Receiver", "removed $requestCode")
                apply()
            }
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
    private fun createNotificationChannel(context: Context) {
        val channelName = "Media Player"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(notificationChannelId, channelName, importance)
        notificationManager?.createNotificationChannel(channel)
    }

    private fun showNotificationAlarm(context: Context) {
        val notificationIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "STOP_MEDIA_PLAYER"
        }
        val pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, notificationChannelId)
            .setContentTitle("Prayer Time Remainder")
            .setSmallIcon(R.drawable.rose)
        if (language == "TR") {
            builder.setContentText(getPrayerName() + " Ezanı Vakti")
                .addAction(R.drawable.ic_media_pause, "Ezanı Durdur", pendingIntent)
        } else {
            builder.setContentText(getPrayerName() + " Prayer Time")
                .addAction(R.drawable.ic_media_pause, "Cancel Prayer Call", pendingIntent)
        }
        val notification = builder.build()
        notificationManager?.notify(notificationId, notification)
    }
    private fun showNotification(context: Context) {
        val builder = NotificationCompat.Builder(context, notificationChannelId)
            .setContentTitle("Prayer Time Reminder")
            .setSmallIcon(R.drawable.rose)

        if (language == "TR") {
            builder.setContentText(getPrayerName() + " Ezanı Vakti")
        } else {
            builder.setContentText(getPrayerName() + " Prayer Time")
        }
        val notification = builder.build()
        notificationManager?.notify(notificationId, notification)
    }

    private fun getPrayerName(): String{
        if(language == "TR"){
            when(requestCode){
                1-> return "Sabah"
                2-> return "Öğle"
                3-> return "İkindi"
                4-> return "Akşam"
                5-> return "Yatsı"
            }
        }
        else{
            when(requestCode){
                1-> return "Morning"
                2-> return "Noon"
                3-> return "Afternoon"
                4-> return "Evening"
                5-> return "Night"
            }
        }
        return ""
    }
    private fun cancelNotification() {
        notificationManager?.cancel(notificationId)
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
