package com.example.ezanokuyucu

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
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
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainActivity : AppCompatActivity() {
    private lateinit var linearLayout: LinearLayout
    private lateinit var locationTextView: TextView
    private lateinit var languageTextView: TextView
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var languageSwitch: Switch
    private lateinit var language:String
    private lateinit var seeButton: Button
    private lateinit var time1TextView: TextView
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var time1Switch: Switch
    private lateinit var time2TextView: TextView
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var time2Switch: Switch
    private lateinit var time3TextView: TextView
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var time3Switch: Switch
    private lateinit var time4TextView: TextView
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var time4Switch: Switch
    private lateinit var time5TextView: TextView
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var time5Switch: Switch
    private lateinit var dateTextView: TextView
    private lateinit var setTimersButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        language = intent.getStringExtra("language") ?: "TR"
        linearLayout = findViewById(R.id.linearLayout)
        locationTextView = findViewById(R.id.location_text_view)
        languageSwitch = findViewById(R.id.language_switch)
        languageTextView = findViewById(R.id.language_text_view)
        time1TextView = findViewById(R.id.time1_text_view)
        time2TextView = findViewById(R.id.time2_text_view)
        time3TextView = findViewById(R.id.time3_text_view)
        time4TextView = findViewById(R.id.time4_text_view)
        time5TextView = findViewById(R.id.time5_text_view)
        time1Switch = findViewById(R.id.time1_switch)
        time2Switch = findViewById(R.id.time2_switch)
        time3Switch = findViewById(R.id.time3_switch)
        time4Switch = findViewById(R.id.time4_switch)
        time5Switch = findViewById(R.id.time5_switch)
        dateTextView = findViewById(R.id.date_text_view)
        setTimersButton = findViewById(R.id.setTimersButton)
        seeButton = findViewById(R.id.see)

        if(language == "EN"){
            languageTextView.setText("EN")
            val params = languageTextView.layoutParams as LinearLayout.LayoutParams
            params.marginStart = dpToPx(370)
            languageTextView.layoutParams = params
        }

        languageSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                languageTextView.setText("EN")
                val params = languageTextView.layoutParams as LinearLayout.LayoutParams
                params.marginStart = dpToPx(370)
                languageTextView.layoutParams = params
            } else {
                languageTextView.setText("TR")
                val params = languageTextView.layoutParams as LinearLayout.LayoutParams
                params.marginStart = dpToPx(350)
                languageTextView.layoutParams = params
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

        seeButton.setOnClickListener {
            val intent = Intent(this, ListPage::class.java)
            intent.putExtra("name",locationTextView.text.toString())
            startActivity(intent)
        }
        setTimersButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val validityWithTime = checkValidity()
                for (i in 0..8 step 2) {
                    if (validityWithTime[i] == "1"){
                        setTimer(validityWithTime[i+1], (i/2) + 1)
                    }
                }
            }

        }
        locationTextView.text = checkSavedLocations()

        if(locationTextView.text.toString() == "Kayıtlı Konum Yok" || locationTextView.text.toString() == "No Saved Location"){
            time1Switch.visibility = View.INVISIBLE
            time2Switch.visibility = View.INVISIBLE
            time3Switch.visibility = View.INVISIBLE
            time4Switch.visibility = View.INVISIBLE
            time5Switch.visibility = View.INVISIBLE
            dateTextView.visibility = View.INVISIBLE
            time1TextView.visibility = View.INVISIBLE
            time2TextView.visibility = View.INVISIBLE
            time3TextView.visibility = View.INVISIBLE
            time4TextView.visibility = View.INVISIBLE
            time5TextView.visibility = View.INVISIBLE
        }
        else {
            time1Switch.visibility = View.VISIBLE
            time2Switch.visibility = View.VISIBLE
            time3Switch.visibility = View.VISIBLE
            time4Switch.visibility = View.VISIBLE
            time5Switch.visibility = View.VISIBLE
            dateTextView.visibility = View.VISIBLE
            time1TextView.visibility = View.VISIBLE
            time2TextView.visibility = View.VISIBLE
            time3TextView.visibility = View.VISIBLE
            time4TextView.visibility = View.VISIBLE
            time5TextView.visibility = View.VISIBLE
            setTimeFields()
        }
    }
    @SuppressLint("ScheduleExactAlarm")
    private fun setTimer(time: String, requestCode: Int){
//        val (hour, minute) = time.split(":").map { it.toInt() }
//
//        val calendar = Calendar.getInstance().apply {
//            set(Calendar.HOUR_OF_DAY, hour)
//            set(Calendar.MINUTE, minute)
//            set(Calendar.SECOND, 0)
//        }
//
//        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val intent = Intent(this, AlarmReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//
//        alarmManager.setExact(
//            AlarmManager.RTC_WAKEUP,
//            calendar.timeInMillis,
//            pendingIntent
//        )

        doToast("$time $requestCode")
    }
    private suspend fun checkValidity(): Array<String> {
        return withContext(Dispatchers.IO) {
            val validityWithTime = Array(10) { "0" }
            val currentDateTime = LocalDateTime.now()
            val currentDate = currentDateTime.toString().substring(0, 10)
            val currentTime = currentDateTime.toString().substring(11, 19)
            val currentTimeParsed = LocalTime.parse(currentTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
            val temp = dateTextView.text.toString().split("-")
            val date = temp[2] + "-" + temp[1] + "-" + temp[0]
            val currentDateParsed = LocalDate.parse(currentDate, DateTimeFormatter.ISO_LOCAL_DATE)
            val dateParsed = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
            if (dateParsed.isAfter(currentDateParsed)) {
                if (time1Switch.isChecked) {
                    validityWithTime[0] = "1"
                    validityWithTime[1] = time1TextView.text.toString()
                }
                if (time2Switch.isChecked) {
                    validityWithTime[2] = "1"
                    validityWithTime[3] = time2TextView.text.toString()
                }
                if (time3Switch.isChecked) {
                    validityWithTime[4] = "1"
                    validityWithTime[5] = time3TextView.text.toString()
                }
                if (time4Switch.isChecked) {
                    validityWithTime[6] = "1"
                    validityWithTime[7] = time4TextView.text.toString()
                }
                if (time5Switch.isChecked) {
                    validityWithTime[8] = "1"
                    validityWithTime[9] = time5TextView.text.toString()
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
                    if (time1Switch.isChecked) {
                        validityWithTime[0] = "1"
                        validityWithTime[1] = time1TextView.text.toString()
                    }
                    if (time2Switch.isChecked) {
                        validityWithTime[2] = "1"
                        validityWithTime[3] = time2TextView.text.toString()
                    }
                    if (time3Switch.isChecked) {
                        validityWithTime[4] = "1"
                        validityWithTime[5] = time3TextView.text.toString()
                    }
                    if (time4Switch.isChecked) {
                        validityWithTime[6] = "1"
                        validityWithTime[7] = time4TextView.text.toString()
                    }
                    if (time5Switch.isChecked) {
                        validityWithTime[8] = "1"
                        validityWithTime[9] = time5TextView.text.toString()
                    }
                } else if (prayerTime2Parsed.isAfter(currentTimeParsed)) {
                    if (time2Switch.isChecked) {
                        validityWithTime[2] = "1"
                        validityWithTime[3] = time2TextView.text.toString()
                    }
                    if (time3Switch.isChecked) {
                        validityWithTime[4] = "1"
                        validityWithTime[5] = time3TextView.text.toString()
                    }
                    if (time4Switch.isChecked) {
                        validityWithTime[6] = "1"
                        validityWithTime[7] = time4TextView.text.toString()
                    }
                    if (time5Switch.isChecked) {
                        validityWithTime[8] = "1"
                        validityWithTime[9] = time5TextView.text.toString()
                    }
                } else if (prayerTime3Parsed.isAfter(currentTimeParsed)) {
                    if (time3Switch.isChecked) {
                        validityWithTime[4] = "1"
                        validityWithTime[5] = time3TextView.text.toString()
                    }
                    if (time4Switch.isChecked) {
                        validityWithTime[6] = "1"
                        validityWithTime[7] = time4TextView.text.toString()
                    }
                    if (time5Switch.isChecked) {
                        validityWithTime[8] = "1"
                        validityWithTime[9] = time5TextView.text.toString()
                    }
                } else if (prayerTime4Parsed.isAfter(currentTimeParsed)) {
                    if (time4Switch.isChecked) {
                        validityWithTime[6] = "1"
                        validityWithTime[7] = time4TextView.text.toString()
                    }
                    if (time5Switch.isChecked) {
                        validityWithTime[8] = "1"
                        validityWithTime[9] = time5TextView.text.toString()
                    }
                } else if (prayerTime5Parsed.isAfter(currentTimeParsed)) {
                    if (time5Switch.isChecked) {
                        validityWithTime[8] = "1"
                        validityWithTime[9] = time5TextView.text.toString()
                    }
                }
            }
            return@withContext validityWithTime
        }
    }
    private fun setTimeFields() {
        val fileName = locationTextView.text.toString() + ".csv"
        val file = File(this.filesDir, fileName)
        if (file.exists()) {
            var inputStream = FileInputStream(file)
            var bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var firstLine = bufferedReader.readLine() // Read the first data line
            firstLine?.let {
                var parts = it.split(",")
                if (parts.size == 6) {
                    val date = parts[0]
                    val time1 = parts[1]
                    val time2 = parts[2]
                    val time3 = parts[3]
                    val time4 = parts[4]
                    val time5 = parts[5]

                    val currentDateTime = LocalDateTime.now()
                    val currentDate = currentDateTime.toString().substring(0,10)
                    val currentTime = currentDateTime.toString().substring(11,19)
                    val currentDateParsed = LocalDate.parse(currentDate, DateTimeFormatter.ISO_LOCAL_DATE)
                    val dateParsed = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)

                    val currentTimeParsed = LocalTime.parse(currentTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
                    val lastPrayerTime = LocalTime.parse(time5, DateTimeFormatter.ofPattern("HH:mm"))
                    if (currentTimeParsed.isAfter(lastPrayerTime) || currentDateParsed.isAfter(dateParsed)) {
                        bufferedReader.close()
                        inputStream.close()
                        CoroutineScope(Dispatchers.Main).launch {
                            val dummy = updateFile(fileName)
                            inputStream = FileInputStream(file)
                            bufferedReader = BufferedReader(InputStreamReader(inputStream))
                            firstLine = bufferedReader.readLine()
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
                    }
                    else {
                        val dateParts = date.split("-")
                        val tmp = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0]
                        dateTextView.text = tmp
                        time1TextView.text = time1
                        time2TextView.text = time2
                        time3TextView.text = time3
                        time4TextView.text = time4
                        time5TextView.text = time5
                    }

                }
            }
            bufferedReader.close()
            inputStream.close()
        }
    }
    private suspend fun updateFile(fileName: String): Boolean{
        val location = fileName.substring(0, fileName.indexOf("."))
        val file = File(this.filesDir, "myLocations.csv")
        return withContext(Dispatchers.IO) {
            if (file.exists()) {
                val bufferedReader = file.bufferedReader()
                val firstLine = bufferedReader.readLine()
                bufferedReader.close()
                val parts = firstLine.split(",")
                val url = parts[1]
                getPrayerTimes(location, url)
            }
            return@withContext true
        }
    }
    private fun getPrayerTimes(name: String, url: String){
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val prayerTimes = fetchPrayerTimes(url)
                if (prayerTimes != null) {
                    Log.d("FetchPrayerTimes", "Prayer Times: $prayerTimes")
                    createCSVFile(prayerTimes, "$name.csv")
                } else {
                    Log.e("FetchPrayerTimes", "Prayer times could not be fetched.")
                }
            } catch (e: Exception) {
                Log.e("FetchPrayerTimes", "Error fetching prayer times", e)
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
        var mounth: String = when(parts[1]) {
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
        return parts[2] + "-" + mounth + "-" + parts[0]
    }
    private suspend fun fetchPrayerTimes(url: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val document: Document = Jsoup.connect(url).get()
                val body = document.body()

                // JSoup ile sayfa içeriğini parse et
                val parsedDoc = Jsoup.parse(body.toString())

                // Cevabı parse etme
                val prayerTimesElement: Element? = parsedDoc.selectFirst("#tab-0 table.vakit-table tbody")
                Log.d("FetchPrayerTimes", "Prayer Times Element found: $prayerTimesElement")

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
                Log.e("FetchPrayerTimes", "Error fetching prayer times", e)
                return@withContext null
            }
        }
    }
    private fun callPage(choice: String){
        if(choice == "new"){
            val intent = Intent(this, AddNewLocation::class.java)
            intent.putExtra("language",languageTextView.text.toString())
            startActivity(intent)
            finish()
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
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
    private fun doToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

