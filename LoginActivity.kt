package com.example.umcimbisa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private val db = FirebaseFirestore.getInstance()

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

        val googleSignInButton =
            findViewById<Button>(R.id.googleSignInButton)

        // EMAIL AND PASSWORD LOGIN
        loginButton.setOnClickListener {

            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isEmpty()) {
                emailInput.error = "Enter your email"
                emailInput.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Enter a valid email"
                emailInput.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordInput.error = "Enter your password"
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            loginButton.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        checkUserRole {
                            loginButton.isEnabled = true
                        }

                    } else {

                        loginButton.isEnabled = true

                        showMessage(
                            task.exception?.message ?: "Login failed"
                        )
                    }
                }
        }

        // REGISTER BUTTON
        registerButton.setOnClickListener {

            startActivity(
                Intent(this, RegisterActivity::class.java)
            )
        }

        // GOOGLE SIGN-IN BUTTON
        googleSignInButton.setOnClickListener {
            signInWithGoogle(googleSignInButton)
        }

        // START GOOGLE SIGN-IN FROM WELCOME SCREEN
        if (
            intent.getBooleanExtra(
                "START_GOOGLE_SIGN_IN",
                false
            )
        ) {
            signInWithGoogle(googleSignInButton)
        }
    }

    private fun signInWithGoogle(button: Button) {

        button.isEnabled = false

        val credentialManager = CredentialManager.create(this)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(
                getString(R.string.default_web_client_id)
            )
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {

            try {

                val result = credentialManager.getCredential(
                    context = this@LoginActivity,
                    request = request
                )

                val credential = result.credential

                if (
                    credential is CustomCredential &&
                    credential.type ==
                    GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {

                    val googleCredential =
                        GoogleIdTokenCredential.createFrom(
                            credential.data
                        )

                    firebaseAuthWithGoogle(
                        googleCredential.idToken,
                        button
                    )

                } else {

                    button.isEnabled = true
                    showMessage("Google account not selected")
                }

            } catch (e: Exception) {

                button.isEnabled = true

                showMessage(
                    e.message ?: "Google Sign-In failed"
                )
            }
        }
    }

    private fun firebaseAuthWithGoogle(
        idToken: String,
        button: Button
    ) {

        val credential =
            GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->

                if (!task.isSuccessful) {

                    button.isEnabled = true

                    showMessage(
                        task.exception?.message
                            ?: "Google authentication failed"
                    )

                    return@addOnCompleteListener
                }

                val firebaseUser = auth.currentUser

                if (firebaseUser == null) {

                    button.isEnabled = true
                    showMessage("Unable to load account")
                    return@addOnCompleteListener
                }

                val uid = firebaseUser.uid

                val userRef = db.collection("users")
                    .document(uid)

                userRef.get()
                    .addOnSuccessListener { document ->

                        if (document.exists()) {

                            // EXISTING GOOGLE ACCOUNT
                            // USE ITS SAVED ROLE
                            checkUserRole {
                                button.isEnabled = true
                            }

                        } else {

                            // NEW GOOGLE ACCOUNT
                            // DEFAULT TO CUSTOMER
                            val profile = hashMapOf(
                                "id" to uid,
                                "name" to (
                                        firebaseUser.displayName
                                            ?.takeIf { it.isNotBlank() }
                                            ?: "UMCIMBI User"
                                        ),
                                "email" to (firebaseUser.email ?: ""),
                                "role" to "customer",
                                "language" to "English",
                                "phone" to "",
                                "notifications" to true,
                                "darkMode" to false
                            )

                            userRef.set(profile)
                                .addOnSuccessListener {

                                    checkUserRole {
                                        button.isEnabled = true
                                    }
                                }
                                .addOnFailureListener { e ->

                                    button.isEnabled = true

                                    showMessage(
                                        "Profile error: ${e.message}"
                                    )
                                }
                        }
                    }
                    .addOnFailureListener { e ->

                        button.isEnabled = true

                        showMessage(
                            "Unable to load profile: ${e.message}"
                        )
                    }
            }
    }

    // CHECK FIRESTORE ROLE AFTER LOGIN
    private fun checkUserRole(
        onFailure: () -> Unit
    ) {

        val uid = auth.currentUser?.uid

        if (uid == null) {
            showMessage("No signed-in user found")
            onFailure()
            return
        }

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                // PREVENT REDIRECTING A DIFFERENT ACCOUNT
                if (auth.currentUser?.uid != uid) {
                    onFailure()
                    return@addOnSuccessListener
                }

                if (!document.exists()) {

                    showMessage(
                        "User profile not found. Please contact support."
                    )

                    onFailure()
                    return@addOnSuccessListener
                }

                val role = document.getString("role")

                when (role) {

                    "customer" -> {

                        saveFcmToken()

                        showMessage("Login successful")

                        openDestination(HomeActivity::class.java)
                    }

                    "provider" -> {

                        saveFcmToken()

                        showMessage("Welcome, Service Provider!")

                        openDestination(
                            ProviderDashboardActivity::class.java
                        )
                    }

                    else -> {

                        showMessage(
                            "Unknown account type. Please contact support."
                        )

                        onFailure()
                    }
                }
            }
            .addOnFailureListener { e ->

                showMessage(
                    "Unable to check account type: ${e.message}"
                )

                onFailure()
            }
    }

    // SAVE FIREBASE MESSAGING TOKEN
    private fun saveFcmToken() {

        val userId = auth.currentUser?.uid ?: return

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->

                if (auth.currentUser?.uid != userId) {
                    return@addOnSuccessListener
                }

                db.collection("users")
                    .document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener {

                        Log.d(
                            "UMCIMBI_FCM",
                            "FCM token saved successfully"
                        )
                    }
                    .addOnFailureListener { e ->

                        Log.e(
                            "UMCIMBI_FCM",
                            "Unable to save FCM token",
                            e
                        )
                    }
            }
            .addOnFailureListener { e ->

                Log.e(
                    "UMCIMBI_FCM",
                    "Unable to retrieve FCM token",
                    e
                )
            }
    }

    // OPEN CUSTOMER OR PROVIDER SCREEN
    private fun openDestination(
        destination: Class<*>
    ) {

        val intent = Intent(this, destination)

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }

    private fun showMessage(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}
