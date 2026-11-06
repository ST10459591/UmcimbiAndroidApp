package com.example.umcimbisa

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CategoriesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        // BACK BUTTON
        findViewById<TextView>(R.id.backButton)
            .setOnClickListener {
                finish()
            }

        // CATEGORY BUTTONS
        val categories = mapOf(
            R.id.cateringButton to "Catering",
            R.id.venueButton to "Venues",
            R.id.photographyButton to "Photography",
            R.id.decorButton to "Decor",
            R.id.musicButton to "Music & Entertainment",
            R.id.transportButton to "Transport"
        )

        categories.forEach { (categoryId, categoryName) ->

            findViewById<TextView>(categoryId)
                .setOnClickListener {

                    val intent = Intent(
                        this,
                        ProviderListActivity::class.java
                    )

                    intent.putExtra("category", categoryName)
                    startActivity(intent)
                }
        }

        // BOTTOM NAVIGATION
        findViewById<TextView>(R.id.navHomeButton)
            .setOnClickListener {
                openPage(HomeActivity::class.java)
            }

        // ALREADY ON EXPLORE
        findViewById<TextView>(R.id.navExploreButton)
            .setOnClickListener {
                // Stay on Categories
            }

        findViewById<TextView>(R.id.navBookingsButton)
            .setOnClickListener {
                openPage(MyBookingsActivity::class.java)
            }

        findViewById<TextView>(R.id.navProfileButton)
            .setOnClickListener {
                openPage(ProfileActivity::class.java)
            }

        // MESSAGES
        findViewById<TextView>(R.id.navMessagesButton)
            .setOnClickListener {
                openPage(MessagesActivity::class.java)
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
}
