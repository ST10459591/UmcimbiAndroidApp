package com.example.umcimbisa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ProviderDetailActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_detail)

        findViewById<TextView>(R.id.backButton)
            .setOnClickListener { finish() }

        val providerId = intent.getStringExtra("providerId")

        val businessNameText =
            findViewById<TextView>(R.id.businessNameText)

        val categoryText =
            findViewById<TextView>(R.id.categoryText)

        val descriptionText =
            findViewById<TextView>(R.id.descriptionText)

        val locationText =
            findViewById<TextView>(R.id.locationText)

        val priceText =
            findViewById<TextView>(R.id.priceText)

        val ratingText =
            findViewById<TextView>(R.id.ratingText)

        val availabilityText =
            findViewById<TextView>(R.id.availabilityText)

        val phoneText =
            findViewById<TextView>(R.id.phoneText)

        val emailText =
            findViewById<TextView>(R.id.emailText)

        val photosContainer =
            findViewById<LinearLayout>(R.id.providerPhotosContainer)

        val noPhotosText =
            findViewById<TextView>(R.id.noProviderPhotosText)

        val contactButton =
            findViewById<Button>(R.id.contactButton)

        val messageButton =
            findViewById<Button>(R.id.messageButton)

        val bookProviderButton =
            findViewById<Button>(R.id.bookProviderButton)

        val favouriteButton =
            findViewById<Button>(R.id.favouriteButton)

        if (providerId.isNullOrBlank()) {
            businessNameText.text = "Provider not found"
            contactButton.isEnabled = false
            messageButton.isEnabled = false
            bookProviderButton.isEnabled = false
            favouriteButton.isEnabled = false
            return
        }

        // BOOK PROVIDER
        bookProviderButton.setOnClickListener {

            val bookingIntent = Intent(
                this,
                BookingRequestActivity::class.java
            )

            bookingIntent.putExtra("providerId", providerId)
            startActivity(bookingIntent)
        }

        // FAVOURITES
        val currentUid =
            FirebaseAuth.getInstance().currentUser?.uid

        favouriteButton.isEnabled = currentUid != null

        if (currentUid != null) {

            val favouriteRef = db.collection("users")
                .document(currentUid)
                .collection("favourites")
                .document(providerId)

            favouriteRef.addSnapshotListener(this) { document, error ->

                if (error == null) {
                    favouriteButton.text =
                        if (document != null && document.exists()) {
                            "♥ Remove from Favourites"
                        } else {
                            "♡ Add to Favourites"
                        }
                }
            }

            favouriteButton.setOnClickListener {

                favouriteButton.isEnabled = false

                favouriteRef.get()
                    .addOnSuccessListener { document ->

                        if (document.exists()) {

                            favouriteRef.delete()
                                .addOnSuccessListener {
                                    favouriteButton.isEnabled = true
                                    showMessage("Removed from favourites")
                                }
                                .addOnFailureListener { e ->
                                    favouriteButton.isEnabled = true
                                    showMessage(
                                        e.message ?: "Unable to remove favourite"
                                    )
                                }

                        } else {

                            val favourite = hashMapOf(
                                "providerId" to providerId,
                                "createdAt" to FieldValue.serverTimestamp()
                            )

                            favouriteRef.set(favourite)
                                .addOnSuccessListener {
                                    favouriteButton.isEnabled = true
                                    showMessage("Added to favourites")
                                }
                                .addOnFailureListener { e ->
                                    favouriteButton.isEnabled = true
                                    showMessage(
                                        e.message ?: "Unable to add favourite"
                                    )
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        favouriteButton.isEnabled = true
                        showMessage(
                            e.message ?: "Unable to load favourite"
                        )
                    }
            }
        }

        // LOAD PROVIDER DETAILS
        messageButton.isEnabled = false
        contactButton.isEnabled = false

        db.collection("providers")
            .document(providerId)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    businessNameText.text = "Provider not found"
                    bookProviderButton.isEnabled = false
                    messageButton.isEnabled = false
                    favouriteButton.isEnabled = false
                    return@addOnSuccessListener
                }

                businessNameText.text =
                    document.getString("businessName")
                        ?: "Unnamed Provider"

                categoryText.text =
                    "Category: ${
                        document.getString("category")
                            ?: "Not specified"
                    }"

                descriptionText.text =
                    document.getString("description")
                        ?: "No description available"

                locationText.text =
                    "Location: ${
                        document.getString("location")
                            ?: "Not specified"
                    }"

                val price = document.getDouble("price")

                priceText.text = if (price != null) {
                    "Price: R${"%,.2f".format(price)}"
                } else {
                    "Price: Contact for a quote"
                }

                val rating = document.getDouble("rating")

                ratingText.text = if (rating != null) {
                    "Rating: $rating / 5"
                } else {
                    "Rating: Not available"
                }

                availabilityText.text =
                    "Availability: ${
                        document.getString("availability")
                            ?: "Not specified"
                    }"

                // LOAD BUSINESS PHOTOS
                val imageUrls = (document.get("imageUrls") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.take(5)
                    .orEmpty()

                photosContainer.removeAllViews()

                noPhotosText.visibility =
                    if (imageUrls.isEmpty()) View.VISIBLE else View.GONE

                for (url in imageUrls) {

                    val imageView = ImageView(this).apply {

                        layoutParams = LinearLayout.LayoutParams(
                            dp(180),
                            dp(150)
                        ).apply {
                            marginEnd = dp(10)
                        }

                        scaleType = ImageView.ScaleType.CENTER_CROP
                        contentDescription = "Business photo"
                    }

                    Glide.with(this)
                        .load(url)
                        .into(imageView)

                    photosContainer.addView(imageView)
                }

                // CONTACT INFORMATION
                val phone =
                    document.getString("phone").orEmpty()

                val email =
                    document.getString("email").orEmpty()

                phoneText.text =
                    "Phone: ${phone.ifBlank { "Not available" }}"

                emailText.text =
                    "Email: ${email.ifBlank { "Not available" }}"

                contactButton.isEnabled =
                    phone.isNotBlank() || email.isNotBlank()

                contactButton.setOnClickListener {

                    val contactIntent = when {

                        phone.isNotBlank() -> Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse("tel:${Uri.encode(phone)}")
                        )

                        email.isNotBlank() -> Intent(
                            Intent.ACTION_SENDTO,
                            Uri.parse("mailto:${Uri.encode(email)}")
                        )

                        else -> null
                    }

                    if (contactIntent != null) {
                        try {
                            startActivity(contactIntent)
                        } catch (e: Exception) {
                            showMessage("No suitable contact app found")
                        }
                    }
                }

                // MESSAGE PROVIDER
                val providerUid =
                    document.getString("userId").orEmpty()

                messageButton.isEnabled = true
                messageButton.text = "Message Provider"

                messageButton.setOnClickListener {

                    if (providerUid.isBlank()) {

                        showMessage(
                            "This provider does not have a linked account yet."
                        )

                    } else {

                        val chatIntent = Intent(
                            this,
                            ChatActivity::class.java
                        )

                        chatIntent.putExtra(
                            "providerId",
                            providerId
                        )

                        startActivity(chatIntent)
                    }
                }
            }
            .addOnFailureListener {

                businessNameText.text = "Unable to load provider"
                showMessage("Please check your internet connection")
            }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}
