package com.example.mysocialproject.networking.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject

class PostRepositoryImpl  @Inject constructor(
    private val postPagingSource: PostPagingSource,
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,

    ):PostRepository {


}