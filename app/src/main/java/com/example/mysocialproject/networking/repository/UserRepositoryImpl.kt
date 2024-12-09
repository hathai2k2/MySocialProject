package com.example.mysocialproject.networking.repository

import android.net.Uri
import android.util.Log
import com.example.mysocialproject.model.PostData
import com.example.mysocialproject.model.UserData
import com.example.mysocialproject.model.room.UserEntity
import com.example.mysocialproject.networking.room.UserDao
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.net.URLDecoder
import java.util.UUID
import javax.inject.Inject


class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) : UserRepository {

    suspend fun addUser(user: List<UserEntity>) {
        userDao.insertUser(user)
    }

    suspend fun getAllUser(): List<UserEntity> {
        return userDao.getAllUser()
    }

    suspend fun getUserById(userId: String): UserEntity {
        return userDao.getUserById(userId)
    }


    ////dang ki tai khoan
    override suspend fun signUp(email: String, password: String): Result<Boolean> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()

            val userId = auth.currentUser?.uid
            if (userId != null) {
                val user = UserData(
                    userId = userId,
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

    // Hàm lấy userId của người dùng hiện tại
    fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun LogData(userId: String): String {
        return try {
            val userDocument = fireStore.collection("users").document(userId).get().await()
            val user = userDocument.toObject(UserData::class.java)


            "userId:$userId \n avatarUser:${user?.avatarUser} \n nameUser:${user?.nameUser}"
        } catch (e: Exception) {
            "no data" // Nếu có lỗi thì mặc định là có vấn đề với trường dữ liệu
        }
    }

    override fun getCurrentId(): String {
        return auth.currentUser?.uid ?: ""
    }

    // Hàm kiểm tra avatarUser và nameUser có rỗng không
    override suspend fun checkIfUserFieldsEmpty(userId: String): Boolean {
        return try {
            val userDocument = fireStore.collection("users").document(userId).get().await()
            val user = userDocument.toObject(UserData::class.java)

            // Kiểm tra avatarUser và nameUser có rỗng không
            user?.avatarUser.isNullOrEmpty() && user?.nameUser.isNullOrEmpty()
        } catch (e: Exception) {
            false // Nếu có lỗi thì mặc định là có vấn đề với trường dữ liệu
        }
    }

    ////dang nhap
    override suspend fun signIn(email: String, password: String): Result<Boolean> {
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

    override suspend fun forgotPassword(email: String): Result<Boolean> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(true)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    ///kiem tra dang nhap
    override fun isUserLoggedIn(): Boolean {
        val current = auth.currentUser
        return current != null && current.uid != "KyH8KCgh0pgxFiPXvr76xh5Vux03"
    }
    override fun isAdmin(): Boolean {
        val current = auth.currentUser
        return current != null && current.uid == "KyH8KCgh0pgxFiPXvr76xh5Vux03"
    }
    //dang xuat
    override fun logout() {
        fireStore.terminate()
        auth.signOut()
    }

    override suspend fun updateAvatar(imageUri: Uri): Result<Boolean> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Chưa đăng nhập"))
            val downloadUrl = uploadAvatarToStorage(imageUri, user.uid)
            updateAvatarUrlInFirestore(user.uid, downloadUrl)

            // Cập nhật ảnh trong các bài viết (nếu cần)
            updateUserAvatarInPosts(user.uid, downloadUrl)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun updateUserAvatarInPosts(userId: String, newAvatarUrl: String) {
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
    private suspend fun uploadAvatarToStorage(imageUri: Uri, userId: String): String {
        val uniqueID = UUID.randomUUID().toString()
        val fileName = "avt_$userId$uniqueID"
        val storageReference = storage.reference.child("$userId/avatar/$fileName")

        // Upload ảnh lên Firebase Storage
        storageReference.putFile(imageUri).await()

        // Lấy URL tải về của ảnh vừa upload
        return storageReference.downloadUrl.await().toString()
    }

    private suspend fun updateAvatarUrlInFirestore(userId: String, downloadUrl: String) {
        val updates = mapOf("avatarUser" to downloadUrl)
        fireStore.collection("users").document(userId).update(updates).await()
    }


    /// dang ki ten va anh dai dien
    override suspend fun createProfile(imageUri: Uri, name: String): Result<Boolean> {
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

    private var isDataLoaded = false
    private var cachedUser: UserData? = null

    override suspend fun getInfoUser(): Result<UserData> {
        if (isDataLoaded) {
            return getCachedUser() // Trả về cached user nếu dữ liệu đã được load
        }

        return try {
            val currentUser =
                auth.currentUser ?: return Result.failure(Exception("Current user is null"))

            // Fetch user information from Firestore
            val user = fetchUserFromFirestore(currentUser.uid)
            return if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User document does not exist"))
            }
        } catch (e: Exception) {
            Log.e("getInfoUser", "Exception: ${e.message}")
            Result.failure(e)
        }
    }

    // Trả về user đã được lưu trong bộ nhớ cache hoặc thất bại nếu không có
    private fun getCachedUser(): Result<UserData> {
        return cachedUser?.let {
            Result.success(it)
        } ?: Result.failure(Exception("No cached data"))
    }

    // Fetch user từ Firestore
    private suspend fun fetchUserFromFirestore(userId: String): UserData? {
        val userDocumentRef = fireStore.collection("users").document(userId)

        val userSnapshot = userDocumentRef.get().await()
        if (userSnapshot.exists()) {
            val user = userSnapshot.toObject(UserData::class.java)
            if (user != null) {
                cacheUserData(user) // Lưu dữ liệu vào cache
            }
            return user
        }

        return null
    }

    // Lưu dữ liệu người dùng vào bộ nhớ cache và đánh dấu là đã load
    private fun cacheUserData(user: UserData) {
        isDataLoaded = true
        cachedUser = user
        Log.d("TAGY", "User data cached: $user")
    }

    override suspend fun updateName(newName: String): Result<Boolean> {
        val user = auth.currentUser
        if (user == null) {
            return Result.failure(Exception("User is not logged in"))
        }

        return try {
            val userId = user.uid
            val userDocument = fireStore.collection("users").document(userId)

            // Cập nhật tên người dùng trong Firestore
            updateUserNameInFirestore(userDocument, newName)

            // Cập nhật tên người dùng trong bài viết của họ
//            updateUserNameInPosts(userId, newName)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateUserNameInFirestore(docRef: DocumentReference, newName: String) {
        docRef.update("nameUser", newName).await()
    }

    override suspend fun updatePassword(newPassword: String): Result<Boolean> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Chưa đăng nhập"))

            // Kiểm tra mật khẩu có hợp lệ (ví dụ độ dài, ký tự đặc biệt, etc.)
            if (!isValidPassword(newPassword)) {
                return Result.failure(Exception("Mật khẩu không hợp lệ"))
            }

            // Cập nhật mật khẩu cho user
            user.updatePassword(newPassword).await()  // Chờ task hoàn thành

            Result.success(true)  // Thành công
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            // Nếu yêu cầu đăng nhập lại
            Result.failure(Exception("Vui lòng đăng nhập lại trước khi đổi mật khẩu"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            // Nếu mật khẩu không hợp lệ
            Result.failure(Exception("Mật khẩu không hợp lệ"))
        } catch (e: FirebaseNetworkException) {
            // Lỗi kết nối mạng
            Result.failure(Exception("Lỗi kết nối, vui lòng kiểm tra lại mạng"))
        } catch (e: Exception) {
            // Xử lý các lỗi chung khác
            Result.failure(Exception("Đã xảy ra lỗi, vui lòng thử lại"))
        }
    }

    // Hàm kiểm tra mật khẩu hợp lệ (ví dụ: ít nhất 6 ký tự)
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6  // Kiểm tra điều kiện mật khẩu
    }


    override suspend fun deleteAccount():Result<Boolean>  {
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
                Log.d("UserRepository", "Deleted ${friendshipsSnapshot.documents.size} friendships (uid1).")
                val friendshipsSnapshot2 = fireStore.collection("friendships")
                    .whereEqualTo("uid2", userId)
                    .get()
                    .await()

                for (document in friendshipsSnapshot2.documents) {
                    document.reference.delete().await()
                }
                Log.d("UserRepository", "Deleted ${friendshipsSnapshot2.documents.size} friendships (uid2).")


                val storageRef = storage.reference.child(userId)
                storageRef.child("avatar").listAll().await().items.forEach { it.delete().await() }
                storageRef.child("post_voice").listAll().await().items.forEach { it.delete().await() }
                storageRef.child("post_image").listAll().await().items.forEach { it.delete().await() }

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
}