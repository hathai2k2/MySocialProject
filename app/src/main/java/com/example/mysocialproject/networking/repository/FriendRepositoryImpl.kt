package com.example.mysocialproject.networking.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject

@Suppress("DEPRECATION")
class FriendRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val pendingDynamicLinkData: FirebaseDynamicLinks,
):FriendRepository {
}