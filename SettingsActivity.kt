package com.example.umcimbisa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class SettingsActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val languages = arrayOf(
        "English",
        "isiZulu",
        "Sesotho"
    )

    private val languageCodes = arrayOf(
        "en",
        "zu",
        "st"
    )

    private lateinit var nameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var languageSpinner: Spinner
    private lateinit var notificationSwitch: Switch
    private lateinit var darkModeSwitch: Switch
    private lateinit var saveButton: Button

    private var settingsLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        nameInput = findViewById(R.id.nameInput)
        phoneInput = findViewById(R.id.phoneInput)
        languageSpinner = findViewById(R.id.languageSpinner)
        notificationSwitch = findViewById(R.id.notificationSwitch)
        darkModeSwitch = findViewById(R.id.darkModeSwitch)
        saveButton = findViewById(R.id.saveButton)

        val backButton =
            findViewById<TextView>(R.id.backButton)

        val logoutButton =
            findViewById<Button>(R.id.logoutButton)

        backButton.setOnClickListener {
            finish()
        }

        val user = auth.currentUser

        if (user == null) {
            goToLogin()
            return
        }

        val uid = user.uid

        val preferences = getSharedPreferences(
            "umcimbi_settings_$uid",
            MODE_PRIVATE
        )

        // LANGUAGE DROPDOWN
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languages
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        languageSpinner.adapter = adapter

        val currentLanguage =
            AppCompatDelegate.getApplicationLocales()
                .toLanguageTags()
                .substringBefore(",")

        val languagePosition =
            languageCodes.indexOf(currentLanguage)

        languageSpinner.setSelection(
            if (languagePosition >= 0) languagePosition else 0
        )

        // RESTORE LOCALLY SAVED PREFERENCES
        notificationSwitch.isChecked =
            preferences.getBoolean("notifications", true)

        darkModeSwitch.isChecked =
            preferences.getBoolean("darkMode", false)

        // WAIT FOR FIREBASE BEFORE SAVING
        saveButton.isEnabled = false

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (isFinishing || isDestroyed) {
                    return@addOnSuccessListener
                }

                if (document.exists()) {

                    nameInput.setText(
                        document.getString("name").orEmpty()
                    )

                    phoneInput.setText(
                        document.getString("phone").orEmpty()
                    )

                    notificationSwitch.isChecked =
                        document.getBoolean("notifications")
                            ?: preferences.getBoolean(
                                "notifications",
                                true
                            )

                    darkModeSwitch.isChecked =
                        document.getBoolean("darkMode")
                            ?: preferences.getBoolean(
                                "darkMode",
                                false
                            )

                    val savedLanguage =
                        document.getString("language")

                    val position = languages.indexOfFirst {
                        it.equals(
                            savedLanguage,
                            ignoreCase = true
                        ) || (
                                it == "Sesotho" &&
                                        savedLanguage.equals(
                                            "seSotho",
                                            ignoreCase = true
                                        )
                                )
                    }

                    if (position >= 0) {
                        languageSpinner.setSelection(position)
                    }
                }

                settingsLoaded = true
                saveButton.isEnabled = true
            }
            .addOnFailureListener {

                if (isFinishing || isDestroyed) {
                    return@addOnFailureListener
                }

                settingsLoaded = true
                saveButton.isEnabled = true

                Toast.makeText(
                    this,
                    "Unable to load cloud settings. " +
                            "Local preferences are available.",
                    Toast.LENGTH_LONG
                ).show()
            }

        // SAVE SETTINGS
        saveButton.setOnClickListener {

            if (!settingsLoaded) {
                return@setOnClickListener
            }

            val name =
                nameInput.text.toString().trim()

            val phone =
                phoneInput.text.toString().trim()

            if (name.isEmpty()) {
                nameInput.error = "Enter your name"
                nameInput.requestFocus()
                return@setOnClickListener
            }

            val selectedPosition =
                languageSpinner.selectedItemPosition

            if (selectedPosition !in languages.indices) {
                return@setOnClickListener
            }

            val language =
                languages[selectedPosition]

            val languageCode =
                languageCodes[selectedPosition]

            val notificationsEnabled =
                notificationSwitch.isChecked

            val darkModeEnabled =
                darkModeSwitch.isChecked

            val settings = hashMapOf<String, Any>(
                "name" to name,
                "phone" to phone,
                "language" to language,
                "notifications" to notificationsEnabled,
                "darkMode" to darkModeEnabled
            )

            saveButton.isEnabled = false

            db.collection("users")
                .document(uid)
                .set(settings, SetOptions.merge())
                .addOnSuccessListener {

                    preferences.edit()
                        .putBoolean(
                            "notifications",
                            notificationsEnabled
                        )
                        .putBoolean(
                            "darkMode",
                            darkModeEnabled
                        )
                        .apply()

                    Toast.makeText(
                        this,
                        "Settings saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    val nightMode =
                        if (darkModeEnabled) {
                            AppCompatDelegate.MODE_NIGHT_YES
                        } else {
                            AppCompatDelegate.MODE_NIGHT_NO
                        }

                    AppCompatDelegate.setDefaultNightMode(
                        nightMode
                    )

                    val locales =
                        LocaleListCompat.forLanguageTags(
                            languageCode
                        )

                    if (
                        AppCompatDelegate
                            .getApplicationLocales()
                            .toLanguageTags() != languageCode
                    ) {
                        AppCompatDelegate
                            .setApplicationLocales(locales)
                    }

                    saveButton.isEnabled = true
                }
                .addOnFailureListener { error ->

                    saveButton.isEnabled = true

                    Toast.makeText(
                        this,
                        "Failed to save settings: " +
                                error.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        // LOGOUT
        logoutButton.setOnClickListener {

            logoutButton.isEnabled = false

            db.collection("users")
                .document(uid)
                .update("fcmToken", "")
                .addOnSuccessListener {

                    auth.signOut()
                    goToLogin()
                }
                .addOnFailureListener {

                    logoutButton.isEnabled = true

                    Toast.makeText(
                        this,
                        "Unable to clear notification token. " +
                                "Please check your connection.",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {

        findViewById<TextView>(R.id.navHomeButton)
            .setOnClickListener {
                openPage(HomeActivity::class.java)
            }

        findViewById<TextView>(R.id.navExploreButton)
            .setOnClickListener {
                openPage(CategoriesActivity::class.java)
            }

        findViewById<TextView>(R.id.navBookingsButton)
            .setOnClickListener {
                openPage(MyBookingsActivity::class.java)
            }

        findViewById<TextView>(R.id.navMessagesButton)
            .setOnClickListener {
                openPage(MessagesActivity::class.java)
            }

        findViewById<TextView>(R.id.navProfileButton)
            .setOnClickListener {
                openPage(ProfileActivity::class.java)
            }

        findViewById<TextView>(R.id.navProfileButton)
            .setTextColor(Color.parseColor("#111111"))

        listOf(
            R.id.navHomeButton,
            R.id.navExploreButton,
            R.id.navBookingsButton,
            R.id.navMessagesButton
        ).forEach { id ->

            findViewById<TextView>(id)
                .setTextColor(Color.parseColor("#666666"))
        }
    }

    private fun openPage(
        destination: Class<out android.app.Activity>
    ) {

        if (destination == this::class.java) return

        val intent = Intent(this, destination)

        intent.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        )

        startActivity(intent)
    }

    private fun goToLogin() {

        val intent = Intent(
            this,
            LoginActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}
