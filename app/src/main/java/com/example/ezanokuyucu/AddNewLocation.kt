package com.example.ezanokuyucu

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
            addLocationButton.text = "Konumu Ekle"
        }
        else{
            addLocationButton.text = "Add Location"
        }

        // WebViewClient kullanarak URL değişikliklerini takip et
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // WebView sayfası yüklendikten sonra URL'i al ve işle
                url?.let { handleUrl(it) }
            }
        }

        // WebView içinde JavaScript ile sayfa yüklendikten sonra yapılacak işlemler
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress == 100) {
                    // Sayfa yüklendiğinde burada ek işlemler yapılabilir
                }
            }
        }
    
        
        // WebView'e Diyanet namaz vakitleri sitesini yükle
        webView.loadUrl("https://namazvakitleri.diyanet.gov.tr/tr-TR")

        // addLocationButton'a tıklanma olayı ekle
        addLocationButton.setOnClickListener {
            val currentUrl = webView.url
            currentUrl?.let { url ->
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val name:String
                        if(language == "TR"){
                            name = getLocationNameTR(this@AddNewLocation)
                        }
                        else{
                            name = getLocationNameEN(this@AddNewLocation)
                        }
                        popUpMessage("w",name+ "\n"+url)
                        addLocationToFile(name,url)
                    } catch (e: Exception) {
                        // Hata işle
                    }
                }
            } ?: run {
                Log.e("MainActivity", "URL is null")
            }
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
    private fun addLocationToFile(name: String, url: String) {
        val file = File(this.filesDir, "myLocations.csv")

        if (!file.exists()) {
            file.writeText("$name,$url\n")
        } else {
            val currentContent = file.readText()
            val newContent = "$name,$url\n" + currentContent
            file.writeText(newContent)
        }
    }
    private suspend fun getLocationNameEN(context: Context): String {
        return suspendCancellableCoroutine { continuation ->
            val editText = EditText(context)
            val dialog = AlertDialog.Builder(context)
                .setTitle("Enter Location Name")
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
    private fun handleUrl(url: String) {
        // URL'i işleme fonksiyonu
        // Burada URL ile istediğiniz işlemleri yapabilirsiniz
        // Örneğin, URL'i parse edebilir veya farklı bir API'ye gönderebilirsiniz
        // Şu anda sadece loglama yapıyoruz
        Log.d("MainActivity", "Handling URL: $url")
    }
    private fun popUpMessage(title: String, message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("Okay") { dialog, which ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    private fun doToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}


/*private suspend fun fetchPrayerTimes(country: String, city: String, district: String): String? {
        return withContext(Dispatchers.IO) {
            val url = "https://namazvakitleri.diyanet.gov.tr/tr-TR"

            try {
                // İlk olarak URL'ye bağlanma işlemi
                Log.d("FetchPrayerTimes", "Connecting to URL: $url")
                val document: Document = Jsoup.connect(url).get()
                Log.d("FetchPrayerTimes", "Connected successfully.")

                // Form verilerini hazırlama
                val formData = hashMapOf(
                    "country" to country,
                    "state" to city,
                    "stateRegion" to district
                )

                // Form verilerini loglama
                Log.d("FetchPrayerTimes", "Form Data: $formData")

                // Post isteği gönderme
                val response = Jsoup.connect(url)
                    .data(formData)
                    .method(org.jsoup.Connection.Method.POST)
                    .execute()

                // Post isteğini loglama
                Log.d("FetchPrayerTimes", "POST request sent.")


                val body = response.body()

                // JSoup ile sayfa içeriğini parse et
                val parsedDoc = Jsoup.parse(body)

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

                // İşlenmiş veriyi loglama
                Log.d("FetchPrayerTimes", "Prayer Times String: $prayerTimesString")

                return@withContext prayerTimesString
            } catch (e: Exception) {
                Log.e("FetchPrayerTimes", "Error fetching prayer times", e)
                return@withContext null
            }
        }
    }




    private fun displayPrayerTimes(prayerTimes: String?) {
        prayerTimes?.let { times ->
            val csvFileName = "prayer_times.csv"
            val csvContent = StringBuilder()
            csvContent.append("Tarih,İmsak,Güneş,Öğle,İkindi,Akşam,Yatsı\n") // .csv dosyasının başlığını oluştur

            times.lines().forEach { line ->
                val prayerTimesArray = line.split(", ")
                if (prayerTimesArray.size == 8) {
                    csvContent.append("${prayerTimesArray[0]},${prayerTimesArray[2]},${prayerTimesArray[3]},${prayerTimesArray[4]},${prayerTimesArray[5]},${prayerTimesArray[6]},${prayerTimesArray[7]}\n")
                }
                popUpMessage("h",line)
            }

            // Dosyayı oluştur ve yaz
            val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadFolder, csvFileName)
            file.writeText(csvContent.toString())

            // Kullanıcıya dosyanın indirildiği yeri bildir
            val context = applicationContext
            val toastText = "Namaz vakitleri .csv dosyası olarak indirildi: ${file.absolutePath}"
            Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()
        } ?: run {
            // Eğer veri yoksa, kullanıcıya hata bildir
            val context = applicationContext
            Toast.makeText(context, "Veri bulunamadı.", Toast.LENGTH_SHORT).show()
        }
    }





    private fun doToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }*/
