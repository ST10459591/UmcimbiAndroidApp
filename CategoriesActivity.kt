package com.example.umcimbisa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CategoriesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_categories
        )

        val backButton =
            findViewById<TextView>(
                R.id.backButton
            )

        backButton.setOnClickListener {
            finish()
        }

        val buttons = listOf(
            R.id.cateringButton,
            R.id.venueButton,
            R.id.photographyButton,
            R.id.decorButton,
            R.id.musicButton,
            R.id.transportButton
        )

        buttons.forEach { buttonId ->

            findViewById<Button>(buttonId)
                .setOnClickListener { button ->

                    val category =
                        (button as Button)
                            .text
                            .toString()

                    val intent =
                        Intent(
                            this,
                            SearchActivity::class.java
                        )

                    intent.putExtra(
                        "category",
                        category
                    )

                    startActivity(intent)
                }
        }
    }
}