package com.example.umcimbisa

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val btnLogin = findViewById<TextView>(R.id.btnLogin)
        val btnRegister = findViewById<TextView>(R.id.btnRegister)
        val btnGoogle = findViewById<TextView>(R.id.btnGoogle)

        // LOGIN BUTTON
        btnLogin.setOnClickListener {
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
        }

        // CREATE ACCOUNT BUTTON
        btnRegister.setOnClickListener {
            startActivity(
                Intent(this, RegisterActivity::class.java)
            )
        }

        // CONTINUE WITH GOOGLE BUTTON
        btnGoogle.setOnClickListener {

            val intent = Intent(
                this,
                LoginActivity::class.java
            )

            intent.putExtra(
                "START_GOOGLE_SIGN_IN",
                true
            )

            startActivity(intent)
        }
    }
}
