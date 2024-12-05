package com.example.mysocialproject.networking.repository

import android.net.Uri
import androidx.paging.PagingData
import com.example.mysocialproject.model.PostData
import com.example.mysocialproject.model.PostResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface PostRepository{
    suspend fun addPost(contentUri: Uri, content: String, isImage: Boolean): PostResult
    fun getPosts(): Flow<PagingData<PostData>>
    suspend fun updateViewedBy(postId: String): Boolean
}