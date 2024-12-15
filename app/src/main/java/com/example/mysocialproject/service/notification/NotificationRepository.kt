import android.util.Base64
import android.util.Log
import com.example.mysocialproject.ui.feature.model.Like
import com.example.mysocialproject.ui.feature.model.LikeStatus
import com.example.mysocialproject.ui.feature.model.Message
import com.example.mysocialproject.ui.feature.model.MessageStatus
import com.example.mysocialproject.ui.feature.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val fireStore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid
    private var listenJob: Job? = null
    private val repositoryScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main) // CoroutineScope riêng cho Repository

    fun getMessagesFromFriends(): Flow<List<Message>> = callbackFlow {
        val listenerRegistration = fireStore.collection("messages")
            .whereEqualTo("receiverId", userId)
            .whereEqualTo("status", MessageStatus.SENT.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }
                val messages = snapshot?.toObjects(Message::class.java) ?: emptyList()
                val decodedMessages = messages.map { message ->
                    try {
                        val decodedMessage =
                            String(Base64.decode(message.message, Base64.NO_WRAP), Charsets.UTF_8)
                        message.copy(message = decodedMessage)
                    } catch (e: IllegalArgumentException) {
                        Log.e(
                            "MessageDecodeError",
                            "Failed to decode message ID: ${message.messageId}, message: ${message.message}",
                            e
                        )
                        message
                    }
                }
                trySend(decodedMessages)
            }
        awaitClose { listenerRegistration.remove() }
    }

    fun listenForLatestMessage(callback: (Message?, User?) -> Unit) {
        listenJob?.cancel()
        listenJob = repositoryScope.launch {
            getMessagesFromFriends()
                .collect { messages ->
                    val latestMessage = messages.maxByOrNull { it.createdAt.toString() }
                    if (latestMessage?.status == MessageStatus.SENT) {
                        Log.d(
                            "NotificationRepository",
                            "listenForLatestMessage: latestMessage = $latestMessage"
                        )
                        val sender = getSenderInfo(latestMessage.senderId ?: "")
                        callback(latestMessage, sender)
                        updateMessageStatus(latestMessage.senderId!!, MessageStatus.DELIVERED)
                    }
                }
        }
    }

    suspend fun updateMessageStatus(senderId: String, newStatus: MessageStatus) {
        try {
            val messagesRef = fireStore.collection("messages")
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("receiverId", auth.currentUser?.uid!!)
                .whereNotIn("status", listOf(MessageStatus.READ.name, MessageStatus.DELIVERED.name))
            val querySnapshot = messagesRef.get().await()
            for (document in querySnapshot.documents) {
                document.reference.update("status", newStatus).await()
            }
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error updating message status", e)
        }
    }

    suspend fun getSenderInfo(senderId: String): User? {
        return try {
            fireStore.collection("users").document(senderId).get().await()
                .toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error fetching sender info", e)
            null
        }
    }


    fun listenForNewLikes(callback: (Like, User) -> Unit) {
        fireStore.collection("likes")
            .whereEqualTo("ownerId", userId)
            .whereEqualTo("status", LikeStatus.NEW.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationRepository", "Listen failed: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    for (documentChange in snapshot.documentChanges) {
                        if (documentChange.type == DocumentChange.Type.ADDED) {
                            val like = documentChange.document.toObject(Like::class.java)
                            repositoryScope.launch {
                                val user = getUserlike(like.userId)
                                if (user != null) {
                                    Log.e("NotificationRepository", "Listen failed: ${user} $like")
                                    callback(like, user)
                                    documentChange.document.reference.update(
                                        "status",
                                        LikeStatus.NOTIFIED.name
                                    ).await()
                                }
                            }
                        }
                    }
                }
            }
    }

    private suspend fun getUserlike(userId: String): User? {
        return try {
            fireStore.collection("users").document(userId).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error fetching user: ${e.message}")
            null
        }
    }


    fun cancelListen() {
        listenJob?.cancel()
        repositoryScope.cancel()
    }

}