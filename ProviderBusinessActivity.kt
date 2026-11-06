package com.example.umcimbisa

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class ProviderBusinessActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var businessNameInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var descriptionInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var priceInput: EditText
    private lateinit var availabilitySpinner: Spinner
    private lateinit var phoneInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var saveButton: Button
    private lateinit var statusText: TextView

    private lateinit var photosContainer: LinearLayout
    private lateinit var noPhotosText: TextView
    private lateinit var addPhotosButton: Button
    private lateinit var uploadStatusText: TextView

    private var providerId = ""
    private var ownerUid = ""
    private var businessExists = false
    private var uploadInProgress = false

    private val imageUrls = mutableListOf<String>()

    private val categories = listOf(
        "Catering",
        "Venues",
        "Photography",
        "Decor",
        "Music & Entertainment",
        "Transport"
    )

    private val availabilityOptions = listOf(
        "Available",
        "Unavailable"
    )

    private val photoPicker =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                uploadBusinessPhoto(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_business)

        val cloudinaryConfig = hashMapOf(
            "cloud_name" to "rojs4dcw"
        )

        try {
            MediaManager.get()
        } catch (e: IllegalStateException) {
            MediaManager.init(applicationContext, cloudinaryConfig)
        }

        businessNameInput = findViewById(R.id.businessNameInput)
        categorySpinner = findViewById(R.id.categorySpinner)
        descriptionInput = findViewById(R.id.descriptionInput)
        locationInput = findViewById(R.id.locationInput)
        priceInput = findViewById(R.id.priceInput)
        availabilitySpinner = findViewById(R.id.availabilitySpinner)
        phoneInput = findViewById(R.id.phoneInput)
        emailInput = findViewById(R.id.emailInput)
        saveButton = findViewById(R.id.saveBusinessButton)
        statusText = findViewById(R.id.businessStatusText)

        photosContainer = findViewById(R.id.businessPhotosContainer)
        noPhotosText = findViewById(R.id.noBusinessPhotosText)
        addPhotosButton = findViewById(R.id.addBusinessPhotosButton)
        uploadStatusText = findViewById(R.id.photoUploadStatusText)

        saveButton.isEnabled = false
        addPhotosButton.isEnabled = false

        findViewById<TextView>(R.id.backButton)
            .setOnClickListener { finish() }

        setupSpinners()
        setupBottomNavigation()

        saveButton.setOnClickListener {
            saveBusiness()
        }

        addPhotosButton.setOnClickListener {
            if (!businessExists) {
                showMessage("Save your business details first")
            } else if (uploadInProgress) {
                showMessage("Please wait for the upload")
            } else if (imageUrls.size >= 5) {
                showMessage("Maximum of 5 photos allowed")
            } else {
                photoPicker.launch("image/*")
            }
        }

        val uid = auth.currentUser?.uid

        if (uid == null) {
            statusText.text = "Please log in first"
            return
        }

        ownerUid = uid

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { user ->

                if (auth.currentUser?.uid != uid) {
                    return@addOnSuccessListener
                }

                if (user.getString("role") != "provider") {
                    statusText.text = "Provider access only"
                    return@addOnSuccessListener
                }

                loadBusiness()
            }
            .addOnFailureListener { error ->
                statusText.text =
                    "Unable to verify account: ${error.message}"
            }
    }

    private fun setupSpinners() {

        categorySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        availabilitySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            availabilityOptions
        )
    }

    private fun loadBusiness() {

        db.collection("providers")
            .whereEqualTo("userId", ownerUid)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->

                if (auth.currentUser?.uid != ownerUid) {
                    return@addOnSuccessListener
                }

                if (snapshot.isEmpty) {

                    providerId = ownerUid
                    businessExists = false

                    emailInput.setText(
                        auth.currentUser?.email.orEmpty()
                    )

                    statusText.text =
                        "No business profile yet. Enter your details."

                    saveButton.isEnabled = true
                    addPhotosButton.isEnabled = false
                    return@addOnSuccessListener
                }

                val document = snapshot.documents.first()

                if (document.getString("userId") != ownerUid) {
                    statusText.text = "Provider access only"
                    return@addOnSuccessListener
                }

                providerId = document.id
                businessExists = true

                businessNameInput.setText(
                    document.getString("businessName").orEmpty()
                )

                descriptionInput.setText(
                    document.getString("description").orEmpty()
                )

                locationInput.setText(
                    document.getString("location").orEmpty()
                )

                priceInput.setText(
                    document.getDouble("price")?.toString().orEmpty()
                )

                phoneInput.setText(
                    document.getString("phone").orEmpty()
                )

                emailInput.setText(
                    document.getString("email").orEmpty()
                )

                val categoryIndex = categories.indexOf(
                    document.getString("category").orEmpty()
                )

                if (categoryIndex >= 0) {
                    categorySpinner.setSelection(categoryIndex)
                }

                val availabilityIndex =
                    availabilityOptions.indexOf(
                        document.getString("availability").orEmpty()
                    )

                if (availabilityIndex >= 0) {
                    availabilitySpinner.setSelection(availabilityIndex)
                }

                imageUrls.clear()
                imageUrls.addAll(
                    document.get("imageUrls")
                        .let { value ->
                            (value as? List<*>)
                                ?.filterIsInstance<String>()
                                .orEmpty()
                        }
                )

                displayPhotos()

                statusText.text = "Business profile ready"
                saveButton.isEnabled = true
                updatePhotoButton()
            }
            .addOnFailureListener { error ->
                statusText.text =
                    "Unable to load business: ${error.message}"
                saveButton.isEnabled = false
                addPhotosButton.isEnabled = false
            }
    }

    private fun updatePhotoButton() {
        addPhotosButton.isEnabled =
            businessExists &&
                    !uploadInProgress &&
                    imageUrls.size < 5
    }

    private fun displayPhotos() {

        photosContainer.removeAllViews()

        noPhotosText.visibility =
            if (imageUrls.isEmpty()) View.VISIBLE else View.GONE

        for (url in imageUrls) {

            val photoLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(4, 4, 12, 4)
            }

            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dp(120),
                    dp(110)
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            Glide.with(this)
                .load(url)
                .into(imageView)

            val removeButton = Button(this).apply {
                text = "Remove"
                textSize = 12f
                setOnClickListener {
                    removeBusinessPhoto(url)
                }
            }

            photoLayout.addView(imageView)
            photoLayout.addView(removeButton)
            photosContainer.addView(photoLayout)
        }

        updatePhotoButton()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun uploadBusinessPhoto(imageUri: Uri) {

        val uid = auth.currentUser?.uid

        if (
            uid == null ||
            uid != ownerUid ||
            providerId.isBlank() ||
            !businessExists ||
            uploadInProgress ||
            imageUrls.size >= 5
        ) {
            showMessage("Unable to upload this photo")
            return
        }

        uploadInProgress = true
        updatePhotoButton()
        uploadStatusText.text = "Uploading photo..."

        MediaManager.get()
            .upload(imageUri)
            .unsigned("umcimbi_business_photos")
            .callback(object : UploadCallback {

                override fun onStart(requestId: String) {
                    runOnUiThread {
                        uploadStatusText.text = "Uploading photo..."
                    }
                }

                override fun onProgress(
                    requestId: String,
                    bytes: Long,
                    totalBytes: Long
                ) {
                    if (totalBytes > 0) {
                        val percent =
                            (bytes * 100 / totalBytes).toInt()

                        runOnUiThread {
                            uploadStatusText.text =
                                "Uploading photo... $percent%"
                        }
                    }
                }

                override fun onSuccess(
                    requestId: String,
                    resultData: Map<*, *>
                ) {
                    runOnUiThread {

                        val imageUrl =
                            resultData["secure_url"] as? String

                        if (imageUrl.isNullOrBlank()) {
                            finishUpload("No image URL returned")
                            return@runOnUiThread
                        }

                        db.collection("providers")
                            .document(providerId)
                            .update(
                                "imageUrls",
                                FieldValue.arrayUnion(imageUrl)
                            )
                            .addOnSuccessListener {
                                if (!imageUrls.contains(imageUrl)) {
                                    imageUrls.add(imageUrl)
                                }

                                displayPhotos()
                                finishUpload("Photo uploaded successfully!")
                            }
                            .addOnFailureListener { error ->
                                finishUpload(
                                    "Photo uploaded but not saved: ${error.message}"
                                )
                            }
                    }
                }

                override fun onError(
                    requestId: String,
                    error: ErrorInfo
                ) {
                    runOnUiThread {
                        finishUpload(
                            "Upload failed: ${error.description}"
                        )
                    }
                }

                override fun onReschedule(
                    requestId: String,
                    error: ErrorInfo
                ) {
                    runOnUiThread {
                        uploadStatusText.text = "Upload rescheduled..."
                    }
                }
            })
            .dispatch()
    }

    private fun finishUpload(message: String) {
        uploadInProgress = false
        uploadStatusText.text = message
        updatePhotoButton()
        showMessage(message)
    }

    private fun removeBusinessPhoto(url: String) {

        val uid = auth.currentUser?.uid

        if (
            uid == null ||
            uid != ownerUid ||
            !businessExists ||
            uploadInProgress
        ) {
            showMessage("Unable to remove photo")
            return
        }

        db.collection("providers")
            .document(providerId)
            .update(
                "imageUrls",
                FieldValue.arrayRemove(url)
            )
            .addOnSuccessListener {
                imageUrls.remove(url)
                displayPhotos()
                showMessage("Photo removed successfully")
            }
            .addOnFailureListener { error ->
                showMessage("Unable to remove photo: ${error.message}")
            }
    }

    private fun saveBusiness() {

        val uid = auth.currentUser?.uid

        if (
            uid == null ||
            uid != ownerUid ||
            providerId.isBlank()
        ) {
            showMessage("Provider access only")
            return
        }

        val businessName =
            businessNameInput.text.toString().trim()

        val description =
            descriptionInput.text.toString().trim()

        val location =
            locationInput.text.toString().trim()

        val price =
            priceInput.text.toString().trim().toDoubleOrNull()

        val phone =
            phoneInput.text.toString().trim()

        val email =
            emailInput.text.toString().trim()

        if (businessName.isBlank() || businessName.length > 120) {
            businessNameInput.error = "Enter a valid business name"
            businessNameInput.requestFocus()
            return
        }

        if (description.length > 2000) {
            descriptionInput.error = "Description is too long"
            descriptionInput.requestFocus()
            return
        }

        if (location.isBlank() || location.length > 200) {
            locationInput.error = "Enter a valid location"
            locationInput.requestFocus()
            return
        }

        if (price == null || !price.isFinite() || price < 0) {
            priceInput.error = "Enter a valid price"
            priceInput.requestFocus()
            return
        }

        if (phone.length > 30) {
            phoneInput.error = "Phone number is too long"
            phoneInput.requestFocus()
            return
        }

        if (
            email.length > 254 ||
            (email.isNotBlank() &&
                    !Patterns.EMAIL_ADDRESS.matcher(email).matches())
        ) {
            emailInput.error = "Enter a valid email address"
            emailInput.requestFocus()
            return
        }

        val businessData = mapOf(
            "userId" to uid,
            "businessName" to businessName,
            "category" to categories[
                categorySpinner.selectedItemPosition
            ],
            "description" to description,
            "location" to location,
            "price" to price,
            "availability" to availabilityOptions[
                availabilitySpinner.selectedItemPosition
            ],
            "phone" to phone,
            "email" to email
        )

        saveButton.isEnabled = false
        statusText.text = "Saving business details..."

        val providerRef = db.collection("providers")
            .document(providerId)

        providerRef.get()
            .addOnSuccessListener { document ->

                if (auth.currentUser?.uid != uid) {
                    saveButton.isEnabled = true
                    return@addOnSuccessListener
                }

                if (document.exists()) {

                    if (document.getString("userId") != uid) {
                        saveButton.isEnabled = true
                        statusText.text = "Provider access only"
                        showMessage("You cannot edit this business")
                        return@addOnSuccessListener
                    }

                    providerRef.update(
                        businessData.filterKeys {
                            it != "userId"
                        }
                    )
                        .addOnSuccessListener {
                            onBusinessSaved()
                        }
                        .addOnFailureListener { error ->
                            onSaveFailed(error.message)
                        }

                } else {

                    if (providerId != uid) {
                        saveButton.isEnabled = true
                        statusText.text = "Invalid business profile ID"
                        return@addOnSuccessListener
                    }

                    providerRef.set(businessData)
                        .addOnSuccessListener {
                            businessExists = true
                            updatePhotoButton()
                            onBusinessSaved()
                        }
                        .addOnFailureListener { error ->
                            onSaveFailed(error.message)
                        }
                }
            }
            .addOnFailureListener { error ->
                onSaveFailed(error.message)
            }
    }

    private fun onBusinessSaved() {
        saveButton.isEnabled = true
        statusText.text = "Business details saved successfully!"
        showMessage("Business details saved successfully!")
    }

    private fun onSaveFailed(message: String?) {
        saveButton.isEnabled = true
        statusText.text = "Unable to save business details"
        showMessage(message ?: "Please try again")
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
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
