package com.example.umcimbisa

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.umcimbisa.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nameInput =
            findViewById<EditText>(R.id.nameInput)

        val emailInput =
            findViewById<EditText>(R.id.emailInput)

        val passwordInput =
            findViewById<EditText>(R.id.passwordInput)

        val registerButton =
            findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {

            val name =
                nameInput.text.toString().trim()

            val email =
                emailInput.text.toString().trim()

            val password =
                passwordInput.text.toString()

            if (name.isEmpty()) {

                nameInput.error =
                    "Enter your full name"

                nameInput.requestFocus()

                return@setOnClickListener
            }

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

            if (password.length < 6) {

                passwordInput.error =
                    "Password must be at least 6 characters"

                passwordInput.requestFocus()

                return@setOnClickListener
            }

            registerButton.isEnabled = false

            auth.createUserWithEmailAndPassword(
                email,
                password
            ).addOnCompleteListener { task ->

                if (!task.isSuccessful) {

                    registerButton.isEnabled = true

                    Toast.makeText(
                        this,
                        task.exception?.message
                            ?: "Registration failed",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnCompleteListener
                }

                val currentUser =
                    auth.currentUser

                if (currentUser == null) {

                    registerButton.isEnabled = true

                    Toast.makeText(
                        this,
                        "Registration failed",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnCompleteListener
                }

                val uid =
                    currentUser.uid

                val user = User(
                    id = uid,
                    name = name,
                    email = email,
                    role = "customer",
                    language = "English",
                    phone = "",
                    notifications = true,
                    darkMode = false
                )

                db.collection("users")
                    .document(uid)
                    .set(user)
                    .addOnSuccessListener {

                        Toast.makeText(
                            this,
                            "Registration successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(
                            Intent(
                                this,
                                HomeActivity::class.java
                            )
                        )

                        finish()
                    }
                    .addOnFailureListener { exception ->

                        registerButton.isEnabled = true

                        Toast.makeText(
                            this,
                            "Account created, but profile could not be saved: " +
                                    "${exception.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }
    }
}