package com.example.prayertimeremainder

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SavedLocations : AppCompatActivity()  {
    private lateinit var linearLayout: LinearLayout
    private lateinit var locationsListView: ListView
    private lateinit var language: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.saved_locations)

        language = intent.getStringExtra("language") ?: "TR"
        linearLayout = findViewById(R.id.linearLayout)
        locationsListView = findViewById(R.id.locationsListView)

        setListView()
    }
    private fun setListView() {
        val file = File(filesDir, "myLocations.csv")
        val locationNames = mutableListOf<String>()

        if (file.exists()) {
            try {
                val bufferedReader = file.bufferedReader()
                bufferedReader.forEachLine { line ->
                    val parts = line.split(",")
                    if (parts.size >= 2) {
                        val name = parts[0]
                        locationNames.add(name)
                    }
                }
                bufferedReader.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            locationNames.add("Kayıtlı yer bulunamadı")
        }

        val adapter = CustomArrayAdapter(this, android.R.layout.simple_list_item_1, locationNames)
        locationsListView.adapter = adapter

        locationsListView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = locationNames[position]
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val choice:String
                    if(language == "TR"){
                        choice = getChoiceTR(this@SavedLocations)
                    }
                    else{
                        choice = getChoiceEN(this@SavedLocations)
                    }
                    if(choice == "delete"){
                        deleteAlarms()
                        deleteLocation(selectedItem)
                    }
                    else if(choice == "set"){
                        deleteAlarms()
                        setCurrentLocation(selectedItem)
                    }
                    else if(choice == "change"){
                        changeLocationName(selectedItem)
                    }
                } catch (e: Exception) {
                    // Hata işle
                }
            }
        }
    }
    private fun deleteAlarms(){
        for(i in 1..5 step 1){
            cancelAlarm(i)
        }
    }
    private fun cancelAlarm(requestCode: Int){
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
                Log.d("Alarm Receiver", "removed $requestCode")
                apply()
            }
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
    private fun deleteLocation(name: String) {
        var file = File(this.filesDir, "myLocations.csv")
        val tempList = mutableListOf<String>()

        try {
            val bufferedReader = file.bufferedReader()
            bufferedReader.forEachLine { line ->
                val parts = line.split(",")
                if (parts.isNotEmpty() && parts[0].trim() != name) {
                    tempList.add(line)
                }
            }
            bufferedReader.close()

            val bufferedWriter = file.bufferedWriter()
            tempList.forEach { line ->
                bufferedWriter.write(line)
                bufferedWriter.newLine()
            }
            bufferedWriter.close()
            setListView()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        file = File(this.filesDir, "$name.csv")
        if(file.exists()){
            file.delete()
        }
    }
    private fun changeLocationName(name: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                var newName:String
                if(language == "TR"){
                    newName = getNewNameTR(this@SavedLocations)
                    while (!isNewNameValid(newName)) {
                        doToast("Lütfen dosya isimleri için geçersiz karakterler kullanmayın")
                        newName = getNewNameTR(this@SavedLocations)
                    }
                }
                else{
                    newName = getNewNameEN(this@SavedLocations)
                    while (!isNewNameValid(newName)) {
                        doToast("Please don't use invalid characters for file names")
                        newName = getNewNameEN(this@SavedLocations)
                    }
                }
                implementChangeToFiles(newName,name)
            } catch (e: Exception) {
                // Hata işle
            }
        }
    }
    private fun isNewNameValid(name: String): Boolean{
        if (name.contains('\\') || name.contains('/') || name.contains(':') || name.contains('*')
            || name.contains('?') || name.contains('"') || name.contains('<') || name.contains('>')
            || name.contains('|') || name.contains('.')) {
            return false
        }
        return true
    }

    private fun implementChangeToFiles(newName: String, oldName: String){
        Log.d("Saved Locations", "$oldName  -> $newName")
        var file = File(this.filesDir, "myLocations.csv")
        var tempList = mutableListOf<String>()
        try {
            val bufferedReader = file.bufferedReader()
            bufferedReader.forEachLine { line ->
                val parts = line.split(",")
                if (parts.isNotEmpty() && parts[0].trim() != oldName) {
                    tempList.add(line)
                }
                else if(parts[0].trim() == oldName){
                    tempList.add(newName + "," + parts[1])
                }
            }
            bufferedReader.close()

            val bufferedWriter = file.bufferedWriter()
            tempList.forEach { line ->
                bufferedWriter.write(line)
                bufferedWriter.newLine()
            }
            bufferedWriter.close()
            setListView()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        file = File(this.filesDir, "$oldName.csv")
        tempList = mutableListOf<String>()
        try {
            val bufferedReader = file.bufferedReader()
            bufferedReader.forEachLine { line ->
                val parts = line.split(",")
                if (parts.isNotEmpty()) {
                    tempList.add(line)
                }
            }
            bufferedReader.close()
            if (file.delete()){
                Log.d("Saved Locations", "Old File deleted")
            }
            if (file.delete()){
                Log.d("Saved Locations", "Old File not deleted")
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        file = File(this.filesDir, "$newName.csv")
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        val bufferedWriter = file.bufferedWriter()
        tempList.forEach { line ->
            bufferedWriter.write(line)
            bufferedWriter.newLine()
        }
        bufferedWriter.close()
    }
    private fun setCurrentLocation(name: String) {
        val file = File(this.filesDir, "myLocations.csv")
        val tempList = mutableListOf<String>()
        var selected: String = ""
        try {
            val bufferedReader = file.bufferedReader()
            bufferedReader.forEachLine { line ->
                val parts = line.split(",")
                if (parts.isNotEmpty() && parts[0].trim() != name) {
                    tempList.add(line)
                }
                else if(parts[0].trim() == name){
                    selected = line
                }
            }
            bufferedReader.close()

            val bufferedWriter = file.bufferedWriter()
            tempList.forEach { line ->
                bufferedWriter.write(line)
                bufferedWriter.newLine()
            }
            bufferedWriter.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (file.exists()) {
            val currentContent = file.readText()
            val newContent = "$selected\n" + currentContent
            file.writeText(newContent)
            setListView()
        }
    }
    private fun doToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private suspend fun getNewNameTR(context: Context): String {
        return suspendCancellableCoroutine { continuation ->
            val editText = EditText(context)
            val dialog = AlertDialog.Builder(context)
                .setTitle("Yeni adını giriniz")
                .setView(editText)
                .setPositiveButton("OK") { _, _ ->
                    val locationName = editText.text.toString()
                    if (locationName.isNotEmpty()) {
                        continuation.resume(locationName)
                    } else {
                        doToast("Konum adı alanı boş bırakılamaz")
                        continuation.resumeWithException(Exception("Konum adı boş olamaz"))
                    }
                }
                .setNegativeButton("Cancel") { _, _ ->
                    continuation.resumeWithException(Exception("User cancelled the dialog"))
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
    private suspend fun getNewNameEN(context: Context): String {
        return suspendCancellableCoroutine { continuation ->
            val editText = EditText(context)
            val dialog = AlertDialog.Builder(context)
                .setTitle("Enter the new name please")
                .setView(editText)
                .setPositiveButton("OK") { _, _ ->
                    val locationName = editText.text.toString()
                    if (locationName.isNotEmpty()) {
                        continuation.resume(locationName)
                    } else {
                        doToast("Location name cannot be empty")
                        continuation.resumeWithException(Exception("Location name cannot be empty"))
                    }
                }
                .setNegativeButton("Cancel") { _, _ ->
                    continuation.resumeWithException(Exception("User cancelled the dialog"))
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
    private suspend fun getChoiceTR(context: Context): String {
        return suspendCancellableCoroutine { continuation ->
            val dialog = AlertDialog.Builder(context)
                .setTitle("Seçiniz:")
                .setMessage("Not:Konumu silmek veya konumun adını değiştirmek kurulu tüm alarm/bildirimleri iptal eder")
                .setPositiveButton("Yeni konum olarak ayarla") { _, _ ->
                    continuation.resume("set")
                }
                .setNeutralButton("Adını değiştir") { _, _ ->
                    continuation.resume("change")
                }
                .setNegativeButton("Konumu sil") { _, _ ->
                    continuation.resume("delete")
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
    private suspend fun getChoiceEN(context: Context): String {
        return suspendCancellableCoroutine { continuation ->
            val dialog = AlertDialog.Builder(context)
                .setTitle("Choose an option:")
                .setMessage("Note: Deleting the location or renaming the location will cancel all installed alarms/notifications")
                .setPositiveButton("Set as current location") { _, _ ->
                    continuation.resume("set")
                }
                .setNeutralButton("Change location name") { _, _ ->
                    continuation.resume("change")
                }
                .setNegativeButton("Delete location") { _, _ ->
                    continuation.resume("delete")
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
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("language", language)
        }
        startActivity(intent)
        finish()
    }
}