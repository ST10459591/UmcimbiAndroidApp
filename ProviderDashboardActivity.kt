package com.example.umcimbisa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProviderDashboardActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var businessNameText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_dashboard)

        businessNameText = findViewById(R.id.businessNameText)

        // BACK BUTTON
        findViewById<TextView>(R.id.backButton)
            .setOnClickListener {
                finish()
            }

        setupBottomNavigation()

        // MY BUSINESS PROFILE
        findViewById<Button>(R.id.myBusinessButton)
            .setOnClickListener {
                startActivity(
                    Intent(
                        this,
                        ProviderBusinessActivity::class.java
                    )
                )
            }

        // BOOKING REQUESTS
        findViewById<Button>(R.id.bookingRequestsButton)
            .setOnClickListener {
                startActivity(
                    Intent(
                        this,
                        ProviderBookingsActivity::class.java
                    )
                )
            }

        // MESSAGES
        findViewById<Button>(R.id.providerMessagesButton)
            .setOnClickListener {
                startActivity(
                    Intent(
                        this,
                        MessagesActivity::class.java
                    )
                )
            }
    }

    // REFRESH BUSINESS DETAILS WHEN DASHBOARD OPENS OR RESUMES
    override fun onResume() {
        super.onResume()

        val uid = auth.currentUser?.uid

        if (uid == null) {
            businessNameText.text = getString(R.string.login)
            return
        }

        // CHECK PROVIDER ACCOUNT ROLE
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { user ->

                if (auth.currentUser?.uid != uid) {
                    return@addOnSuccessListener
                }

                if (user.getString("role") != "provider") {
                    Toast.makeText(
                        this,
                        "Provider access only",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                    return@addOnSuccessListener
                }

                loadBusiness(uid)
            }
            .addOnFailureListener {
                businessNameText.text =
                    getString(R.string.business_load_error)
            }
    }

    // LOAD SAVED BUSINESS NAME FROM FIRESTORE
    private fun loadBusiness(uid: String) {

        db.collection("providers")
            .whereEqualTo("userId", uid)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->

                if (auth.currentUser?.uid != uid) {
                    return@addOnSuccessListener
                }

                if (snapshot.isEmpty) {
                    businessNameText.text =
                        getString(R.string.no_business_profile)
                } else {
                    businessNameText.text =
                        snapshot.documents.first()
                            .getString("businessName")
                            ?: getString(R.string.service_provider)
                }
            }
            .addOnFailureListener {
                businessNameText.text =
                    getString(R.string.business_load_error)
            }
    }

    // BOTTOM NAVIGATION
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
                openPage(ProviderBookingsActivity::class.java)
            }

        findViewById<TextView>(R.id.navMessagesButton)
            .setOnClickListener {
                openPage(MessagesActivity::class.java)
            }

        findViewById<TextView>(R.id.navProfileButton)
            .setOnClickListener {
                openPage(ProfileActivity::class.java)
            }

        listOf(
            R.id.navHomeButton,
            R.id.navExploreButton,
            R.id.navBookingsButton,
            R.id.navMessagesButton,
            R.id.navProfileButton
        ).forEach { id ->
            findViewById<TextView>(id)
                .setTextColor(Color.parseColor("#666666"))
        }
    }

    private fun openPage(
        destination: Class<out android.app.Activity>
    ) {

        val intent = Intent(this, destination)

        intent.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        )

        startActivity(intent)
    }
}
