package com.example.ezanokuyucu

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileNotFoundException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainActivity : AppCompatActivity() {
    private lateinit var linearLayout: LinearLayout
    private lateinit var locationTextView: TextView
    private lateinit var languageTextView: TextView
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var languageSwitch: Switch
    private lateinit var language:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        language = intent.getStringExtra("language") ?: "TR"
        linearLayout = findViewById(R.id.linearLayout)
        locationTextView = findViewById(R.id.location_text_view)
        languageSwitch = findViewById(R.id.language_switch)
        languageTextView = findViewById(R.id.language_text_view)

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

        locationTextView.text = checkSavedLocations()
    }
    private fun callPage(choice: String){
        if(choice == "new"){
            val intent = Intent(this, AddNewLocation::class.java)
            intent.putExtra("language",languageTextView.text.toString())
            startActivity(intent)
            finish()
        }
        else{
            doToast("savedLocations")
            val intent = Intent(this, SavedLocations::class.java)
            intent.putExtra("language", languageTextView.text.toString())
            startActivity(intent)
            finish()
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
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
    private fun doToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

