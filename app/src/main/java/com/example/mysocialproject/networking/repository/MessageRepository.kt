package com.example.mysocialproject.networking.repository

import com.example.mysocialproject.model.room.MessageEntity
import com.example.mysocialproject.networking.room.MessageDao
import javax.inject.Inject

class MessageRepository @Inject constructor(
    private val messageDao: MessageDao
) {

    suspend fun insertMessages(messages: List<MessageEntity>) {
        messageDao.insertMessages(messages)
    }

    suspend fun getMessagesBetween(senderId: String, receiverId: String): List<MessageEntity> {
        return messageDao.getMessagesBetween(senderId, receiverId)
    }
}