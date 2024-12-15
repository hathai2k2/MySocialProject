package com.example.mysocialproject.networking.repository

import android.net.Uri
import android.util.Log
import com.example.mysocialproject.ui.feature.model.Post
import com.example.mysocialproject.ui.feature.model.User
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.net.URLDecoder
import java.util.UUID
import javax.inject.Inject


class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private var fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) : UserRepository {

    ////dang ki tai khoan
    override suspend fun signUp(email: String, password: String): Result<Boolean> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()

            val userId = auth.currentUser?.uid
            if (userId != null) {
                val user = User(
                    UserId = userId,
                    emailUser = email,
                    avatarUser = "",
                    nameUser = "",
                    passwordUser = "",
                )
                fireStore.collection("users").document(userId).set(user).await()
                Result.success(true)
            } else {
                Result.failure(Exception("User ID is null"))
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /// dang ki ten va anh dai dien
    override suspend fun createAvtandNameUser(imageUri: Uri, name: String): Result<Boolean> {
        return try {
            val uniqueID = UUID.randomUUID().toString()
            val fileName = "avt_${auth.currentUser!!.uid}_$uniqueID"
            val storageReference =
                storage.reference.child("${auth.currentUser!!.uid}/avatar/$fileName")
            val uploadTask = storageReference.putFile(imageUri).await()
            val downloadUrl = storageReference.downloadUrl.await()

            val userId = auth.currentUser?.uid
            if (userId != null) {
                val updates = if (downloadUrl != null) {
                    mapOf(
                        "avatarUser" to downloadUrl.toString(),
                        "nameUser" to name
                    )
                } else {
                    mapOf(
                        "avatarUser" to null,
                        "nameUser" to name
                    )
                }
                fireStore.collection("users").document(userId).update(updates).await()
                Result.success(true)
            } else {
                Result.failure(Exception("User ID is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    ////dang nhap
    override suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()

            Result.success(true)
        } catch (e: Exception) {
            when (e) {

                is FirebaseAuthInvalidCredentialsException -> {
                    Log.e("LoginError", "Invalid credentials: ${e.message}")
                    Result.failure(e)
                }

                is FirebaseNetworkException -> {
                    Log.e("LoginError", "Network error: ${e.message}")
                    Result.failure(e)
                }

                else -> {
                    Log.e("LoginError", "Other error: ${e.message}")
                    Result.failure(e)
                }

            }
        }
    }

    ///kiem tra dang nhap
    override fun isUserLoggedIn(): Boolean {
        val current = auth.currentUser
        return current != null && current.uid != "KyH8KCgh0pgxFiPXvr76xh5Vux03"
    }

    //isadmin
    override fun isAdmin(): Boolean {
        val current = auth.currentUser
        return current != null && current.uid == "KyH8KCgh0pgxFiPXvr76xh5Vux03"
    }

    private var isDataLoaded = false
    private var cachedUser: User? = null
    override suspend fun getInfoUser(): Result<User> {
        if (isDataLoaded) {
            return cachedUser?.let { Result.success(it) }
                ?: Result.failure(Exception("No cached data"))
        }
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userId = currentUser.uid

                val userDocumentRef = fireStore.collection("users").document(userId)
                val userSnapshot = userDocumentRef.get().await()

                userDocumentRef.addSnapshotListener { documentSnapshot, e ->
                    if (e != null) {
                        // Xử lý lỗi
                        return@addSnapshotListener
                    }
                }
                Log.d("TAGY", "tt: $userSnapshot")
                if (userSnapshot.exists()) {
                    val user = userSnapshot.toObject(User::class.java)
                    isDataLoaded = true
                    cachedUser = user
                    Log.d("TAGY", "User data: $user")
                    if (user != null) {
                        Result.success(user)
                    } else {
                        Result.failure(Exception("loi k ep sang user"))
                    }
                } else {
                    Result.failure(Exception("User document does not exist"))
                }
            } else {
                Log.e("getInfoUser", "Current user is null")
                Result.failure(Exception("Current user is null"))
            }
        } catch (e: Exception) {
            Log.e("getInfoUser", "Exception: ${e.message}")
            Result.failure(e)
        }
    }

    override fun getCurrentUid(): String {
        return auth.currentUser?.uid.toString()
    }


    //quenmatkhau

    override suspend fun forgotPassword(email: String): Result<Boolean> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(true)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun updateUserAvatarInPosts(userId: String, newAvatarUrl: String) {
        try {
            val postsRef = fireStore.collection("posts")
            val querySnapshot = postsRef.whereEqualTo("userId", userId).get().await()
            val batch = fireStore.batch()

            for (document in querySnapshot.documents) {
                val postRef = postsRef.document(document.id)
                batch.update(postRef, "userAvatar", newAvatarUrl)
            }

            batch.commit().await()
        } catch (e: Exception) {
            Log.e("TAG", "Error updating user avatar in posts: ${e.message}", e)
        }
    }

    override suspend fun updateAvatar(imageUri: Uri): Result<Boolean> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Chưa đăng nhập"))
            val uniqueID = UUID.randomUUID().toString()
            val fileName = "avt_${auth.currentUser!!.uid}_$uniqueID"
            val storageReference =
                storage.reference.child("${auth.currentUser!!.uid}/avatar/$fileName")
            val uploadTask = storageReference.putFile(imageUri).await()
            val downloadUrl = storageReference.downloadUrl.await()

            val userId = user.uid
            val updates = mapOf(
                "avatarUser" to downloadUrl.toString()
            )
            fireStore.collection("users").document(userId).update(updates).await()
            updateUserAvatarInPosts(userId, downloadUrl.toString())
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserNameInPosts(userId: String, newName: String) {
        try {
            val postsRef = fireStore.collection("posts")
            val querySnapshot = postsRef.whereEqualTo("userId", userId).get().await()
            val batch = fireStore.batch()

            for (document in querySnapshot.documents) {
                val postRef = postsRef.document(document.id)
                batch.update(postRef, "userName", newName)
            }

            batch.commit().await()
        } catch (e: Exception) {
            Log.e("TAG", "Error updating user name in posts: ${e.message}", e)
        }
    }

    //doi ten
    override suspend fun updateName(newName: String): Result<Boolean> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Chưa đăng nhập"))
            val userId = user.uid
            val docRef = fireStore.collection("users").document(userId)
            docRef.update("nameUser", newName).await()
            updateUserNameInPosts(userId, newName)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // doi mat khau
    override suspend fun updatePassword(newPassword: String): Result<Boolean> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Chưa đăng nhập"))
            user.updatePassword(newPassword).await() // Chờ Task hoàn thành
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun deleteAllPostUser(userId: String) {
        val postsSnapshot = fireStore.collection("posts")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        // Duyệt qua từng bài viết và xóa chúng
        for (document in postsSnapshot.documents) {
            val postRef = document.reference
            val post = document.toObject(Post::class.java)
            if (post != null) {
                val decodedImageUrl = URLDecoder.decode(post.imageURL, "UTF-8")
                val decodedVoiceUrl = URLDecoder.decode(post.imageURL, "UTF-8")
                when {
                    post.imageURL == null && post.voiceURL != null -> {
                        val voiceRef = storage.getReferenceFromUrl(decodedVoiceUrl)
                        voiceRef.delete().await()
                    }

                    post.imageURL != null && post.voiceURL == null -> {
                        val imageRef = storage.getReferenceFromUrl(decodedImageUrl)
                        imageRef.delete().await()
                    }
                }

                val likesSnapshot = fireStore.collection("likes")
                    .whereEqualTo("postId", post.postId)
                    .get()
                    .await()
                if (!likesSnapshot.isEmpty) {
                    likesSnapshot.documents.forEach { it.reference.delete().await() }
                }

                val messagesSnapshot = fireStore.collection("messages")
                    .whereEqualTo("postId", post.postId)
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
            }
        }
    }

    override suspend fun deleteAccount(): Result<Boolean> {
        val user = auth.currentUser
        val userId = user?.uid
        return if (user != null && userId != null) {

            try {
                //xoa tn
                val messagesSnapshot = fireStore.collection("messages")
                    .whereEqualTo("senderId", userId)
                    .get()
                    .await()

                for (document in messagesSnapshot.documents) {
                    document.reference.delete().await()
                }
                Log.d("UserRepository", "Deleted ${messagesSnapshot.documents.size} messages.")
                // xoa like
                val likesSnapshot = fireStore.collection("likes")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                for (document in likesSnapshot.documents) {
                    document.reference.delete().await()
                }
                Log.d("UserRepository", "Deleted ${likesSnapshot.documents.size} likes.")
                // xoa khoi ban be
                val friendshipsSnapshot = fireStore.collection("friendships")
                    .whereEqualTo("uid1", userId)
                    .get()
                    .await()

                for (document in friendshipsSnapshot.documents) {
                    document.reference.delete().await()
                }
                Log.d(
                    "UserRepository",
                    "Deleted ${friendshipsSnapshot.documents.size} friendships (uid1)."
                )
                val friendshipsSnapshot2 = fireStore.collection("friendships")
                    .whereEqualTo("uid2", userId)
                    .get()
                    .await()

                for (document in friendshipsSnapshot2.documents) {
                    document.reference.delete().await()
                }
                Log.d(
                    "UserRepository",
                    "Deleted ${friendshipsSnapshot2.documents.size} friendships (uid2)."
                )


                val storageRef = storage.reference.child(userId)
                storageRef.child("avatar").listAll().await().items.forEach { it.delete().await() }
                storageRef.child("post_voice").listAll().await().items.forEach {
                    it.delete().await()
                }
                storageRef.child("post_image").listAll().await().items.forEach {
                    it.delete().await()
                }

                val postsSnapshot = fireStore.collection("posts")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                for (document in postsSnapshot.documents) {
                    document.reference.delete().await()
                }
                Log.d("UserRepository", "Deleted ${postsSnapshot.documents.size} posts.")


                // xoa users
                fireStore.collection("users").document(userId).delete().await()

                user.delete().await()

                fireStore.terminate()
                Log.d("UserRepository", "User account deleted.")
                Result.success(true)
            } catch (e: Exception) {
                Log.e("UserRepository", "Error deleting user account: ${e.message}", e)
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("Chưa đăng nhập"))
        }
    }

    override suspend fun deletenewAccount(): Boolean {
        val user = auth.currentUser
        return if (user != null) {
            try {
                // xoa users
                val userId = user.uid
                fireStore.collection("users").document(userId).delete().await()

                user.delete().await()
                Log.d("UserRepository", "User account deleted.")
                true
            } catch (e: Exception) {
                Log.e("UserRepository", "Error deleting user account", e)
                false
            }
        } else {
            false
        }
    }

    //dang xuat
    override fun logout() {
        fireStore.terminate()
        auth.signOut()
    }
}