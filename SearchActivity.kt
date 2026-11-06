
package com.example.umcimbisa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import java.util.Locale

class SearchActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val backButton = findViewById<TextView>(R.id.backButton)
        val categoryInput = findViewById<EditText>(R.id.categoryInput)
        val searchButton = findViewById<Button>(R.id.searchButton)
        val results = findViewById<TextView>(R.id.resultsText)

        backButton.setOnClickListener {
            finish()
        }

        val selectedCategory = intent.getStringExtra("category")

        if (!selectedCategory.isNullOrEmpty()) {
            categoryInput.setText(selectedCategory)
        }

        fun searchServices() {

            val searchTerm = categoryInput.text.toString().trim()

            if (searchTerm.isEmpty()) {
                categoryInput.error = "Enter a service or business name"
                categoryInput.requestFocus()
                return
            }

            searchButton.isEnabled = false
            results.text = "Searching for providers..."

            // Get actual registered businesses from Firebase
            db.collection("providers")
                .get(Source.SERVER)
                .addOnSuccessListener { documents ->

                    val matchingProviders = documents.filter { document ->

                        val businessName =
                            document.getString("businessName").orEmpty()

                        val category =
                            document.getString("category").orEmpty()

                        val location =
                            document.getString("location").orEmpty()

                        businessName.contains(searchTerm, ignoreCase = true) ||
                                category.contains(searchTerm, ignoreCase = true) ||
                                location.contains(searchTerm, ignoreCase = true)
                    }.sortedBy {
                        it.getString("businessName")
                            .orEmpty()
                            .lowercase(Locale.ROOT)
                    }

                    if (matchingProviders.isEmpty()) {
                        results.text =
                            "No service providers found for \"$searchTerm\"."
                    } else {

                        val builder = StringBuilder()

                        builder.append(
                            "Results for: $searchTerm\n\n"
                        )

                        matchingProviders.forEachIndexed { index, document ->

                            val businessName =
                                document.getString("businessName")
                                    ?: "Unnamed Provider"

                            val category =
                                document.getString("category")
                                    ?: "Not specified"

                            val location =
                                document.getString("location")
                                    ?: "Not specified"

                            val availability =
                                document.getString("availability")
                                    ?: "Not specified"

                            builder.append("${index + 1}. $businessName\n")
                            builder.append("Service: $category\n")
                            builder.append("Location: $location\n")
                            builder.append("Availability: $availability\n")
                            builder.append("\n--------------------\n\n")
                        }

                        results.text = builder.toString()
                    }

                    searchButton.isEnabled = true
                }
                .addOnFailureListener { exception ->

                    results.text =
                        "Unable to load providers from Firebase. " +
                                "Please check your internet connection and try again."

                    searchButton.isEnabled = true
                }
        }

        searchButton.setOnClickListener {
            searchServices()
        }

        // Automatically search if a category was passed
        if (!selectedCategory.isNullOrEmpty()) {
            searchServices()
        }
    }
}