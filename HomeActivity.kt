package com.example.umcimbisa

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // NOTIFICATION PERMISSION LAUNCHER
    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { _ ->
            // Android saves the permission choice.
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        // REQUEST NOTIFICATION PERMISSION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }

        // FIND BUTTONS
        val searchButton =
            findViewById<Button>(R.id.searchServicesButton)

        val categoriesButton =
            findViewById<Button>(R.id.categoriesButton)

        val exploreButton =
            findViewById<TextView>(R.id.exploreButton)

        val bookingsButton =
            findViewById<TextView>(R.id.bookingsButton)

        val profileButton =
            findViewById<TextView>(R.id.profileButton)

        val languageButton =
            findViewById<TextView>(R.id.languageButton)

        // DISPLAY CURRENT LANGUAGE
        updateLanguageButton(languageButton)

        // LANGUAGE SELECTOR
        languageButton.setOnClickListener {

            val popupMenu = PopupMenu(
                this,
                languageButton
            )

            popupMenu.menu.add(0, 1, 0, "English")
            popupMenu.menu.add(0, 2, 1, "isiZulu")
            popupMenu.menu.add(0, 3, 2, "Sesotho")

            popupMenu.setOnMenuItemClickListener { item ->

                val languageCode: String
                val languageName: String

                when (item.itemId) {

                    1 -> {
                        languageCode = "en"
                        languageName = "English"
                    }

                    2 -> {
                        languageCode = "zu"
                        languageName = "isiZulu"
                    }

                    3 -> {
                        languageCode = "st"
                        languageName = "Sesotho"
                    }

                    else -> return@setOnMenuItemClickListener false
                }

                // APPLY LANGUAGE LOCALLY
                val locales =
                    LocaleListCompat.forLanguageTags(
                        languageCode
                    )

                AppCompatDelegate.setApplicationLocales(
                    locales
                )

                // SAVE TO FIRESTORE FOR THE USER ACCOUNT
                val uid = auth.currentUser?.uid

                if (uid != null) {
                    db.collection("users")
                        .document(uid)
                        .update("language", languageName)
                }

                true
            }

            popupMenu.show()
        }

        // SEARCH SERVICES
        searchButton.setOnClickListener {
            startActivity(
                Intent(this, SearchActivity::class.java)
            )
        }

        // BROWSE CATEGORIES
        categoriesButton.setOnClickListener {
            startActivity(
                Intent(this, CategoriesActivity::class.java)
            )
        }

        // EXPLORE NAVIGATION
        exploreButton.setOnClickListener {
            startActivity(
                Intent(this, CategoriesActivity::class.java)
            )
        }

        // MY BOOKINGS NAVIGATION
        bookingsButton.setOnClickListener {
            startActivity(
                Intent(this, MyBookingsActivity::class.java)
            )
        }

        // PROFILE NAVIGATION
        profileButton.setOnClickListener {
            startActivity(
                Intent(this, ProfileActivity::class.java)
            )
        }
    }

    // UPDATE LANGUAGE INDICATOR
    private fun updateLanguageButton(
        languageButton: TextView
    ) {

        val currentLanguage =
            AppCompatDelegate.getApplicationLocales()
                .toLanguageTags()
                .substringBefore(",")

        val label = when (currentLanguage) {
            "zu" -> "ZU  ▾"
            "st" -> "ST  ▾"
            else -> "EN  ▾"
        }

        languageButton.text = label
    }
}
