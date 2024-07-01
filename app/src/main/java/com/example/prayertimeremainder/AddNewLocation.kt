package com.example.prayertimeremainder

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
//import android.util.Log

class AddNewLocation : AppCompatActivity() {
    private lateinit var linearLayout: LinearLayout
    private lateinit var addLocationButton: Button
    private lateinit var webView: WebView
    private lateinit var language: String

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_new_location_page)

        language = intent.getStringExtra("language")!!
        linearLayout = findViewById(R.id.linearLayout)
        addLocationButton = findViewById(R.id.addLocationButton)
        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true

        if(language == "TR"){
            addLocationButton.text = buildString { append("Konumu Ekle")
            }
        }
        else{
            addLocationButton.text = buildString { append("Add Location")
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let { handleUrl(it) }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress == 100) {
                    // Sayfa yüklendiğinde burada ek işlemler yapılabilir
                }
            }
        }

        webView.loadUrl("https://namazvakitleri.diyanet.gov.tr/tr-TR")

        addLocationButton.setOnClickListener {
            val currentUrl = webView.url
            currentUrl?.let { url ->
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        var name:String
                        if(language == "TR"){
                            name = getLocationNameTR(this@AddNewLocation)
                            while (!isNewNameValid(name)) {
                                doToast("Lütfen dosya isimleri için geçersiz karakterler kullanmayın")
                                name = getLocationNameTR(this@AddNewLocation)
                            }
                        }
                        else{
                            name = getLocationNameEN(this@AddNewLocation)
                            while (!isNewNameValid(name)) {
                                doToast("Please don't use invalid characters for file names")
                                name = getLocationNameEN(this@AddNewLocation)
                            }
                        }
                        if(isUrlValid(url)){
                            if(language == "TR"){
                                doToast("Konum ekleniyor...")
                            }
                            else{
                                doToast("Processing...")
                            }
                            deleteAlarms()
                            addLocationToFile(name,url)
                            getPrayerTimes(name,url)
                        }
                        else if(language == "TR"){
                            doToast("Lütfen ülkeden başlayarak tekrar seçim yapınız")
                        }
                        else{
                            doToast("Please choose your location again starting from country")
                        }
                    } catch (e: Exception) {
                        //Log.e("AddNewLocation", e.toString())
                    }
                }
            } ?: run {
                //Log.e("MainActivity", "URL is null")
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
                //Log.d("Alarm Receiver", "removed $requestCode")
                apply()
            }
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
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
    private fun isUrlValid(url: String): Boolean{
        val parts = url.substring(8).split("/")
        if(parts.size == 4 && parts[0] == "namazvakitleri.diyanet.gov.tr" && parts[1] == "tr-TR" &&
            parts[2].toInt() > 1 && parts[3].contains("-icin-namaz-vakti")){
            return true
        }
        else{
            return false
        }
    }
    private fun getPrayerTimes(name: String, url: String){
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val prayerTimes = fetchPrayerTimes(url)
                if (prayerTimes != null) {
                    //Log.d("Add New Location", "Prayer Times: $prayerTimes")
                    createCSVFile(prayerTimes, "$name.csv")
                } else {
                    //Log.e("Add New Location", "Prayer times could not be fetched.")
                }
                if(language == "TR"){
                    doToast("Konum eklendi")
                }
                else{
                    doToast("Location added successfully")
                }
            } catch (e: Exception) {
                //Log.e("FetchPrayerTimes", "Error fetching prayer times", e)
            }
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
            if (line.isNotBlank()) {  // Check if the line is not blank
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
    private suspend fun fetchPrayerTimes(url: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val document: Document = Jsoup.connect(url).get()
                val body = document.body()

                val parsedDoc = Jsoup.parse(body.toString())

                val prayerTimesElement: Element? = parsedDoc.selectFirst("#tab-0 table.vakit-table tbody")
                //Log.d("FetchPrayerTimes", "Prayer Times Element found: $prayerTimesElement")

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

                val prayerTimesString = prayerTimesStringBuilder.toString()

                return@withContext prayerTimesString
            } catch (e: Exception) {
                //Log.e("FetchPrayerTimes", "Error fetching prayer times", e)
                return@withContext null
            }
        }
    }
    private fun addLocationToFile(name: String, url: String) {
        val file = File(this.filesDir, "myLocations.csv")

        if (!file.exists()) {
            file.writeText("$name,$url\n")
        } else {
            val currentContent = file.readText()
            val newContent = "$name,$url\n$currentContent"
            file.writeText(newContent)
        }
    }
    private fun handleUrl(url: String) {
        //Log.d("MainActivity", "Handling URL: $url")
    }
    private fun doToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private suspend fun getLocationNameEN(context: Context): String {
        return suspendCancellableCoroutine { continuation ->
            val editText = EditText(context)
            val dialog = AlertDialog.Builder(context)
                .setTitle("Enter Location Name")
                .setMessage("Note:Set alarms/notifications will be deleted as the new location will be set as your current location.")
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
    private suspend fun getLocationNameTR(context: Context): String {
        return suspendCancellableCoroutine { continuation ->
            val editText = EditText(context)
            val dialog = AlertDialog.Builder(context)
                .setTitle("Konum Adını Giriniz")
                .setMessage("Not:Yeni konum şuanki konumunuz olarak ayarlanacağı için kayıtlı alarmlar/bildirimler silinecek")
                .setView(editText)
                .setPositiveButton("Tamam") { _, _ ->
                    val locationName = editText.text.toString()
                    if (locationName.isNotEmpty()) {
                        continuation.resume(locationName)
                    } else {
                        doToast("Konum adı alanı boş bırakılamaz")
                        continuation.resumeWithException(Exception("Konum adı boş olamaz"))
                    }
                }
                .setNegativeButton("İptal") { _, _ ->
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
