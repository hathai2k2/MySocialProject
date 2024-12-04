package com.example.mysocialproject.networking.repository

import com.example.mysocialproject.model.room.MessageEntity
import com.example.mysocialproject.networking.room.MessageDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
):MessageRepository {

    suspend fun insertMessages(messages: List<MessageEntity>) {
        messageDao.insertMessages(messages)
    }

    suspend fun getMessagesBetween(senderId: String, receiverId: String): List<MessageEntity> {
        return messageDao.getMessagesBetween(senderId, receiverId)
    }
}