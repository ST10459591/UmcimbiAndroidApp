package com.example.umcimbisa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges

class MyBookingsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var bookingsContainer: LinearLayout

    private var listener: ListenerRegistration? = null
    private var initialSnapshotReceived = false

    // Track booking statuses to avoid duplicate notifications.
    private val knownStatuses = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        bookingsContainer = findViewById(R.id.bookingsContainer)

        findViewById<TextView>(R.id.backButton)
            .setOnClickListener { finish() }

        setupBottomNavigation()

        val uid = auth.currentUser?.uid

        if (uid == null) {
            showMessage(getString(R.string.my_bookings_login_required))
            return
        }

        listenForBookings(uid)
    }

    private fun listenForBookings(uid: String) {

        listener = db.collection("eventRequests")
            .whereEqualTo("customerId", uid)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->

                if (error != null) {
                    bookingsContainer.removeAllViews()
                    showMessage(getString(R.string.my_bookings_load_error))
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    return@addSnapshotListener
                }

                // Initialise known statuses without showing notifications.
                if (!initialSnapshotReceived) {

                    snapshot.documents.forEach { document ->
                        knownStatuses[document.id] =
                            document.getString("status") ?: "pending"
                    }

                    if (!snapshot.metadata.isFromCache) {
                        initialSnapshotReceived = true
                    }

                    displayBookings(snapshot.documents)
                    return@addSnapshotListener
                }

                // Check server-confirmed changes only.
                if (!snapshot.metadata.isFromCache) {

                    for (change in snapshot.documentChanges) {

                        val document = change.document
                        val bookingId = document.id
                        val newStatus =
                            document.getString("status") ?: "pending"

                        when (change.type) {

                            DocumentChange.Type.MODIFIED -> {

                                val previousStatus =
                                    knownStatuses[bookingId]

                                if (
                                    previousStatus != null &&
                                    previousStatus != newStatus
                                ) {
                                    showStatusNotification(
                                        document,
                                        newStatus
                                    )
                                }

                                knownStatuses[bookingId] = newStatus
                            }

                            DocumentChange.Type.ADDED -> {
                                // New bookings should not trigger
                                // accepted/rejected notifications.
                                knownStatuses[bookingId] = newStatus
                            }

                            DocumentChange.Type.REMOVED -> {
                                knownStatuses.remove(bookingId)
                            }
                        }
                    }
                }

                displayBookings(snapshot.documents)
            }
    }

    private fun showStatusNotification(
        document: DocumentSnapshot,
        status: String
    ) {

        val providerName =
            document.getString("providerName") ?: "Provider"

        val message = when (status) {

            "accepted" -> getString(
                R.string.booking_accepted_notification,
                providerName
            )

            "rejected" -> getString(
                R.string.booking_rejected_notification,
                providerName
            )

            else -> return
        }

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun displayBookings(
        documents: List<DocumentSnapshot>
    ) {

        bookingsContainer.removeAllViews()

        if (documents.isEmpty()) {
            showMessage(getString(R.string.no_customer_bookings))
            return
        }

        // Newest bookings first.
        val bookings = documents.sortedByDescending {
            it.getTimestamp("createdAt")?.seconds ?: 0L
        }

        for (document in bookings) {

            val provider =
                document.getString("providerName") ?: "Provider"

            val eventType =
                document.getString("eventType") ?: "Event"

            val date =
                document.getString("date") ?: "Not specified"

            val status =
                document.getString("status") ?: "pending"

            val card = TextView(this).apply {

                text = getString(
                    R.string.customer_booking_details,
                    provider,
                    eventType,
                    date,
                    status.uppercase()
                )

                textSize = 16f
                setTextColor(Color.BLACK)
                setBackgroundColor(Color.WHITE)
                setPadding(24, 24, 24, 24)
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }

            bookingsContainer.addView(card, params)
        }
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
            .apply {
                setTextColor(Color.parseColor("#111111"))
                setOnClickListener {
                    // Already on My Bookings.
                }
            }

        findViewById<TextView>(R.id.navMessagesButton)
            .setOnClickListener {
                openPage(MessagesActivity::class.java)
            }

        findViewById<TextView>(R.id.navProfileButton)
            .setOnClickListener {
                openPage(ProfileActivity::class.java)
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

    private fun showMessage(message: String) {

        val textView = TextView(this).apply {
            text = message
            textSize = 15f
            setTextColor(Color.DKGRAY)
            setPadding(16, 24, 16, 24)
        }

        bookingsContainer.addView(textView)
    }

    override fun onDestroy() {
        listener?.remove()
        super.onDestroy()
    }
}
