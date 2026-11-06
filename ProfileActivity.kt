package com.example.umcimbisa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile)

        // FIND VIEWS
        val backButton =
            findViewById<TextView>(R.id.backButton)

        val profileEmail =
            findViewById<TextView>(R.id.profileEmail)

        val settingsButton =
            findViewById<TextView>(R.id.settingsButton)

        val favouritesButton =
            findViewById<TextView>(R.id.favouritesButton)

        val providerBookingsButton =
            findViewById<TextView>(R.id.providerBookingsButton)

        val logoutButton =
            findViewById<TextView>(R.id.logoutButton)

        // DISPLAY USER EMAIL
        val currentUser = auth.currentUser

        profileEmail.text =
            currentUser?.email ?: getString(R.string.your_account)

        // BACK BUTTON
        backButton.setOnClickListener {
            finish()
        }

        // SETTINGS
        settingsButton.setOnClickListener {
            startActivity(
                Intent(this, SettingsActivity::class.java)
            )
        }

        // FAVOURITES
        favouritesButton.setOnClickListener {
            startActivity(
                Intent(this, FavouritesActivity::class.java)
            )
        }

        // PROVIDER BOOKING REQUESTS
        providerBookingsButton.visibility = View.GONE

        val currentUid = currentUser?.uid

        if (currentUid != null) {

            db.collection("users")
                .document(currentUid)
                .get()
                .addOnSuccessListener { document ->

                    val role =
                        document.getString("role").orEmpty()

                    providerBookingsButton.visibility =
                        if (role.equals(
                                "provider",
                                ignoreCase = true
                            )
                        ) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                }
                .addOnFailureListener {

                    providerBookingsButton.visibility = View.GONE

                    Toast.makeText(
                        this,
                        "Unable to load account role",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        providerBookingsButton.setOnClickListener {
            startActivity(
                Intent(this, ProviderDashboardActivity::class.java)
            )
        }

        // LOGOUT
        logoutButton.setOnClickListener {

            logoutButton.isEnabled = false

            val userId = auth.currentUser?.uid

            if (userId == null) {
                finishLogout()
                return@setOnClickListener
            }

            db.collection("users")
                .document(userId)
                .update("fcmToken", "")
                .addOnSuccessListener {
                    finishLogout()
                }
                .addOnFailureListener { exception ->

                    logoutButton.isEnabled = true

                    Toast.makeText(
                        this,
                        "Unable to clear notification token. " +
                                "Please check your connection and try again.",
                        Toast.LENGTH_LONG
                    ).show()

                    Log.e(
                        "UMCIMBI_FCM",
                        "Failed to clear FCM token",
                        exception
                    )
                }
        }

        // BOTTOM NAVIGATION
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {

        // HOME
        findViewById<TextView>(R.id.navHomeButton)
            .setOnClickListener {
                openPage(HomeActivity::class.java)
            }

        // EXPLORE
        findViewById<TextView>(R.id.navExploreButton)
            .setOnClickListener {
                openPage(CategoriesActivity::class.java)
            }

        // BOOKINGS
        findViewById<TextView>(R.id.navBookingsButton)
            .setOnClickListener {
                openPage(MyBookingsActivity::class.java)
            }

        // MESSAGES
        findViewById<TextView>(R.id.navMessagesButton)
            .setOnClickListener {
                openPage(MessagesActivity::class.java)
            }

        // PROFILE - CURRENT PAGE
        findViewById<TextView>(R.id.navProfileButton)
            .apply {
                setTextColor(Color.parseColor("#111111"))
                setOnClickListener {
                    // Already on Profile
                }
            }

        // SET OTHER TABS TO GREY
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

    private fun finishLogout() {

        auth.signOut()

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
