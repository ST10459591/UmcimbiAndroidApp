package com.example.umcimbisa

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class SettingsActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        val backButton =
            findViewById<TextView>(R.id.backButton)

        val nameInput =
            findViewById<EditText>(R.id.nameInput)

        val phoneInput =
            findViewById<EditText>(R.id.phoneInput)

        val languageSpinner =
            findViewById<Spinner>(R.id.languageSpinner)

        val notificationSwitch =
            findViewById<Switch>(R.id.notificationSwitch)

        val darkModeSwitch =
            findViewById<Switch>(R.id.darkModeSwitch)

        val saveButton =
            findViewById<Button>(R.id.saveButton)

        val logoutButton =
            findViewById<Button>(R.id.logoutButton)

        // Available languages
        val languages = arrayOf(
            "English",
            "isiZulu",
            "seSotho"
        )

        val adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                languages
            )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        languageSpinner.adapter = adapter

        // Check logged-in user
        val user = auth.currentUser

        if (user == null) {
            goToLogin()
            return
        }

        val uid = user.uid

        // ------------------------------------------------
        // LOAD USER SETTINGS
        // ------------------------------------------------

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    return@addOnSuccessListener
                }

                nameInput.setText(
                    document.getString("name") ?: ""
                )

                phoneInput.setText(
                    document.getString("phone") ?: ""
                )

                notificationSwitch.isChecked =
                    document.getBoolean("notifications")
                        ?: true

                val darkMode =
                    document.getBoolean("darkMode")
                        ?: false

                darkModeSwitch.isChecked =
                    darkMode

                val language =
                    document.getString("language")
                        ?: "English"

                val position =
                    languages.indexOf(language)

                if (position >= 0) {
                    languageSpinner.setSelection(position)
                }

                // Apply saved theme
                if (darkMode) {

                    AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES
                    )

                } else {

                    AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO
                    )
                }
            }

        // ------------------------------------------------
        // BACK BUTTON
        // ------------------------------------------------

        backButton.setOnClickListener {
            finish()
        }

        // ------------------------------------------------
        // DARK MODE
        // ------------------------------------------------

        darkModeSwitch.setOnCheckedChangeListener {
                _, isChecked ->

            if (isChecked) {

                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
                )

            } else {

                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }

        // ------------------------------------------------
        // SAVE SETTINGS
        // ------------------------------------------------

        saveButton.setOnClickListener {

            val name =
                nameInput.text
                    .toString()
                    .trim()

            val phone =
                phoneInput.text
                    .toString()
                    .trim()

            val language =
                languageSpinner
                    .selectedItem
                    .toString()

            // Validate name
            if (name.isEmpty()) {

                nameInput.error =
                    "Enter your name"

                nameInput.requestFocus()

                return@setOnClickListener
            }

            // Disable button while saving
            saveButton.isEnabled = false

            val settings =
                hashMapOf<String, Any>(

                    "name" to name,

                    "phone" to phone,

                    "language" to language,

                    "notifications" to
                            notificationSwitch.isChecked,

                    "darkMode" to
                            darkModeSwitch.isChecked
                )

            db.collection("users")
                .document(uid)
                .set(
                    settings,
                    SetOptions.merge()
                )
                .addOnSuccessListener {

                    saveButton.isEnabled = true

                    Toast.makeText(
                        this,
                        "Settings saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {

                    saveButton.isEnabled = true

                    Toast.makeText(
                        this,
                        "Failed to save settings",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        // ------------------------------------------------
        // LOGOUT
        // ------------------------------------------------

        logoutButton.setOnClickListener {

            auth.signOut()

            goToLogin()
        }
    }

    // ----------------------------------------------------
    // GO TO LOGIN
    // ----------------------------------------------------

    private fun goToLogin() {

        val intent =
            Intent(
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