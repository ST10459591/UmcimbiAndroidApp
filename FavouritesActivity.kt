package com.example.umcimbisa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FavouritesActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_favourites)

        // FIND VIEWS
        container = findViewById(R.id.favouritesContainer)

        // BACK BUTTON
        findViewById<TextView>(R.id.backButton)
            .setOnClickListener {
                finish()
            }

        // SET UP BOTTOM NAVIGATION
        setupBottomNavigation()

        // CHECK LOGGED-IN USER
        val uid = FirebaseAuth.getInstance()
            .currentUser?.uid

        if (uid == null) {
            showMessage("Please log in first")
            return
        }

        // LOAD SAVED FAVOURITES FROM FIREBASE
        listener = db.collection("users")
            .document(uid)
            .collection("favourites")
            .addSnapshotListener { snapshot, error ->

                container.removeAllViews()

                if (error != null) {
                    showMessage("Unable to load favourites")
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    showMessage("No favourites saved yet.")
                    return@addSnapshotListener
                }

                // DISPLAY EACH SAVED PROVIDER
                for (favourite in snapshot.documents) {

                    val providerId = favourite.id

                    db.collection("providers")
                        .document(providerId)
                        .get()
                        .addOnSuccessListener { provider ->

                            if (!provider.exists()) {
                                return@addOnSuccessListener
                            }

                            val name =
                                provider.getString("businessName")
                                    ?: "Service Provider"

                            val category =
                                provider.getString("category")
                                    ?: ""

                            // CREATE PROVIDER CARD
                            val card = TextView(this).apply {

                                text = "$name\n$category\nTap to view"

                                textSize = 17f

                                setTextColor(Color.BLACK)

                                setBackgroundColor(Color.WHITE)

                                setPadding(24, 24, 24, 24)

                                // OPEN PROVIDER DETAILS
                                setOnClickListener {

                                    val intent = Intent(
                                        this@FavouritesActivity,
                                        ProviderDetailActivity::class.java
                                    )

                                    intent.putExtra(
                                        "providerId",
                                        providerId
                                    )

                                    startActivity(intent)
                                }
                            }

                            // ADD PROVIDER TO LIST
                            container.addView(card)
                        }
                }
            }
    }

    // DISPLAY MESSAGES
    private fun showMessage(message: String) {

        container.addView(
            TextView(this).apply {

                text = message
                textSize = 16f

                setTextColor(Color.DKGRAY)
            }
        )
    }

    // BOTTOM NAVIGATION
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


        // PROFILE
        findViewById<TextView>(R.id.navProfileButton)
            .setOnClickListener {
                openPage(ProfileActivity::class.java)
            }

        // HIGHLIGHT PROFILE
        findViewById<TextView>(R.id.navProfileButton)
            .setTextColor(
                Color.parseColor("#111111")
            )

        // SET OTHER TABS TO GREY
        listOf(
            R.id.navHomeButton,
            R.id.navExploreButton,
            R.id.navBookingsButton,
            R.id.navMessagesButton
        ).forEach { id ->

            findViewById<TextView>(id)
                .setTextColor(
                    Color.parseColor("#666666")
                )
        }
    }

    // OPEN ANOTHER PAGE
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

    // REMOVE FIREBASE LISTENER WHEN PAGE CLOSES
    override fun onDestroy() {

        listener?.remove()

        super.onDestroy()
    }
}
