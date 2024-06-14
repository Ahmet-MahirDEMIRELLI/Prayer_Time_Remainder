package com.example.prayertimeremainder

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileNotFoundException

class ListPage : AppCompatActivity() {
    private lateinit var linearLayout: LinearLayout
    private lateinit var name:String
    private lateinit var listView: ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_page)

        name = intent.getStringExtra("name")!!
        linearLayout = findViewById(R.id.linearLayout)
        listView = findViewById(R.id.alistView)
        setListView()
    }
    private fun setListView() {
        val file = File(this.filesDir, name + ".csv")
        val locationNames = mutableListOf<String>()

        if (file.exists()) {
            try {
                val bufferedReader = file.bufferedReader()
                bufferedReader.forEachLine { line ->
                    locationNames.add(line)
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
        listView.adapter = adapter


    }
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
    private fun doToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}