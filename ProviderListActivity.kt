package com.example.umcimbisa

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ProviderListActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var localDatabase: UmcimbiDatabase
    private lateinit var container: LinearLayout
    private lateinit var connectivityManager: ConnectivityManager

    private var category = ""
    private var hasDisplayedProviders = false
    private var isRefreshing = false
    private var isStarted = false
    private var lastNetworkAvailable = false

    private val networkCallback =
        object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                runOnUiThread {
                    if (isStarted && !lastNetworkAvailable) {
                        lastNetworkAvailable = true
                        loadFirebaseProviders()
                    }
                }
            }

            override fun onLost(network: Network) {
                runOnUiThread {
                    lastNetworkAvailable = isInternetAvailable()
                }
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                runOnUiThread {
                    val connected = isInternetAvailable()

                    if (connected && !lastNetworkAvailable && isStarted) {
                        lastNetworkAvailable = true
                        loadFirebaseProviders()
                    } else if (!connected) {
                        lastNetworkAvailable = false
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_list)

        localDatabase = UmcimbiDatabase.getDatabase(this)

        connectivityManager = getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        findViewById<TextView>(R.id.backButton)
            .setOnClickListener {
                finish()
            }

        val categoryTitle =
            findViewById<TextView>(R.id.categoryTitle)

        container = findViewById(R.id.providersContainer)

        category = intent.getStringExtra("category").orEmpty()

        categoryTitle.text =
            getString(R.string.provider_list_title, category)

        loadFirebaseProviders()
    }

    override fun onStart() {
        super.onStart()

        isStarted = true
        lastNetworkAvailable = isInternetAvailable()

        try {
            connectivityManager.registerDefaultNetworkCallback(
                networkCallback
            )
        } catch (e: Exception) {
            Log.e(
                "UMCIMBI_NETWORK",
                "Unable to register network callback",
                e
            )
        }
    }

    override fun onStop() {
        isStarted = false

        try {
            connectivityManager.unregisterNetworkCallback(
                networkCallback
            )
        } catch (e: Exception) {
            Log.e(
                "UMCIMBI_NETWORK",
                "Unable to unregister network callback",
                e
            )
        }

        super.onStop()
    }

    private fun isInternetAvailable(): Boolean {
        val network = connectivityManager.activeNetwork
            ?: return false

        val capabilities =
            connectivityManager.getNetworkCapabilities(network)
                ?: return false

        return capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) && capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_VALIDATED
        )
    }

    private fun loadCachedProviders() {

        lifecycleScope.launch {
            try {
                val providers = withContext(Dispatchers.IO) {
                    localDatabase.providerDao()
                        .getProvidersByCategory(category)
                }

                if (providers.isNotEmpty()) {
                    displayProviders(providers)
                    hasDisplayedProviders = true
                } else {
                    showOnlyMessage(
                        getString(R.string.loading_providers)
                    )
                }

            } catch (e: Exception) {
                Log.e(
                    "UMCIMBI_ROOM",
                    "Unable to load cached providers",
                    e
                )

                showOnlyMessage(
                    getString(R.string.loading_providers)
                )
            }

            loadFirebaseProviders()
        }
    }

    private fun loadFirebaseProviders() {

        if (isRefreshing) return

        isRefreshing = true

        db.collection("providers")
            .whereEqualTo("category", category)
            .get(Source.SERVER)
            .addOnSuccessListener { documents ->

                val providers = documents.map { document ->

                    ProviderEntity(
                        providerId = document.id,
                        businessName =
                            document.getString("businessName")
                                ?: "Unnamed Provider",
                        category =
                            document.getString("category")
                                ?: category,
                        location =
                            document.getString("location")
                                ?: "Location unavailable",
                        price =
                            document.getDouble("price") ?: 0.0,
                        rating =
                            document.getDouble("rating") ?: 0.0,
                        availability =
                            document.getString("availability")
                                ?: "Not specified"
                    )
                }

                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            val dao = localDatabase.providerDao()

                            // Replace this category's cached snapshot.
                            // Only do this after a successful server response.
                            dao.clearProvidersByCategory(category)
                            dao.insertProviders(providers)
                        }

                        displayProviders(providers)
                        hasDisplayedProviders = true

                    } catch (e: Exception) {
                        Log.e(
                            "UMCIMBI_ROOM",
                            "Unable to synchronise providers",
                            e
                        )

                        // Still display the successful online result.
                        displayProviders(providers)
                        hasDisplayedProviders = true
                    } finally {
                        isRefreshing = false
                    }
                }
            }
            .addOnFailureListener { exception ->

                isRefreshing = false

                Log.e(
                    "UMCIMBI_FIRESTORE",
                    "Unable to load online providers",
                    exception
                )

                if (!hasDisplayedProviders) {
                    loadOfflineFallback()
                }
            }
    }

    private fun loadOfflineFallback() {

        lifecycleScope.launch {
            try {
                val providers = withContext(Dispatchers.IO) {
                    localDatabase.providerDao()
                        .getProvidersByCategory(category)
                }

                if (providers.isNotEmpty()) {
                    displayProviders(providers)
                    hasDisplayedProviders = true
                } else {
                    showOnlyMessage(
                        getString(R.string.no_cached_providers)
                    )
                }

            } catch (e: Exception) {
                showOnlyMessage(
                    getString(R.string.providers_load_error)
                )
            }
        }
    }

    private fun displayProviders(
        providers: List<ProviderEntity>
    ) {

        container.removeAllViews()

        if (providers.isEmpty()) {
            showMessage(
                getString(R.string.no_category_providers, category)
            )
            return
        }

        val sortedProviders = providers.sortedWith(
            compareByDescending<ProviderEntity> {
                it.availability.equals(
                    "Available",
                    ignoreCase = true
                )
            }.thenBy {
                it.businessName.lowercase(Locale.ROOT)
            }
        )

        for (provider in sortedProviders) {

            val available = provider.availability.equals(
                "Available",
                ignoreCase = true
            )

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL

                val padding = dp(18)
                setPadding(padding, padding, padding, padding)

                setBackgroundColor(Color.WHITE)

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(12)
                }
            }

            val nameText = TextView(this).apply {
                text = provider.businessName
                textSize = 19f
                setTextColor(Color.BLACK)
                setTypeface(null, Typeface.BOLD)
            }

            val categoryText = TextView(this).apply {
                text = provider.category
                textSize = 13f
                setTextColor(Color.DKGRAY)
                setPadding(0, dp(5), 0, dp(8))
            }

            val locationText = TextView(this).apply {
                text = getString(
                    R.string.provider_location_label,
                    provider.location
                )
                textSize = 14f
                setTextColor(Color.DKGRAY)
            }

            val priceText = TextView(this).apply {
                text = getString(
                    R.string.provider_price_label,
                    String.format(
                        Locale("en", "ZA"),
                        "%,.2f",
                        provider.price
                    )
                )
                textSize = 15f
                setTextColor(Color.BLACK)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, dp(8), 0, dp(5))
            }

            val ratingText = TextView(this).apply {
                text = if (provider.rating > 0.0) {
                    getString(
                        R.string.provider_rating_label,
                        String.format(
                            Locale.US,
                            "%.1f",
                            provider.rating
                        )
                    )
                } else {
                    getString(R.string.provider_rating_unavailable)
                }

                textSize = 13f
                setTextColor(Color.DKGRAY)
                setPadding(0, 0, 0, dp(8))
            }

            val availabilityText = TextView(this).apply {
                text = if (available) {
                    getString(R.string.provider_status_available)
                } else {
                    getString(R.string.provider_status_unavailable)
                }

                textSize = 14f
                setTypeface(null, Typeface.BOLD)

                setTextColor(
                    if (available) {
                        Color.rgb(32, 117, 68)
                    } else {
                        Color.rgb(170, 55, 55)
                    }
                )

                setPadding(0, 0, 0, dp(12))
            }

            val viewText = TextView(this).apply {
                text = getString(R.string.view_provider_profile)
                textSize = 15f
                setTextColor(Color.BLACK)
                gravity = Gravity.END
                setTypeface(null, Typeface.BOLD)
            }

            card.addView(nameText)
            card.addView(categoryText)
            card.addView(locationText)
            card.addView(priceText)
            card.addView(ratingText)
            card.addView(availabilityText)
            card.addView(viewText)

            card.setOnClickListener {
                val detailIntent = Intent(
                    this,
                    ProviderDetailActivity::class.java
                )

                detailIntent.putExtra(
                    "providerId",
                    provider.providerId
                )

                startActivity(detailIntent)
            }

            container.addView(card)
        }
    }

    private fun showOnlyMessage(message: String) {
        container.removeAllViews()
        showMessage(message)
    }

    private fun showMessage(message: String) {

        val textView = TextView(this).apply {
            text = message
            textSize = 14f
            setTextColor(Color.DKGRAY)
        }

        container.addView(textView)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
