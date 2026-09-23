package com.example.umcimbisa

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailInput =
            findViewById<EditText>(R.id.emailInput)

        val passwordInput =
            findViewById<EditText>(R.id.passwordInput)

        val loginButton =
            findViewById<Button>(R.id.loginButton)

        val registerButton =
            findViewById<Button>(R.id.registerButton)

        loginButton.setOnClickListener {

            val email =
                emailInput.text.toString().trim()

            val password =
                passwordInput.text.toString()

            if (email.isEmpty()) {

                emailInput.error =
                    "Enter your email"

                emailInput.requestFocus()

                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches()
            ) {

                emailInput.error =
                    "Enter a valid email"

                emailInput.requestFocus()

                return@setOnClickListener
            }

            if (password.isEmpty()) {

                passwordInput.error =
                    "Enter your password"

                passwordInput.requestFocus()

                return@setOnClickListener
            }

            loginButton.isEnabled = false

            auth.signInWithEmailAndPassword(
                email,
                password
            ).addOnCompleteListener { task ->

                loginButton.isEnabled = true

                if (task.isSuccessful) {

                    Toast.makeText(
                        this,
                        "Login successful",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(
                        Intent(
                            this,
                            HomeActivity::class.java
                        )
                    )

                    finish()

                } else {

                    Toast.makeText(
                        this,
                        task.exception?.message
                            ?: "Login failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        registerButton.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    RegisterActivity::class.java
                )
            )
        }
    }
}