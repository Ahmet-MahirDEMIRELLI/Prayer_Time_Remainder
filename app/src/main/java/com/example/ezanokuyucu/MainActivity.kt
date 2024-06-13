package com.example.ezanokuyucu

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
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
    private fun setTimeFields() {
        val fileName = locationTextView.text.toString() + ".csv"
        val file = File(this.filesDir, fileName)

        if (file.exists()) {
            val inputStream = FileInputStream(file)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val firstLine = bufferedReader.readLine() // Read the first data line
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
                    val currentDate = currentDateTime.toString().substring(0,11)
                    val currentTime = currentDateTime.toString().substring(11,19)

                    val currentTimeParsed = LocalTime.parse(currentTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
                    val lastPrayerTime = LocalTime.parse(time5, DateTimeFormatter.ofPattern("HH:mm"))
                    if (currentTimeParsed.isAfter(lastPrayerTime)) {
                        val nextLine = bufferedReader.readLine()
                        parts = nextLine.toString().split(",")
                        dateTextView.text = parts[0]
                        time1TextView.text = parts[1]
                        time2TextView.text = parts[2]
                        time3TextView.text = parts[3]
                        time4TextView.text = parts[4]
                        time5TextView.text = parts[5]
                        // updateFile
                    }
                    else {
                        dateTextView.text = date
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

