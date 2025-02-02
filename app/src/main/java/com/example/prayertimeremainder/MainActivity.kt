package com.example.prayertimeremainder

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
//import android.util.Log

class MainActivity : AppCompatActivity() {
    private lateinit var linearLayout: LinearLayout
    private lateinit var locationTextView: TextView
    private lateinit var languageTextView: TextView
    private lateinit var time1TextView: TextView
    private lateinit var time2TextView: TextView
    private lateinit var time3TextView: TextView
    private lateinit var time4TextView: TextView
    private lateinit var time5TextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var alarmClockImageView: ImageView
    private lateinit var notificationImageView: ImageView
    private lateinit var time1BinImageView: ImageView
    private lateinit var time2BinImageView: ImageView
    private lateinit var time3BinImageView: ImageView
    private lateinit var time4BinImageView: ImageView
    private lateinit var time5BinImageView: ImageView
    private lateinit var time1AudioImageView: ImageView
    private lateinit var time2AudioImageView: ImageView
    private lateinit var time3AudioImageView: ImageView
    private lateinit var time4AudioImageView: ImageView
    private lateinit var time5AudioImageView: ImageView
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var languageSwitch: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var time1AlarmSwitch: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var time2AlarmSwitch: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var time3AlarmSwitch: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var time4AlarmSwitch: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var time5AlarmSwitch: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var time1NotificationSwitch: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var time2NotificationSwitch: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var time3NotificationSwitch: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var time4NotificationSwitch: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var time5NotificationSwitch: Switch
    private lateinit var setTimersButton: Button
    private lateinit var language:String
    private var time1AlarmChoice: String = "Azan"
    private var time2AlarmChoice: String = "Azan"
    private var time3AlarmChoice: String = "Azan"
    private var time4AlarmChoice: String = "Azan"
    private var time5AlarmChoice: String = "Azan"
    private var errorOnSetTimeFields: Boolean = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        language = intent.getStringExtra("language") ?: "TR"
        linearLayout = findViewById(R.id.linearLayout)
        languageTextView = findViewById(R.id.language_text_view)
        locationTextView = findViewById(R.id.location_text_view)
        dateTextView = findViewById(R.id.date_text_view)
        time1TextView = findViewById(R.id.time1_text_view)
        time2TextView = findViewById(R.id.time2_text_view)
        time3TextView = findViewById(R.id.time3_text_view)
        time4TextView = findViewById(R.id.time4_text_view)
        time5TextView = findViewById(R.id.time5_text_view)
        alarmClockImageView = findViewById(R.id.alarm_clock_image_view)
        notificationImageView = findViewById(R.id.notification_image_view)
        time1AudioImageView = findViewById(R.id.time1_audio_image_view)
        time2AudioImageView = findViewById(R.id.time2_audio_image_view)
        time3AudioImageView = findViewById(R.id.time3_audio_image_view)
        time4AudioImageView = findViewById(R.id.time4_audio_image_view)
        time5AudioImageView = findViewById(R.id.time5_audio_image_view)
        time1BinImageView = findViewById(R.id.time1_bin_image_view)
        time2BinImageView = findViewById(R.id.time2_bin_image_view)
        time3BinImageView = findViewById(R.id.time3_bin_image_view)
        time4BinImageView = findViewById(R.id.time4_bin_image_view)
        time5BinImageView = findViewById(R.id.time5_bin_image_view)
        time1AlarmSwitch = findViewById(R.id.time1_alarm_switch)
        time2AlarmSwitch = findViewById(R.id.time2_alarm_switch)
        time3AlarmSwitch = findViewById(R.id.time3_alarm_switch)
        time4AlarmSwitch = findViewById(R.id.time4_alarm_switch)
        time5AlarmSwitch = findViewById(R.id.time5_alarm_switch)
        time1NotificationSwitch = findViewById(R.id.time1_notification_switch)
        time2NotificationSwitch = findViewById(R.id.time2_notification_switch)
        time3NotificationSwitch = findViewById(R.id.time3_notification_switch)
        time4NotificationSwitch = findViewById(R.id.time4_notification_switch)
        time5NotificationSwitch = findViewById(R.id.time5_notification_switch)
        languageSwitch = findViewById(R.id.language_switch)
        setTimersButton = findViewById(R.id.setTimersButton)

        if(language == "EN"){
            languageTextView.text = "EN"
            val params = languageTextView.layoutParams as LinearLayout.LayoutParams
            params.marginStart = dpToPx(370)
            languageTextView.layoutParams = params
            setTimersButton.hint = "Set Alarm"
        }

        languageSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                languageTextView.text = "EN"
                val params = languageTextView.layoutParams as LinearLayout.LayoutParams
                params.marginStart = dpToPx(370)
                languageTextView.layoutParams = params
                setTimersButton.hint = "Set Alarm"
            } else {
                languageTextView.text = "TR"
                val params = languageTextView.layoutParams as LinearLayout.LayoutParams
                params.marginStart = dpToPx(350)
                languageTextView.layoutParams = params
                setTimersButton.hint = "Alarm Kur"
            }
        }

        locationTextView.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val choice:String
                    if(languageTextView.text.toString() == "TR"){
                        choice = getLocationChoiceTR(this@MainActivity)
                    }
                    else{
                        choice = getLocationChoiceEN(this@MainActivity)
                    }
                    if(choice == "new" || choice == "saved"){
                        callPage(choice)
                    }
                } catch (e: Exception) {
                    // Hata işle
                }
            }
        }
        time1AudioImageView.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                if(languageTextView.text.toString() == "TR"){
                    time1AlarmChoice = getAlarmChoiceTR(this@MainActivity)
                }
                else{
                    time1AlarmChoice = getAlarmChoiceEN(this@MainActivity)
                }
                if(time1AlarmChoice == "none"){
                    time1AlarmChoice = "Azan"
                }
            }
        }
        time2AudioImageView.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                if(languageTextView.text.toString() == "TR"){
                    time2AlarmChoice = getAlarmChoiceTR(this@MainActivity)
                }
                else{
                    time2AlarmChoice = getAlarmChoiceEN(this@MainActivity)
                }
                if(time2AlarmChoice == "none"){
                    time2AlarmChoice = "Azan"
                }
            }
        }
        time3AudioImageView.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                if(languageTextView.text.toString() == "TR"){
                    time3AlarmChoice = getAlarmChoiceTR(this@MainActivity)
                }
                else{
                    time3AlarmChoice = getAlarmChoiceEN(this@MainActivity)
                }
                if(time3AlarmChoice == "none"){
                    time3AlarmChoice = "Azan"
                }
            }
        }
        time4AudioImageView.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                if(languageTextView.text.toString() == "TR"){
                    time4AlarmChoice = getAlarmChoiceTR(this@MainActivity)
                }
                else{
                    time4AlarmChoice = getAlarmChoiceEN(this@MainActivity)
                }
                if(time4AlarmChoice == "none"){
                    time4AlarmChoice = "Azan"
                }
            }
        }
        time5AudioImageView.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                if(languageTextView.text.toString() == "TR"){
                    time5AlarmChoice = getAlarmChoiceTR(this@MainActivity)
                }
                else{
                    time5AlarmChoice = getAlarmChoiceEN(this@MainActivity)
                }
                if(time5AlarmChoice == "none"){
                    time5AlarmChoice = "Azan"
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU is API level 33 (Android 13)
            val notificationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            if (notificationPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 6)
            }
        }
        setTimersButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val validityWithTimeAndAudio = checkAlarmsValidity()
                for (i in 0..12 step 3) {
                    if (validityWithTimeAndAudio[i] == "1"){
                        setTimer(validityWithTimeAndAudio[i+2], (i/3) + 1, "alarm", validityWithTimeAndAudio[i+1])
                    }
                    else if(validityWithTimeAndAudio[i] == "2"){
                        setTimer(validityWithTimeAndAudio[i+2], (i/3) + 1, "notification", validityWithTimeAndAudio[i+1])
                    }
                }
            }
        }
        locationTextView.text = checkSavedLocations()

        setVisibility("INVISIBLE")

        if(locationTextView.text.toString() != "Kayıtlı Konum Yok" && locationTextView.text.toString() != "No Saved Location"){
            CoroutineScope(Dispatchers.Main).launch {
                val answer = setTimeFields()
                if(answer == "done" || answer == "oldData"){
                    setVisibility("VISIBLE")
                }
                //Log.d("Main Activity", "returned")
            }
        }

        time1AlarmSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                time1NotificationSwitch.isChecked = false
                time1NotificationSwitch.isEnabled = false
            } else {
                time1NotificationSwitch.isEnabled = true
            }
        }
        time2AlarmSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                time2NotificationSwitch.isChecked = false
                time2NotificationSwitch.isEnabled = false
            } else {
                time2NotificationSwitch.isEnabled = true
            }
        }
        time3AlarmSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                time3NotificationSwitch.isChecked = false
                time3NotificationSwitch.isEnabled = false
            } else {
                time3NotificationSwitch.isEnabled = true
            }
        }
        time4AlarmSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                time4NotificationSwitch.isChecked = false
                time4NotificationSwitch.isEnabled = false
            } else {
                time4NotificationSwitch.isEnabled = true
            }
        }
        time5AlarmSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                time5NotificationSwitch.isChecked = false
                time5NotificationSwitch.isEnabled = false
            } else {
                time5NotificationSwitch.isEnabled = true
            }
        }
        time1NotificationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                time1AlarmSwitch.isChecked = false
                time1AlarmSwitch.isEnabled = false
            } else {
                time1AlarmSwitch.isEnabled = true
            }
        }
        time2NotificationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                time2AlarmSwitch.isChecked = false
                time2AlarmSwitch.isEnabled = false
            } else {
                time2AlarmSwitch.isEnabled = true
            }
        }
        time3NotificationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                time3AlarmSwitch.isChecked = false
                time3AlarmSwitch.isEnabled = false
            } else {
                time3AlarmSwitch.isEnabled = true
            }
        }
        time4NotificationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                time4AlarmSwitch.isChecked = false
                time4AlarmSwitch.isEnabled = false
            } else {
                time4AlarmSwitch.isEnabled = true
            }
        }
        time5NotificationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                time5AlarmSwitch.isChecked = false
                time5AlarmSwitch.isEnabled = false
            } else {
                time5AlarmSwitch.isEnabled = true
            }
        }
        time1BinImageView.setOnClickListener {
            if(cancelAlarm(1)){
                if(languageTextView.text.toString() == "TR"){
                    doToast("Silindi")
                }
                else{
                    doToast("Deleted")
                }
            }
        }
        time2BinImageView.setOnClickListener {
            if(cancelAlarm(2)){
                if(languageTextView.text.toString() == "TR"){
                    doToast("Silindi")
                }
                else{
                    doToast("Deleted")
                }
            }
        }
        time3BinImageView.setOnClickListener {
            if(cancelAlarm(3)){
                if(languageTextView.text.toString() == "TR"){
                    doToast("Silindi")
                }
                else{
                    doToast("Deleted")
                }
            }
        }
        time4BinImageView.setOnClickListener {
            if(cancelAlarm(4)){
                if(languageTextView.text.toString() == "TR"){
                    doToast("Silindi")
                }
                else{
                    doToast("Deleted")
                }
            }
        }
        time5BinImageView.setOnClickListener {
            if(cancelAlarm(5)){
                if(languageTextView.text.toString() == "TR"){
                    doToast("Silindi")
                }
                else{
                    doToast("Deleted")
                }
            }
        }
    }
    private fun cancelAlarm(requestCode: Int): Boolean {
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val sharedPref = this.getSharedPreferences("ALARMS", Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                remove("time_$requestCode")
                //Log.d("Alarm Receiver", "removed $requestCode")
                apply()
            }
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            return true
        }
        return false
    }

    private fun isThereAnAlarmSet(reqCode: Int): Boolean{
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            reqCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }
    @SuppressLint("ScheduleExactAlarm", "SimpleDateFormat")
    private fun setTimer(time: String, requestCode: Int, type: String, audio: String){
        if(!isThereAnAlarmSet(requestCode)){
            val (hour, minute) = time.split(":").map { it.toInt() }
            val calendar: Calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            val triggerTime: Long = calendar.timeInMillis

            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val broadcastIntent = Intent(this, AlarmReceiver::class.java).apply {
                putExtra("REQUEST_CODE", requestCode)
                putExtra("Language", languageTextView.text.toString())
                putExtra("Type", type)
                putExtra("Audio", audio)
            }
            val pendingIntent = PendingIntent.getBroadcast(this@MainActivity, requestCode, broadcastIntent, PendingIntent.FLAG_IMMUTABLE)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)

            val alarmDate = Date(triggerTime)
            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
            val formattedDate = dateFormat.format(alarmDate)
            if (languageTextView.text.toString() == "TR"){
                if(type == "alarm"){
                    if(audio == "Azan"){
                        doToast("$formattedDate'de ezan okunacak")
                    }
                    else{
                        doToast("$formattedDate'de alarm çalacak")
                    }
                }
                else{
                    doToast("$formattedDate'da bildirim gönderilecek")
                }
            }
            else{
                if(type == "alarm"){
                    doToast("Azan set for $formattedDate")
                }
                else{
                    doToast("Notification will be send at $formattedDate")
                }
            }
            saveAlarm(time, requestCode)
        }
        else if (languageTextView.text.toString() == "TR"){
            doToast("$time için kurulu ezan/bildirim bulunmakta")
        }
        else{
            doToast("There is an existing azan/notification for $time")
        }
    }
    private fun saveAlarm(time: String, requestCode: Int) {
        val sharedPref = getSharedPreferences("ALARMS", Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putString("time_$requestCode", time)
            //Log.d("Alarm Receiver", "add $requestCode")
            apply()
        }
    }
    private suspend fun checkAlarmsValidity(): Array<String> {
        return withContext(Dispatchers.IO) {
            val validityWithTime = Array(15) { "0" }
            val currentDateTime = LocalDateTime.now()
            val currentDate = currentDateTime.toString().substring(0, 10)
            val currentTime = currentDateTime.toString().substring(11, 19)
            val currentTimeParsed = LocalTime.parse(currentTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
            val temp = dateTextView.text.toString().split("-")
            val date = temp[2] + "-" + temp[1] + "-" + temp[0]
            val currentDateParsed = LocalDate.parse(currentDate, DateTimeFormatter.ISO_LOCAL_DATE)
            val dateParsed = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
            if (dateParsed.isAfter(currentDateParsed)) {
                if (time1AlarmSwitch.isChecked) {
                    validityWithTime[0] = "1"
                    validityWithTime[1] = time1AlarmChoice
                    validityWithTime[2] = time1TextView.text.toString()
                }
                else if (time1NotificationSwitch.isChecked){
                    validityWithTime[0] = "2"
                    validityWithTime[1] = time1AlarmChoice
                    validityWithTime[2] = time1TextView.text.toString()
                }
                if (time2AlarmSwitch.isChecked) {
                    validityWithTime[3] = "1"
                    validityWithTime[4] = time2AlarmChoice
                    validityWithTime[5] = time2TextView.text.toString()
                }
                else if (time2NotificationSwitch.isChecked){
                    validityWithTime[3] = "2"
                    validityWithTime[4] = time2AlarmChoice
                    validityWithTime[5] = time2TextView.text.toString()
                }
                if (time3AlarmSwitch.isChecked) {
                    validityWithTime[6] = "1"
                    validityWithTime[7] = time3AlarmChoice
                    validityWithTime[8] = time3TextView.text.toString()
                }
                else if (time3NotificationSwitch.isChecked){
                    validityWithTime[6] = "2"
                    validityWithTime[7] = time3AlarmChoice
                    validityWithTime[8] = time3TextView.text.toString()
                }
                if (time4AlarmSwitch.isChecked) {
                    validityWithTime[9] = "1"
                    validityWithTime[10] = time4AlarmChoice
                    validityWithTime[11] = time4TextView.text.toString()
                }
                else if (time4NotificationSwitch.isChecked){
                    validityWithTime[9] = "2"
                    validityWithTime[10] = time4AlarmChoice
                    validityWithTime[11] = time4TextView.text.toString()
                }
                if (time5AlarmSwitch.isChecked) {
                    validityWithTime[12] = "1"
                    validityWithTime[13] = time5AlarmChoice
                    validityWithTime[14] = time5TextView.text.toString()
                }
                else if (time5NotificationSwitch.isChecked){
                    validityWithTime[12] = "2"
                    validityWithTime[13] = time5AlarmChoice
                    validityWithTime[14] = time5TextView.text.toString()
                }
            } else {
                val prayerTime1Parsed =
                    LocalTime.parse(time1TextView.text.toString(), DateTimeFormatter.ofPattern("HH:mm"))
                val prayerTime2Parsed =
                    LocalTime.parse(time2TextView.text.toString(), DateTimeFormatter.ofPattern("HH:mm"))
                val prayerTime3Parsed =
                    LocalTime.parse(time3TextView.text.toString(), DateTimeFormatter.ofPattern("HH:mm"))
                val prayerTime4Parsed =
                    LocalTime.parse(time4TextView.text.toString(), DateTimeFormatter.ofPattern("HH:mm"))
                val prayerTime5Parsed =
                    LocalTime.parse(time5TextView.text.toString(), DateTimeFormatter.ofPattern("HH:mm"))
                if (prayerTime1Parsed.isAfter(currentTimeParsed)) {
                    if (time1AlarmSwitch.isChecked) {
                        validityWithTime[0] = "1"
                        validityWithTime[1] = time1AlarmChoice
                        validityWithTime[2] = time1TextView.text.toString()
                    }
                    else if (time1NotificationSwitch.isChecked){
                        validityWithTime[0] = "2"
                        validityWithTime[1] = time1AlarmChoice
                        validityWithTime[2] = time1TextView.text.toString()
                    }
                    if (time2AlarmSwitch.isChecked) {
                        validityWithTime[3] = "1"
                        validityWithTime[4] = time2AlarmChoice
                        validityWithTime[5] = time2TextView.text.toString()
                    }
                    else if (time2NotificationSwitch.isChecked){
                        validityWithTime[3] = "2"
                        validityWithTime[4] = time2AlarmChoice
                        validityWithTime[5] = time2TextView.text.toString()
                    }
                    if (time3AlarmSwitch.isChecked) {
                        validityWithTime[6] = "1"
                        validityWithTime[7] = time3AlarmChoice
                        validityWithTime[8] = time3TextView.text.toString()
                    }
                    else if (time3NotificationSwitch.isChecked){
                        validityWithTime[6] = "2"
                        validityWithTime[7] = time3AlarmChoice
                        validityWithTime[8] = time3TextView.text.toString()
                    }
                    if (time4AlarmSwitch.isChecked) {
                        validityWithTime[9] = "1"
                        validityWithTime[10] = time4AlarmChoice
                        validityWithTime[11] = time4TextView.text.toString()
                    }
                    else if (time4NotificationSwitch.isChecked){
                        validityWithTime[9] = "2"
                        validityWithTime[10] = time4AlarmChoice
                        validityWithTime[11] = time4TextView.text.toString()
                    }
                    if (time5AlarmSwitch.isChecked) {
                        validityWithTime[12] = "1"
                        validityWithTime[13] = time5AlarmChoice
                        validityWithTime[14] = time5TextView.text.toString()
                    }
                    else if (time5NotificationSwitch.isChecked){
                        validityWithTime[12] = "2"
                        validityWithTime[13] = time5AlarmChoice
                        validityWithTime[14] = time5TextView.text.toString()
                    }
                } else if (prayerTime2Parsed.isAfter(currentTimeParsed)) {
                    if (time2AlarmSwitch.isChecked) {
                        validityWithTime[3] = "1"
                        validityWithTime[4] = time2AlarmChoice
                        validityWithTime[5] = time2TextView.text.toString()
                    }
                    else if (time2NotificationSwitch.isChecked){
                        validityWithTime[3] = "2"
                        validityWithTime[4] = time2AlarmChoice
                        validityWithTime[5] = time2TextView.text.toString()
                    }
                    if (time3AlarmSwitch.isChecked) {
                        validityWithTime[6] = "1"
                        validityWithTime[7] = time3AlarmChoice
                        validityWithTime[8] = time3TextView.text.toString()
                    }
                    else if (time3NotificationSwitch.isChecked){
                        validityWithTime[6] = "2"
                        validityWithTime[7] = time3AlarmChoice
                        validityWithTime[8] = time3TextView.text.toString()
                    }
                    if (time4AlarmSwitch.isChecked) {
                        validityWithTime[9] = "1"
                        validityWithTime[10] = time4AlarmChoice
                        validityWithTime[11] = time4TextView.text.toString()
                    }
                    else if (time4NotificationSwitch.isChecked){
                        validityWithTime[9] = "2"
                        validityWithTime[10] = time4AlarmChoice
                        validityWithTime[11] = time4TextView.text.toString()
                    }
                    if (time5AlarmSwitch.isChecked) {
                        validityWithTime[12] = "1"
                        validityWithTime[13] = time5AlarmChoice
                        validityWithTime[14] = time5TextView.text.toString()
                    }
                    else if (time5NotificationSwitch.isChecked){
                        validityWithTime[12] = "2"
                        validityWithTime[13] = time5AlarmChoice
                        validityWithTime[14] = time5TextView.text.toString()
                    }
                } else if (prayerTime3Parsed.isAfter(currentTimeParsed)) {
                    if (time3AlarmSwitch.isChecked) {
                        validityWithTime[6] = "1"
                        validityWithTime[7] = time3AlarmChoice
                        validityWithTime[8] = time3TextView.text.toString()
                    }
                    else if (time3NotificationSwitch.isChecked){
                        validityWithTime[6] = "2"
                        validityWithTime[7] = time3AlarmChoice
                        validityWithTime[8] = time3TextView.text.toString()
                    }
                    if (time4AlarmSwitch.isChecked) {
                        validityWithTime[9] = "1"
                        validityWithTime[10] = time4AlarmChoice
                        validityWithTime[11] = time4TextView.text.toString()
                    }
                    else if (time4NotificationSwitch.isChecked){
                        validityWithTime[9] = "2"
                        validityWithTime[10] = time4AlarmChoice
                        validityWithTime[11] = time4TextView.text.toString()
                    }
                    if (time5AlarmSwitch.isChecked) {
                        validityWithTime[12] = "1"
                        validityWithTime[13] = time5AlarmChoice
                        validityWithTime[14] = time5TextView.text.toString()
                    }
                    else if (time5NotificationSwitch.isChecked){
                        validityWithTime[12] = "2"
                        validityWithTime[13] = time5AlarmChoice
                        validityWithTime[14] = time5TextView.text.toString()
                    }
                } else if (prayerTime4Parsed.isAfter(currentTimeParsed)) {
                    if (time4AlarmSwitch.isChecked) {
                        validityWithTime[9] = "1"
                        validityWithTime[10] = time4AlarmChoice
                        validityWithTime[11] = time4TextView.text.toString()
                    }
                    else if (time4NotificationSwitch.isChecked){
                        validityWithTime[9] = "2"
                        validityWithTime[10] = time4AlarmChoice
                        validityWithTime[11] = time4TextView.text.toString()
                    }
                    if (time5AlarmSwitch.isChecked) {
                        validityWithTime[12] = "1"
                        validityWithTime[13] = time5AlarmChoice
                        validityWithTime[14] = time5TextView.text.toString()
                    }
                    else if (time5NotificationSwitch.isChecked){
                        validityWithTime[12] = "2"
                        validityWithTime[13] = time5AlarmChoice
                        validityWithTime[14] = time5TextView.text.toString()
                    }
                } else if (prayerTime5Parsed.isAfter(currentTimeParsed)) {
                    if (time5AlarmSwitch.isChecked) {
                        validityWithTime[12] = "1"
                        validityWithTime[13] = time5AlarmChoice
                        validityWithTime[14] = time5TextView.text.toString()
                    }
                    else if (time5NotificationSwitch.isChecked){
                        validityWithTime[12] = "2"
                        validityWithTime[13] = time5AlarmChoice
                        validityWithTime[14] = time5TextView.text.toString()
                    }
                }
            }
            return@withContext validityWithTime
        }
    }
    private suspend fun setTimeFields(): String {
        val fileName = locationTextView.text.toString() + ".csv"
        val file = File(this.filesDir, fileName)
        if (file.exists()) {
            var inputStream = withContext(Dispatchers.IO) {
                FileInputStream(file)
            }
            var bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var firstLine = withContext(Dispatchers.IO) {
                bufferedReader.readLine()
            }
            //Log.d("Main Activity", firstLine)
            var parts = firstLine.split(",")
            if (parts.size == 6) {
                var date = parts[0]
                var time1 = parts[1]
                var time2 = parts[2]
                var time3 = parts[3]
                var time4 = parts[4]
                var time5 = parts[5]

                val currentDateTime = LocalDateTime.now()
                val currentDate = currentDateTime.toString().substring(0,10)
                val currentTime = currentDateTime.toString().substring(11,19)
                val currentDateParsed = LocalDate.parse(currentDate, DateTimeFormatter.ISO_LOCAL_DATE)
                val dateParsed = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)

                val currentTimeParsed = LocalTime.parse(currentTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
                var lastPrayerTime = LocalTime.parse(time5, DateTimeFormatter.ofPattern("HH:mm"))

                if(currentDateParsed.isAfter(dateParsed)){
                    return suspendCoroutine { continuation ->
                        CoroutineScope(Dispatchers.Main).launch {
                            var answer: String
                            if (isNetworkConnected()) {
                                if (languageTextView.text.toString() == "TR"){
                                    doToast("Veriler İşleniyor...")
                                }
                                else{
                                    doToast("Processing...")
                                }
                                bufferedReader.close()
                                inputStream.close()
                                answer = updateFile(fileName)
                                if (answer != "done") {
                                    if(answer == "error"){
                                        inputStream = FileInputStream(file)
                                        bufferedReader =  BufferedReader(InputStreamReader(inputStream))
                                        var line: String
                                        var temp = ""
                                        line = bufferedReader.readLine()
                                        parts = line.split(",")
                                        var counter = 0
                                        while(counter < 6 && currentDate != parts[0]){

                                            temp = line
                                            line = bufferedReader.readLine()
                                            parts = line.split(",")
                                            counter++
                                        }
                                        if(currentDate == parts[0]){
                                            val dateParts = parts[0].split("-")
                                            val tmp =
                                                dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0]
                                            dateTextView.text = tmp
                                            time1TextView.text = parts[1]
                                            time2TextView.text = parts[2]
                                            time3TextView.text = parts[3]
                                            time4TextView.text = parts[4]
                                            time5TextView.text = parts[5]
                                            answer = "done"
                                        }
                                        else{
                                            parts = temp.split(",")
                                            val dateParts = parts[0].split("-")
                                            val tmp =
                                                dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0]
                                            dateTextView.text = tmp
                                            time1TextView.text = parts[1]
                                            time2TextView.text = parts[2]
                                            time3TextView.text = parts[3]
                                            time4TextView.text = parts[4]
                                            time5TextView.text = parts[5]
                                            answer = "oldData"
                                        }


                                        if(languageTextView.text.toString() == "TR"){
                                            doToast("Veriler güncellenemedi. Eski veri görüyor olabilirsiniz.")
                                        }
                                        else{
                                            doToast("Data can not be updated. You might be seeing ol data.")
                                        }
                                    }
                                    else{
                                        if (languageTextView.text.toString() == "TR") {
                                            doToast("Veri güncellemesi gerekli, lütfen internete bağlanıp uygulamaya tekrar girin")
                                        } else {
                                            doToast("Data update required, please connect to the internet and re-enter the app")
                                        }
                                    }

                                }
                                else {
                                    inputStream = FileInputStream(file)
                                    bufferedReader =
                                        BufferedReader(InputStreamReader(inputStream))
                                    firstLine = bufferedReader.readLine()
                                    parts = firstLine.split(",")
                                    date = parts[0]
                                    time1 = parts[1]
                                    time2 = parts[2]
                                    time3 = parts[3]
                                    time4 = parts[4]
                                    time5 = parts[5]
                                    lastPrayerTime = LocalTime.parse(time5, DateTimeFormatter.ofPattern("HH:mm"))
                                    if (currentTimeParsed.isAfter(lastPrayerTime)) {
                                        firstLine = bufferedReader.readLine()
                                        parts = firstLine.split(",")
                                        val dateParts = parts[0].split("-")
                                        val tmp =
                                            dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0]
                                        dateTextView.text = tmp
                                        time1TextView.text = parts[1]
                                        time2TextView.text = parts[2]
                                        time3TextView.text = parts[3]
                                        time4TextView.text = parts[4]
                                        time5TextView.text = parts[5]
                                    }
                                    else {
                                        val dateParts = date.split("-")
                                        val tmp =
                                            dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0]
                                        dateTextView.text = tmp
                                        time1TextView.text = time1
                                        time2TextView.text = time2
                                        time3TextView.text = time3
                                        time4TextView.text = time4
                                        time5TextView.text = time5
                                    }
                                }
                            }
                            else {
                                if (languageTextView.text.toString() == "TR") {
                                    doToast("Veri güncellemesi gerekli, lütfen internete bağlanıp uygulamaya tekrar girin")
                                } else {
                                    doToast("Data update required, please connect to the internet and re-enter the app")
                                }

                                answer = "error"
                            }
                            bufferedReader.close()
                            inputStream.close()
                            continuation.resume(answer)
                        }
                    }
                }
                else{
                    if(currentTimeParsed.isAfter(lastPrayerTime)){
                        firstLine = withContext(Dispatchers.IO) {
                            bufferedReader.readLine()
                        }
                        parts = firstLine.split(",")
                        val dateParts = parts[0].split("-")
                        val tmp = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0]
                        dateTextView.text = tmp
                        time1TextView.text = parts[1]
                        time2TextView.text = parts[2]
                        time3TextView.text = parts[3]
                        time4TextView.text = parts[4]
                        time5TextView.text = parts[5]
                    }
                    else{
                        val dateParts = date.split("-")
                        val tmp = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0]
                        dateTextView.text = tmp
                        time1TextView.text = time1
                        time2TextView.text = time2
                        time3TextView.text = time3
                        time4TextView.text = time4
                        time5TextView.text = time5
                    }
                    bufferedReader.close()
                    inputStream.close()
                    return "done"
                }
            }
            else{
                bufferedReader.close()
                inputStream.close()
                return "error"
            }
        }
        else{
            return "error"
        }
    }

    private suspend fun updateFile(fileName: String): String{
        val location = fileName.substring(0, fileName.indexOf("."))
        val file = File(this.filesDir, "myLocations.csv")
        return withContext(Dispatchers.IO) {
            if (file.exists()) {
                val bufferedReader = file.bufferedReader()
                val firstLine = bufferedReader.readLine()
                bufferedReader.close()
                val parts = firstLine.split(",")
                val url = parts[1]
                val prayerTimes: String
                //fetch prayer times
                try {
                    val document: Document = Jsoup.connect(url).get()
                    val body = document.body()
                    val parsedDoc = Jsoup.parse(body.toString())
                    val prayerTimesElement: Element? = parsedDoc.selectFirst("#tab-0 table.vakit-table tbody")
                    //Log.d("Main Activity", "Prayer Times Element found: $prayerTimesElement")
                    val rows = prayerTimesElement?.select("tr")
                    val prayerTimesStringBuilder = StringBuilder()
                    rows?.forEach { row ->
                        val columns = row.select("td")
                        columns.forEachIndexed { index, column ->
                            if (index == 0) {
                                prayerTimesStringBuilder.append("${column.text()}: ")
                            } else if (index == columns.size - 1) {
                                prayerTimesStringBuilder.append(column.text())
                            } else {
                                prayerTimesStringBuilder.append("${column.text()}, ")
                            }
                        }
                        prayerTimesStringBuilder.append("\n")
                    }
                    prayerTimes = prayerTimesStringBuilder.toString()
                } catch (e: Exception) {
                    //Log.e("FetchPrayerTimes", "Error fetching prayer times", e)
                    return@withContext "fetching"
                }
                if(prayerTimes.isEmpty()){
                    return@withContext "error"
                }
                else{
                    createCSVFile(prayerTimes, "$location.csv")
                    return@withContext "done"
                }

            }
            return@withContext "no file"
        }
    }
    private fun createCSVFile(prayerTimes: String, fileName: String) {
        val csvFile = File(this.filesDir, fileName)
        if (csvFile.exists()) {
            csvFile.delete()
        }
        csvFile.createNewFile()

        val lines = prayerTimes.split("\n")
        for (line in lines) {
            if (line.isNotBlank()) {
                val parts = line.split(": ")
                if (parts.size > 1) {
                    val tmp = parts[0].trim().split(" ")
                    val date = formatDate(tmp[0] + " " + tmp[1] + " " + tmp[2])
                    val temp = parts[1].split(", ")
                    val times = temp[1] + "," + temp[3] + "," + temp[4] + "," + temp[5] + "," + temp[6]
                    val csvLine = "$date,$times\n"
                    csvFile.appendText(csvLine)
                }
            }
        }
    }
    private fun formatDate(date: String): String{
        val parts = date.split(" ")
        val month: String = when(parts[1]) {
            "Ocak" -> "01"
            "Şubat" -> "02"
            "Mart" -> "03"
            "Nisan" -> "04"
            "Mayıs" -> "05"
            "Haziran" -> "06"
            "Temmuz" -> "07"
            "Ağustos" -> "08"
            "Eylül" -> "09"
            "Ekim" -> "10"
            "Kasım" -> "11"
            "Aralık" -> "12"
            else -> ""
        }
        return parts[2] + "-" + month + "-" + parts[0]
    }
    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
    private fun callPage(choice: String){
        if(choice == "new"){
            if(isNetworkConnected()){
                val intent = Intent(this, AddNewLocation::class.java)
                intent.putExtra("language",languageTextView.text.toString())
                startActivity(intent)
                finish()
            }
            else if(languageTextView.text.toString() == "TR"){
                doToast("Bu sayfa için bir ağa bağlı olmanız gerekir")
            }
            else{
                doToast("For this page, you need to connected to a wifi/cellular")
            }
        }
        else{
            val intent = Intent(this, SavedLocations::class.java)
            intent.putExtra("language", languageTextView.text.toString())
            startActivity(intent)
            finish()
        }
    }
    private fun checkSavedLocations(): String {
        val file = File(this.filesDir, "myLocations.csv")

        return if (file.exists()) {
            try {
                val bufferedReader = file.bufferedReader()
                val firstLine = bufferedReader.readLine()
                bufferedReader.close()

                if (firstLine != null) {
                    val parts = firstLine.split(",")
                    if (parts.size >= 2) {
                        val name = parts[0]
                        return name
                    }
                }
                if(language == "TR"){
                    return "Kayıtlı Konum Yok"
                }
                else{
                    return "No Saved Location"
                }
            } catch (e: FileNotFoundException) {
                return "Kayıtlı Konum Yok"
            } catch (e: Exception) {
                "Hata oluştu: ${e.message}"
            }
        } else {
            if(language == "TR"){
                return "Kayıtlı Konum Yok"
            }
            else{
                return "No Saved Location"
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            6 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //
                }
            }
        }
    }
    private fun setVisibility(situation: String){
        if(situation == "VISIBLE"){
            alarmClockImageView.visibility = View.VISIBLE
            notificationImageView.visibility = View.VISIBLE
            time1AudioImageView.visibility = View.VISIBLE
            time2AudioImageView.visibility = View.VISIBLE
            time3AudioImageView.visibility = View.VISIBLE
            time4AudioImageView.visibility = View.VISIBLE
            time5AudioImageView.visibility = View.VISIBLE
            time1BinImageView.visibility = View.VISIBLE
            time2BinImageView.visibility = View.VISIBLE
            time3BinImageView.visibility = View.VISIBLE
            time4BinImageView.visibility = View.VISIBLE
            time5BinImageView.visibility = View.VISIBLE
            time1AlarmSwitch.visibility = View.VISIBLE
            time2AlarmSwitch.visibility = View.VISIBLE
            time3AlarmSwitch.visibility = View.VISIBLE
            time4AlarmSwitch.visibility = View.VISIBLE
            time5AlarmSwitch.visibility = View.VISIBLE
            time1NotificationSwitch.visibility = View.VISIBLE
            time2NotificationSwitch.visibility = View.VISIBLE
            time3NotificationSwitch.visibility = View.VISIBLE
            time4NotificationSwitch.visibility = View.VISIBLE
            time5NotificationSwitch.visibility = View.VISIBLE
            dateTextView.visibility = View.VISIBLE
            time1TextView.visibility = View.VISIBLE
            time2TextView.visibility = View.VISIBLE
            time3TextView.visibility = View.VISIBLE
            time4TextView.visibility = View.VISIBLE
            time5TextView.visibility = View.VISIBLE
            setTimersButton.visibility = View.VISIBLE
        }
        else{
            alarmClockImageView.visibility = View.INVISIBLE
            notificationImageView.visibility = View.INVISIBLE
            time1AudioImageView.visibility = View.INVISIBLE
            time2AudioImageView.visibility = View.INVISIBLE
            time3AudioImageView.visibility = View.INVISIBLE
            time4AudioImageView.visibility = View.INVISIBLE
            time5AudioImageView.visibility = View.INVISIBLE
            time1BinImageView.visibility = View.INVISIBLE
            time2BinImageView.visibility = View.INVISIBLE
            time3BinImageView.visibility = View.INVISIBLE
            time4BinImageView.visibility = View.INVISIBLE
            time5BinImageView.visibility = View.INVISIBLE
            time1AlarmSwitch.visibility = View.INVISIBLE
            time2AlarmSwitch.visibility = View.INVISIBLE
            time3AlarmSwitch.visibility = View.INVISIBLE
            time4AlarmSwitch.visibility = View.INVISIBLE
            time5AlarmSwitch.visibility = View.INVISIBLE
            time1NotificationSwitch.visibility = View.INVISIBLE
            time2NotificationSwitch.visibility = View.INVISIBLE
            time3NotificationSwitch.visibility = View.INVISIBLE
            time4NotificationSwitch.visibility = View.INVISIBLE
            time5NotificationSwitch.visibility = View.INVISIBLE
            dateTextView.visibility = View.INVISIBLE
            time1TextView.visibility = View.INVISIBLE
            time2TextView.visibility = View.INVISIBLE
            time3TextView.visibility = View.INVISIBLE
            time4TextView.visibility = View.INVISIBLE
            time5TextView.visibility = View.INVISIBLE
            setTimersButton.visibility = View.INVISIBLE
        }
    }
    private suspend fun getLocationChoiceEN(context: Context): String {
        return suspendCancellableCoroutine { continuation ->
            val dialog = AlertDialog.Builder(context)
                .setMessage("Choose an option:")
                .setPositiveButton("Add New Location") { _, _ ->
                    continuation.resume("new")
                }
                .setNegativeButton("Select From Saved Locations") { _, _ ->
                    continuation.resume("saved")
                }
                .setOnCancelListener {
                    continuation.resumeWithException(Exception("Dialog was cancelled"))
                }
                .create()

            continuation.invokeOnCancellation {
                dialog.dismiss()
            }

            dialog.show()
        }
    }
    private suspend fun getLocationChoiceTR(context: Context): String {
        return suspendCancellableCoroutine { continuation ->
            val dialog = AlertDialog.Builder(context)
                .setTitle("")
                .setMessage("Seçiniz:")
                .setPositiveButton("Yeni Konum Ekle") { _, _ ->
                    continuation.resume("new")
                }
                .setNegativeButton("Kayıtlı Konumlardan Seç") { _, _ ->
                    continuation.resume("saved")
                }
                .setOnCancelListener {
                    continuation.resumeWithException(Exception("Dialog was cancelled"))
                }
                .create()

            continuation.invokeOnCancellation {
                dialog.dismiss()
            }

            dialog.show()
        }
    }
    private suspend fun getAlarmChoiceEN(context: Context): String {
        return suspendCancellableCoroutine { continuation ->
            val dialog = AlertDialog.Builder(context)
                .setMessage("Select an alarm noise:")
                .setPositiveButton("Azan") { _, _ ->
                    continuation.resume("Azan")
                }
                .setNegativeButton("Morning Glory") { _, _ ->
                    continuation.resume("Morning Glory")
                }
                .setOnCancelListener {
                    continuation.resume("none")
                }
                .create()

            continuation.invokeOnCancellation {
                dialog.dismiss()
            }

            dialog.show()
        }
    }
    private suspend fun getAlarmChoiceTR(context: Context): String {
        return suspendCancellableCoroutine { continuation ->
            val dialog = AlertDialog.Builder(context)
                .setTitle("")
                .setMessage("Alarm Sesini Seçiniz:")
                .setPositiveButton("Ezan") { _, _ ->
                    continuation.resume("Azan")
                }
                .setNegativeButton("Morning Glory") { _, _ ->
                    continuation.resume("Morning Glory")
                }
                .setOnCancelListener {
                    continuation.resume("none")
                }
                .create()

            continuation.invokeOnCancellation {
                dialog.dismiss()
            }

            dialog.show()
        }
    }
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
    private fun doToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}