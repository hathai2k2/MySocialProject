package com.example.mysocialproject.networking.repository

import android.net.Uri
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.mysocialproject.model.PostData
import com.example.mysocialproject.model.PostResult
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class PostRepositoryImpl  @Inject constructor(
    private val postPagingSource: PostPagingSource,
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,

    ):PostRepository {
    override suspend fun addPost(contentUri: Uri, content: String, isImage: Boolean): PostResult {
        return try {
            // Kiểm tra người dùng
            val currentUID = auth.currentUser?.uid
            if (currentUID == null) {
                return PostResult.Failure("User ID is null")
            }

            val uniqueID = UUID.randomUUID().toString()
            val fileName = "post_${currentUID}_$uniqueID"

            // Lựa chọn thư mục lưu trữ phù hợp với loại file
            val storageReference = if (isImage) {
                storage.reference.child("$currentUID/post_image/$fileName.jpeg")
            } else {
                storage.reference.child("$currentUID/post_voice/$fileName.aac")
            }

            // Upload file lên Firebase Storage
            val uploadTask = storageReference.putFile(contentUri).await()
            val downloadUrl = storageReference.downloadUrl.await()
            Log.d("TAGY", "Download URL obtained: $downloadUrl")

            // Lấy thông tin người dùng từ Firestore
            val docRef = fireStore.collection("users").document(currentUID).get().await()
            val userName = docRef.getString("nameUser")
            val userAvatar = docRef.getString("avatarUser")

            // Tạo đối tượng Post
            val timeStamp = Timestamp(Date())
            val newPostRef = fireStore.collection("posts").document()
            val postId = newPostRef.id

            val post = PostData(
                postId = postId,
                userId = currentUID,
                userName = userName,
                userAvatar = userAvatar,
                content = content,
                imageURL = if (isImage) downloadUrl.toString() else "",
                voiceURL = if (!isImage) downloadUrl.toString() else "",
                createdAt = timeStamp,
                hiddenForUsers = emptyList(),
                viewedBy = emptyList()
            )

            // Lưu bài đăng vào Firestore
            newPostRef.set(post).await()

            // Trả về kết quả thành công
            PostResult.Success(postId = postId)
        } catch (e: FirebaseNetworkException) {
            // Xử lý lỗi mạng
            PostResult.Failure("Network error: ${e.message ?: "Kiểm tra kết nối mạng"}")
        } catch (e: Exception) {
            // Xử lý các lỗi khác
            PostResult.Failure("An error occurred: ${e.message}")
        }
    }


    override fun getPosts(): Flow<PagingData<PostData>> {
        return Pager(
            config = PagingConfig(
                pageSize = 1,
                enablePlaceholders = false,
                prefetchDistance = 20,
                initialLoadSize = 20,
                maxSize = 100,
                jumpThreshold = 10
            ),
            pagingSourceFactory = { PostPagingSource(fireStore, auth) }
        ).flow
    }

    override suspend fun updateViewedBy(postId: String): Boolean {
        val postRef = fireStore.collection("posts").document(postId)
        val currentId = auth.currentUser?.uid ?: return false // Handle no user logged in

        return try {
            postRef.update("viewedBy", FieldValue.arrayUnion(currentId)).await()
            Log.d("PostRepository", "Post $postId viewed by $currentId")
            true
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                Log.e("PostRepository", "Document $postId not found", e)
            } else {
                Log.e("PostRepository", "Error updating viewedBy for $postId", e)
            }
            false
        }
    }
}