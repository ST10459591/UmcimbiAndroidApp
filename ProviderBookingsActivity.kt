package com.example.umcimbisa

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges

class ProviderBookingsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var requestsContainer: LinearLayout

    private var listener: ListenerRegistration? = null
    private var initialSnapshotReceived = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_bookings)

        requestsContainer = findViewById(R.id.requestsContainer)

        findViewById<TextView>(R.id.backButton)
            .setOnClickListener { finish() }

        val uid = auth.currentUser?.uid

        if (uid == null) {
            showMessage(getString(R.string.booking_login_required))
            return
        }

        listenForBookings(uid)
    }

    private fun listenForBookings(uid: String) {

        listener = db.collection("eventRequests")
            .whereEqualTo("providerUid", uid)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->

                if (error != null) {
                    requestsContainer.removeAllViews()
                    showMessage(
                        getString(R.string.provider_requests_error)
                    )
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    return@addSnapshotListener
                }

                // Do not notify for existing bookings on initial load.
                // Wait for the first server-confirmed snapshot.
                if (!initialSnapshotReceived) {

                    if (snapshot.metadata.isFromCache) {
                        displayBookings(snapshot.documents)
                        return@addSnapshotListener
                    }

                    initialSnapshotReceived = true
                    displayBookings(snapshot.documents)
                    return@addSnapshotListener
                }

                // Notify only for new bookings received from Firestore.
                if (!snapshot.metadata.isFromCache) {

                    for (change in snapshot.documentChanges) {

                        if (change.type == DocumentChange.Type.ADDED) {

                            val status =
                                change.document.getString("status")

                            if (status == "pending") {
                                Toast.makeText(
                                    this,
                                    getString(R.string.new_booking_received),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }

                displayBookings(snapshot.documents)
            }
    }

    private fun displayBookings(
        documents: List<com.google.firebase.firestore.DocumentSnapshot>
    ) {

        requestsContainer.removeAllViews()

        if (documents.isEmpty()) {
            showMessage(getString(R.string.no_provider_requests))
            return
        }

        for (document in documents) {

            val status =
                document.getString("status") ?: "pending"

            val eventType =
                document.getString("eventType") ?: "Event"

            val date =
                document.getString("date") ?: "Not specified"

            val location =
                document.getString("location") ?: "Not specified"

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
                setBackgroundColor(Color.WHITE)
            }

            val details = TextView(this).apply {
                text = getString(
                    R.string.provider_booking_details,
                    eventType,
                    date,
                    location,
                    status.uppercase()
                )

                textSize = 16f
                setTextColor(Color.BLACK)
            }

            card.addView(details)

            if (status == "pending") {

                val acceptButton = Button(this).apply {
                    text = getString(R.string.accept_booking)
                    setOnClickListener {
                        updateStatus(document.id, "accepted")
                    }
                }

                val rejectButton = Button(this).apply {
                    text = getString(R.string.reject_booking)
                    setOnClickListener {
                        updateStatus(document.id, "rejected")
                    }
                }

                card.addView(acceptButton)
                card.addView(rejectButton)
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }

            requestsContainer.addView(card, params)
        }
    }

    private fun updateStatus(
        bookingId: String,
        newStatus: String
    ) {

        db.collection("eventRequests")
            .document(bookingId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    getString(R.string.booking_status_updated, newStatus),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "${getString(R.string.booking_update_failed)}: " +
                            error.localizedMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showMessage(message: String) {

        val textView = TextView(this).apply {
            text = message
            textSize = 15f
            setTextColor(Color.DKGRAY)
            setPadding(16, 24, 16, 24)
        }

        requestsContainer.addView(textView)
    }

    override fun onDestroy() {
        listener?.remove()
        super.onDestroy()
    }
}
