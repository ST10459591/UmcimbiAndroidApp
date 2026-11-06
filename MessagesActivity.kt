package com.example.umcimbisa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges

class MessagesActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var conversationsContainer: LinearLayout

    private var listener: ListenerRegistration? = null
    private var initialSnapshotReceived = false

    // Keep track of the last message in each conversation.
    private val knownMessages = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        conversationsContainer =
            findViewById(R.id.conversationsContainer)

        findViewById<TextView>(R.id.backButton)
            .setOnClickListener { finish() }

        setupBottomNavigation()

        val uid = auth.currentUser?.uid

        if (uid == null) {
            showMessage(getString(R.string.login_to_messages))
            return
        }

        listenForConversations(uid)
    }

    private fun listenForConversations(uid: String) {

        listener = db.collection("conversations")
            .whereArrayContains("participants", uid)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->

                if (error != null) {
                    conversationsContainer.removeAllViews()
                    showMessage(getString(R.string.messages_load_error))
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    return@addSnapshotListener
                }

                // First server-confirmed snapshot establishes
                // existing conversations without notifications.
                if (!initialSnapshotReceived) {

                    snapshot.documents.forEach { conversation ->
                        knownMessages[conversation.id] =
                            conversation.getString("lastMessage").orEmpty()
                    }

                    if (!snapshot.metadata.isFromCache) {
                        initialSnapshotReceived = true
                    }

                    displayConversations(snapshot.documents, uid)
                    return@addSnapshotListener
                }

                // Only notify for changes confirmed by Firestore.
                if (!snapshot.metadata.isFromCache) {

                    for (change in snapshot.documentChanges) {

                        val conversation = change.document
                        val conversationId = conversation.id
                        val newMessage =
                            conversation.getString("lastMessage").orEmpty()

                        when (change.type) {

                            DocumentChange.Type.MODIFIED -> {

                                val previousMessage =
                                    knownMessages[conversationId]

                                if (
                                    previousMessage != null &&
                                    newMessage.isNotBlank() &&
                                    previousMessage != newMessage
                                ) {
                                    showNewMessageNotification(
                                        conversation,
                                        uid
                                    )
                                }

                                knownMessages[conversationId] = newMessage
                            }

                            DocumentChange.Type.ADDED -> {
                                knownMessages[conversationId] = newMessage
                            }

                            DocumentChange.Type.REMOVED -> {
                                knownMessages.remove(conversationId)
                            }
                        }
                    }
                }

                displayConversations(snapshot.documents, uid)
            }
    }

    private fun showNewMessageNotification(
        conversation: DocumentSnapshot,
        uid: String
    ) {

        val customerId =
            conversation.getString("customerId").orEmpty()

        val senderName = if (uid == customerId) {
            conversation.getString("providerName")
                ?: getString(R.string.service_provider)
        } else {
            getString(R.string.customer_conversation)
        }

        Toast.makeText(
            this,
            getString(
                R.string.new_message_notification,
                senderName
            ),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun displayConversations(
        documents: List<DocumentSnapshot>,
        uid: String
    ) {

        conversationsContainer.removeAllViews()

        if (documents.isEmpty()) {
            showMessage(getString(R.string.no_conversations))
            return
        }

        val conversations = documents.sortedByDescending {
            it.getTimestamp("updatedAt")?.seconds ?: 0L
        }

        for (conversation in conversations) {

            val conversationId = conversation.id

            val customerId =
                conversation.getString("customerId").orEmpty()

            val providerName =
                conversation.getString("providerName")
                    ?: getString(R.string.service_provider)

            val lastMessage =
                conversation.getString("lastMessage").orEmpty()

            val title = if (uid == customerId) {
                providerName
            } else {
                getString(R.string.customer_conversation)
            }

            val card = TextView(this).apply {

                text = "$title\n" +
                        lastMessage.ifBlank {
                            getString(R.string.no_messages_yet)
                        }

                textSize = 16f
                setTextColor(Color.parseColor("#111111"))
                setBackgroundColor(Color.WHITE)
                setPadding(24, 28, 24, 28)

                setOnClickListener {
                    startActivity(
                        Intent(
                            this@MessagesActivity,
                            ChatActivity::class.java
                        ).putExtra(
                            "conversationId",
                            conversationId
                        )
                    )
                }
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }

            conversationsContainer.addView(card, params)
        }
    }

    private fun showMessage(message: String) {

        conversationsContainer.addView(
            TextView(this).apply {
                text = message
                textSize = 16f
                setTextColor(Color.DKGRAY)
            }
        )
    }

    private fun setupBottomNavigation() {

        findViewById<TextView>(R.id.navHomeButton)
            .setOnClickListener {
                openPage(HomeActivity::class.java)
            }

        findViewById<TextView>(R.id.navExploreButton)
            .setOnClickListener {
                openPage(CategoriesActivity::class.java)
            }

        findViewById<TextView>(R.id.navBookingsButton)
            .setOnClickListener {
                openPage(MyBookingsActivity::class.java)
            }

        findViewById<TextView>(R.id.navMessagesButton).apply {
            setTextColor(Color.parseColor("#111111"))
            setOnClickListener { }
        }

        findViewById<TextView>(R.id.navProfileButton)
            .setOnClickListener {
                openPage(ProfileActivity::class.java)
            }

        listOf(
            R.id.navHomeButton,
            R.id.navExploreButton,
            R.id.navBookingsButton,
            R.id.navProfileButton
        ).forEach {
            findViewById<TextView>(it)
                .setTextColor(Color.parseColor("#666666"))
        }
    }

    private fun openPage(
        destination: Class<out android.app.Activity>
    ) {

        val intent = Intent(this, destination)

        intent.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        )

        startActivity(intent)
    }

    override fun onDestroy() {
        listener?.remove()
        super.onDestroy()
    }
}
