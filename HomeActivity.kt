package com.example.umcimbisa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        val settingsButton =
            findViewById<LinearLayout>(R.id.settingsButton)

        val searchButton =
            findViewById<Button>(R.id.searchServicesButton)

        val categoriesButton =
            findViewById<Button>(R.id.categoriesButton)

        // SETTINGS
        settingsButton.setOnClickListener {

            val intent =
                Intent(
                    this@HomeActivity,
                    SettingsActivity::class.java
                )

            startActivity(intent)
        }

        // FIND EVENT SERVICES
        searchButton.setOnClickListener {

            val intent =
                Intent(
                    this,
                    SearchActivity::class.java
                )

            startActivity(intent)
        }

        // BROWSE CATEGORIES
        categoriesButton.setOnClickListener {

            val intent =
                Intent(
                    this,
                    CategoriesActivity::class.java
                )

            startActivity(intent)
        }
    }
}