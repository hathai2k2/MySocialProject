package com.example.mysocialproject.repository

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mysocialproject.model.Post
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class PostPagingSource(
    private val fireStore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : PagingSource<QuerySnapshot, Post>() {

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Post> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return LoadResult.Error(Exception("Vui lòng đăng nhập"))
            }
            val userId = currentUser.uid

            // Lấy danh sách ID bạn bè
            val friendIds = getFriendIds(userId)
            val userIds = mutableListOf(userId)
            userIds.addAll(friendIds)

            // Chia nhỏ userIds thành các nhóm có tối đa 10 ID
            val userIdChunks = userIds.chunked(10)

            // Tạo một danh sách để chứa tất cả các bài viết
            val allPosts = mutableListOf<Post>()

            // Tạo biến currentPage để lưu trữ trang hiện tại
            var currentPage: QuerySnapshot? = null

            // Thực hiện truy vấn cho từng nhóm
            for (chunk in userIdChunks) {
                currentPage = params.key ?: fireStore.collection("posts")
                    .whereIn("userId", chunk)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(params.loadSize.toLong())
                    .get()
                    .await()

                val posts = currentPage!!.documents.mapNotNull { it.toObject(Post::class.java) }
                allPosts.addAll(posts.filter { !it.hiddenForUsers.contains(userId) })
            }

            Log.d("PostPagingSource", "Loaded ${allPosts.size} posts for page: ${params.key}")

            val lastDocument = currentPage?.documents?.lastOrNull()
            val nextKey = lastDocument?.let {
                val nextChunk = userIdChunks.find { chunk ->
                    chunk.contains(it.getString("userId"))
                }
                nextChunk?.let {
                    fireStore.collection("posts")
                        .whereIn("userId", it)
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .limit(params.loadSize.toLong())
                        .startAfter(lastDocument)
                        .get()
                        .await()
                }
            }

            LoadResult.Page(
                data = allPosts,
                prevKey = null, // Không hỗ trợ phân trang ngược
                nextKey = nextKey
            )

        } catch (e: Exception) {
            Log.e("PostPagingSource", "Error loading posts", e)
            LoadResult.Error(e)
        }
    }

    override val jumpingSupported: Boolean
        get() = true

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Post>): QuerySnapshot? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
        }
    }

    private suspend fun getFriendIds(userId: String): List<String> {
        return try {
            val query1 = fireStore.collection("friends")
                .whereEqualTo("userId1", userId)
                .whereEqualTo("status", "Accepted")

            val query2 = fireStore.collection("friends")
                .whereEqualTo("userId2", userId)
                .whereEqualTo("status", "Accepted")

            val combinedTask = Tasks.whenAllSuccess<QuerySnapshot>(query1.get(), query2.get())
                .await()

            val friendIds = mutableSetOf<String>() // dùng set thay list để tránh trùng lặp
            for (result in combinedTask) {
                result.documents.forEach { doc ->
                    val uid1 = doc.getString("userId1")
                    val uid2 = doc.getString("userId2")
                    if (uid1 != userId && uid1 != null) {
                        friendIds.add(uid1)
                    }
                    if (uid2 != userId && uid2 != null) {
                        friendIds.add(uid2)
                    }
                }
            }

            friendIds.toList()
        } catch (e: Exception) {
            Log.e("PostPagingSource", "Error fetching friend IDs", e)
            emptyList()
        }
    }
}
