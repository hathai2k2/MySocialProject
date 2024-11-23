package com.example.mysocialproject.networking.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mysocialproject.model.room.MessageEntity
@Dao
interface MessageDao {

    @Insert
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("SELECT * FROM messages WHERE senderId = :senderId AND receiverId = :receiverId ORDER BY timestamp ASC")
    suspend fun getMessagesBetween(senderId: String, receiverId: String): List<MessageEntity>

    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()
}