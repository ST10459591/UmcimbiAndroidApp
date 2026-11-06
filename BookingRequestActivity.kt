package com.example.umcimbisa

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

class BookingRequestActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var providerNameText: TextView
    private lateinit var eventTypeInput: EditText
    private lateinit var eventDateInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var guestsInput: EditText
    private lateinit var budgetInput: EditText
    private lateinit var submitButton: Button

    private var providerId = ""
    private var providerUid = ""
    private var providerName = ""
    private var category = ""
    private var selectedDate = ""
    private var providerAvailable = false
    private var submitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_request)

        providerNameText = findViewById(R.id.providerNameText)
        eventTypeInput = findViewById(R.id.eventTypeInput)
        eventDateInput = findViewById(R.id.eventDateInput)
        locationInput = findViewById(R.id.locationInput)
        guestsInput = findViewById(R.id.guestsInput)
        budgetInput = findViewById(R.id.budgetInput)
        submitButton = findViewById(R.id.submitBookingButton)

        findViewById<TextView>(R.id.backButton)
            .setOnClickListener { finish() }

        submitButton.isEnabled = false

        providerId = intent.getStringExtra("providerId").orEmpty()

        if (providerId.isBlank()) {
            showToast(getString(R.string.booking_provider_missing))
            finish()
            return
        }

        if (auth.currentUser == null) {
            showToast(getString(R.string.booking_login_required))
            finish()
            return
        }

        eventDateInput.setOnClickListener {
            showDatePicker()
        }

        submitButton.setOnClickListener {
            submitBooking()
        }

        loadProvider()
    }

    private fun loadProvider() {

        db.collection("providers")
            .document(providerId)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    providerAvailable = false
                    submitButton.isEnabled = false
                    showToast(getString(R.string.booking_provider_missing))
                    return@addOnSuccessListener
                }

                providerName =
                    document.getString("businessName") ?: "Provider"

                providerUid =
                    document.getString("userId").orEmpty()

                category =
                    document.getString("category").orEmpty()

                val availability =
                    document.getString("availability").orEmpty()

                providerAvailable =
                    availability.equals("Available", ignoreCase = true)

                providerNameText.text = providerName

                submitButton.isEnabled =
                    providerAvailable && providerUid.isNotBlank()

                if (!providerAvailable) {
                    showToast(getString(R.string.provider_unavailable))
                } else if (providerUid.isBlank()) {
                    showToast(getString(R.string.provider_cannot_book))
                }
            }
            .addOnFailureListener {
                providerAvailable = false
                submitButton.isEnabled = false
                showToast(getString(R.string.booking_provider_load_error))
            }
    }

    private fun showDatePicker() {

        val calendar = Calendar.getInstance()

        val picker = DatePickerDialog(
            this,
            { _, year, month, day ->

                selectedDate = String.format(
                    Locale.US,
                    "%04d-%02d-%02d",
                    year,
                    month + 1,
                    day
                )

                eventDateInput.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        picker.datePicker.minDate =
            System.currentTimeMillis() - 1000

        picker.show()
    }

    private fun submitBooking() {

        if (submitting) return

        val customerUid = auth.currentUser?.uid ?: return

        val eventType = eventTypeInput.text.toString().trim()
        val location = locationInput.text.toString().trim()
        val guests = guestsInput.text.toString().toIntOrNull()
        val budget = budgetInput.text.toString().toDoubleOrNull()

        if (eventType.isBlank()) {
            eventTypeInput.error = getString(R.string.enter_event_type)
            return
        }

        if (selectedDate.isBlank()) {
            eventDateInput.error = getString(R.string.select_event_date)
            return
        }

        if (location.isBlank()) {
            locationInput.error = getString(R.string.enter_event_location)
            return
        }

        if (guests == null || guests <= 0) {
            guestsInput.error = getString(R.string.invalid_guest_count)
            return
        }

        if (budget == null || !budget.isFinite() || budget <= 0) {
            budgetInput.error = getString(R.string.invalid_budget)
            return
        }

        if (providerUid == customerUid) {
            showToast(getString(R.string.cannot_book_self))
            return
        }

        submitting = true
        submitButton.isEnabled = false

        // CHECK THE PROVIDER AGAIN BEFORE SUBMITTING
        db.collection("providers")
            .document(providerId)
            .get()
            .addOnSuccessListener { provider ->

                if (!provider.exists()) {
                    submitting = false
                    providerAvailable = false
                    showToast(getString(R.string.booking_provider_missing))
                    return@addOnSuccessListener
                }

                val latestAvailability =
                    provider.getString("availability").orEmpty()

                val latestProviderUid =
                    provider.getString("userId").orEmpty()

                providerAvailable =
                    latestAvailability.equals(
                        "Available",
                        ignoreCase = true
                    )

                if (!providerAvailable) {
                    submitting = false
                    showToast(getString(R.string.provider_unavailable))
                    return@addOnSuccessListener
                }

                if (latestProviderUid.isBlank() ||
                    latestProviderUid == customerUid
                ) {
                    submitting = false
                    showToast(getString(R.string.provider_cannot_book))
                    return@addOnSuccessListener
                }

                providerUid = latestProviderUid
                providerName =
                    provider.getString("businessName") ?: providerName
                category =
                    provider.getString("category") ?: category

                val booking = hashMapOf(
                    "customerId" to customerUid,
                    "providerId" to providerId,
                    "providerUid" to providerUid,
                    "providerName" to providerName,
                    "service" to category,
                    "eventType" to eventType,
                    "date" to selectedDate,
                    "location" to location,
                    "guests" to guests,
                    "budget" to budget,
                    "status" to "pending",
                    "createdAt" to FieldValue.serverTimestamp()
                )

                db.collection("eventRequests")
                    .add(booking)
                    .addOnSuccessListener {
                        showToast(getString(R.string.booking_submitted))
                        finish()
                    }
                    .addOnFailureListener { error ->
                        submitting = false
                        submitButton.isEnabled = providerAvailable
                        showToast(
                            "${getString(R.string.booking_failed)}: " +
                                    error.localizedMessage
                        )
                    }
            }
            .addOnFailureListener {
                submitting = false
                submitButton.isEnabled = false
                showToast(getString(R.string.booking_provider_load_error))
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
