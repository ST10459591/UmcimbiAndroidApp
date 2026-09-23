package com.example.umcimbisa

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.umcimbisa.api.RetrofitClient
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_search
        )

        val backButton =
            findViewById<TextView>(
                R.id.backButton
            )

        val categoryInput =
            findViewById<EditText>(
                R.id.categoryInput
            )

        val searchButton =
            findViewById<Button>(
                R.id.searchButton
            )

        val results =
            findViewById<TextView>(
                R.id.resultsText
            )

        val selectedCategory =
            intent.getStringExtra("category")

        if (!selectedCategory.isNullOrEmpty()) {

            categoryInput.setText(
                selectedCategory
            )
        }

        backButton.setOnClickListener {
            finish()
        }

        searchButton.setOnClickListener {

            val category =
                categoryInput.text
                    .toString()
                    .trim()

            if (category.isEmpty()) {

                categoryInput.error =
                    "Enter a category"

                categoryInput.requestFocus()

                return@setOnClickListener
            }

            searchButton.isEnabled = false

            results.text =
                "Connecting to REST API..."

            lifecycleScope.launch {

                try {

                    val providers =
                        RetrofitClient
                            .apiService
                            .getUsers()

                    val builder =
                        StringBuilder()

                    builder.append(
                        "Results for "
                    )

                    builder.append(category)

                    builder.append("\n\n")

                    providers.forEach { provider ->

                        builder.append(
                            provider.name
                        )

                        builder.append("\n")

                        builder.append(
                            provider.email
                        )

                        builder.append("\n")

                        builder.append(
                            provider.phone
                        )

                        builder.append("\n\n")
                    }

                    results.text =
                        builder.toString()

                } catch (e: Exception) {

                    results.text =
                        "Could not connect to the REST API."

                    Toast.makeText(
                        this@SearchActivity,
                        "Network error",
                        Toast.LENGTH_LONG
                    ).show()

                } finally {

                    searchButton.isEnabled = true
                }
            }
        }
    }
}