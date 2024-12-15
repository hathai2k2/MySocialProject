package com.example.mysocialproject.ui.feature.Admin

import android.util.Log
import com.example.mysocialproject.ui.feature.model.Post
import com.example.mysocialproject.ui.feature.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.net.URLDecoder
import java.util.Calendar
import java.util.Date

class AdminRepository {

    private val fireStore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getAllPosts(onSuccess: (List<Post>) -> Unit, onFailure: (Exception) -> Unit) {
        val now = Date() // Lấy ngày hiện tại
        val startOfDay = now.toStartOfDay()
        val endOfDay = now.toEndOfDay()

        fireStore.collection("posts")
            .whereGreaterThanOrEqualTo("createdAt", startOfDay)
            .whereLessThan("createdAt", endOfDay)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val posts = result.map { document ->
                    document.toObject(Post::class.java)
                }
                onSuccess(posts)
            }
            .addOnFailureListener { exception ->
                Log.w("PostRepository", "Error getting posts for today.", exception)
                onFailure(exception)
            }
    }

    private fun Date.toStartOfDay(): Date {
        return Calendar.getInstance().apply {
            time = this@toStartOfDay
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    private fun Date.toEndOfDay(): Date {
        return Calendar.getInstance().apply {
            time = this@toEndOfDay
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
    }

    suspend fun deletePost(postId: String): Boolean {
        val postRef = fireStore.collection("posts").document(postId)
        val currentId = auth.currentUser?.uid

        return if (currentId != null) {
            try {
                val postSnapshot = postRef.get().await()
                if (postSnapshot.exists()) {
                    val post = postSnapshot.toObject(Post::class.java)
                    if (post != null) {
                        val decodedImageUrl = URLDecoder.decode(post.imageURL, "UTF-8")
                        val decodedVoiceUrl = URLDecoder.decode(post.voiceURL, "UTF-8")
                        when {
                            post.imageURL == "" && post.voiceURL != "" -> {
                                val voiceRef = storage.getReferenceFromUrl(decodedVoiceUrl)
                                Log.d("XOAVOICE", "delete URL obtained: $voiceRef")
                                voiceRef.delete().await()
                            }

                            post.imageURL != "" && post.voiceURL == "" -> {
                                val imageRef = storage.getReferenceFromUrl(decodedImageUrl)
                                imageRef.delete().await()
                            }
                        }
                        //xoa like
                        val likesSnapshot = fireStore.collection("likes")
                            .whereEqualTo("postId", postId)
                            .get()
                            .await()
                        if (!likesSnapshot.isEmpty) {
                            likesSnapshot.documents.forEach { it.reference.delete().await() }
                        }

                        // xoa noi dung cmt
                        val messagesSnapshot = fireStore.collection("messages")
                            .whereEqualTo("postId", postId)
                            .get()
                            .await()
                        if (!messagesSnapshot.isEmpty) {
                            messagesSnapshot.documents.forEach {
                                it.reference.update(
                                    mapOf(
                                        "postId" to "",
                                        "imageUrl" to "",
                                        "voiceUrl" to "",
                                        "timestamp" to "",
                                        "content" to "",
                                        "avtpost" to ""
                                    )
                                ).await()
                            }
                        }
                        postRef.delete().await()
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.d("PostRepository", "Error: ${e}")
                false
            }
        } else {
            false
        }
    }


    suspend fun getUsersWithMissingNameAndAvatar(): List<User> {
        val usersRef = fireStore.collection("users")

        return try {
            val snapshot = usersRef.get().await()
            snapshot.documents.mapNotNull { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    user
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.d(
                "UserRepository",
                "Error fetching users with missing name and avatar: ${e.message}"
            )
            emptyList()
        }
    }

    // Xóa tài khoản khỏi Firestore
    suspend fun deleteUserFromFirestore(userId: String): Boolean {
        return try {
            fireStore.collection("users").document(userId).delete().await()

            true
        } catch (e: Exception) {
            Log.d("UserRepository", "Error deleting user from Firestore: ${e.message}")
            false
        }
    }


}