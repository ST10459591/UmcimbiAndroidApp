package com.example.umcimbisa

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var messagesContainer: LinearLayout
    private lateinit var messagesScrollView: ScrollView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var chatTitle: TextView

    private var messageListener: ListenerRegistration? = null

    private var conversationId = ""
    private var providerId = ""
    private var providerUid = ""
    private var providerName = ""
    private var customerUid = ""

    private var conversationReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        messagesContainer = findViewById(R.id.messagesContainer)
        messagesScrollView = findViewById(R.id.messagesScrollView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        chatTitle = findViewById(R.id.chatTitle)

        findViewById<TextView>(R.id.backButton)
            .setOnClickListener { finish() }

        sendButton.isEnabled = false

        val user = auth.currentUser

        if (user == null) {
            showError("Please log in first.")
            return
        }

        val existingConversationId =
            intent.getStringExtra("conversationId").orEmpty()

        if (existingConversationId.isNotBlank()) {
            openExistingConversation(existingConversationId)
        } else {
            openProviderConversation(user.uid)
        }

        sendButton.setOnClickListener {
            sendMessage()
        }
    }

    // OPEN A CHAT FROM THE MESSAGES INBOX
    private fun openExistingConversation(id: String) {

        db.collection("conversations")
            .document(id)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    showError("Conversation not found.")
                    return@addOnSuccessListener
                }

                val uid = auth.currentUser?.uid

                val participants =
                    document.get("participants") as? List<*>

                if (uid == null || participants == null ||
                    uid !in participants
                ) {
                    showError("You cannot access this conversation.")
                    return@addOnSuccessListener
                }

                conversationId = id
                customerUid =
                    document.getString("customerId").orEmpty()
                providerUid =
                    document.getString("providerUid").orEmpty()
                providerId =
                    document.getString("providerId").orEmpty()
                providerName =
                    document.getString("providerName")
                        ?: "Service Provider"

                chatTitle.text =
                    if (uid == customerUid) {
                        providerName
                    } else {
                        getString(R.string.customer_conversation)
                    }

                startChat()
            }
            .addOnFailureListener { error ->
                showError(
                    "Unable to open conversation: " +
                            error.localizedMessage
                )
            }
    }

    // OPEN A CHAT FROM A PROVIDER PROFILE
    private fun openProviderConversation(currentUid: String) {

        providerId =
            intent.getStringExtra("providerId").orEmpty()

        if (providerId.isBlank()) {
            showError("Provider not found.")
            return
        }

        db.collection("providers")
            .document(providerId)
            .get()
            .addOnSuccessListener { provider ->

                if (!provider.exists()) {
                    showError("Provider not found.")
                    return@addOnSuccessListener
                }

                providerUid =
                    provider.getString("userId").orEmpty()

                providerName =
                    provider.getString("businessName")
                        ?: "Service Provider"

                if (providerUid.isBlank()) {
                    showError("Provider account not linked.")
                    return@addOnSuccessListener
                }

                if (providerUid == currentUid) {
                    showError("You cannot message yourself.")
                    return@addOnSuccessListener
                }

                customerUid = currentUid
                chatTitle.text = providerName

                conversationId =
                    listOf(customerUid, providerUid)
                        .sorted()
                        .joinToString("_")

                prepareConversation()
            }
            .addOnFailureListener { error ->
                showError(
                    "Provider error: ${error.localizedMessage}"
                )
            }
    }


    private fun prepareConversation() {

        val conversationRef =
            db.collection("conversations")
                .document(conversationId)

        val conversationData = hashMapOf(
            "participants" to listOf(customerUid, providerUid),
            "customerId" to customerUid,
            "providerUid" to providerUid,
            "providerId" to providerId,
            "providerName" to providerName,
            "lastMessage" to "",
            "updatedAt" to FieldValue.serverTimestamp()
        )

        conversationRef.set(conversationData)
            .addOnSuccessListener {
                startChat()
            }
            .addOnFailureListener {

                // Existing conversations may reject overwriting
                // protected fields. Read and verify membership.
                conversationRef.get()
                    .addOnSuccessListener { document ->

                        val participants =
                            document.get("participants") as? List<*>

                        if (document.exists() &&
                            auth.currentUser?.uid in
                            (participants ?: emptyList<Any>())
                        ) {
                            startChat()
                        } else {
                            showError(
                                "Unable to access this conversation."
                            )
                        }
                    }
                    .addOnFailureListener { error ->
                        showError(
                            "Chat setup failed: ${error.localizedMessage}"
                        )
                    }
            }
    }


    private fun startChat() {

        if (conversationReady) return

        conversationReady = true
        sendButton.isEnabled = true

        listenForMessages()
    }

    // SEND A MESSAGE
    private fun sendMessage() {

        if (!conversationReady) {
            showToast("Chat is still loading.")
            return
        }

        val message =
            messageInput.text.toString().trim()

        if (message.isBlank()) {
            messageInput.error = "Enter a message"
            return
        }

        if (message.length > 2000) {
            messageInput.error = "Maximum 2000 characters"
            return
        }

        val user = auth.currentUser ?: return

        sendButton.isEnabled = false

        val conversationRef =
            db.collection("conversations")
                .document(conversationId)

        val messageRef =
            conversationRef.collection("messages").document()

        val messageData = hashMapOf(
            "senderId" to user.uid,
            "text" to message,
            "timestamp" to FieldValue.serverTimestamp()
        )

        val batch = db.batch()

        batch.set(messageRef, messageData)

        batch.update(
            conversationRef,
            mapOf(
                "lastMessage" to message,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )

        batch.commit()
            .addOnSuccessListener {

                messageInput.text.clear()
                sendButton.isEnabled = true
            }
            .addOnFailureListener { error ->

                sendButton.isEnabled = true

                showToast(
                    "Send failed: ${error.localizedMessage}"
                )
            }
    }

    // LISTEN FOR MESSAGES IN REAL TIME
    private fun listenForMessages() {

        messageListener?.remove()

        messageListener =
            db.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .orderBy(
                    "timestamp",
                    Query.Direction.ASCENDING
                )
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        showToast(
                            "Messages error: " +
                                    error.localizedMessage
                        )
                        return@addSnapshotListener
                    }

                    messagesContainer.removeAllViews()

                    if (snapshot == null || snapshot.isEmpty) {
                        addInfoText("No messages yet. Say hello!")
                        return@addSnapshotListener
                    }

                    for (document in snapshot.documents) {

                        val text =
                            document.getString("text").orEmpty()

                        val senderId =
                            document.getString("senderId").orEmpty()

                        addMessageBubble(
                            text,
                            senderId == auth.currentUser?.uid
                        )
                    }

                    messagesScrollView.post {
                        messagesScrollView.fullScroll(
                            ScrollView.FOCUS_DOWN
                        )
                    }
                }
    }

    private fun addMessageBubble(
        text: String,
        isMine: Boolean
    ) {

        val bubble = TextView(this).apply {

            this.text = text
            textSize = 15f

            setTextColor(Color.BLACK)
            setPadding(28, 20, 28, 20)

            setBackgroundColor(
                if (isMine) {
                    Color.rgb(220, 220, 220)
                } else {
                    Color.WHITE
                }
            )
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {

            gravity =
                if (isMine) Gravity.END else Gravity.START

            bottomMargin = 14
            marginStart = 12
            marginEnd = 12
        }

        messagesContainer.addView(bubble, params)
    }

    private fun addInfoText(message: String) {

        val textView = TextView(this).apply {

            text = message
            textSize = 14f
            gravity = Gravity.CENTER

            setTextColor(Color.DKGRAY)
            setPadding(16, 32, 16, 32)
        }

        messagesContainer.addView(textView)
    }

    private fun showError(message: String) {

        sendButton.isEnabled = false
        messagesContainer.removeAllViews()

        addInfoText(message)
        showToast(message)
    }

    private fun showToast(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroy() {

        messageListener?.remove()
        super.onDestroy()
    }
}
