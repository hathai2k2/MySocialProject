package com.example.mysocialproject.networking

import com.example.mysocialproject.model.room.MessageEntity
import java.util.UUID

object FakeDataGenerator {
    fun generateFakeMessages(count: Int): List<MessageEntity> {
        val messages = mutableListOf<MessageEntity>()
        val users = listOf("user_1", "user_2", "user_3") // Danh sách user giả

        for (i in 1..count) {
            val senderId = users.random()
            val receiverId = users.filter { it != senderId }.random() // Đảm bảo người nhận khác người gửi
            messages.add(
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    senderId = senderId,
                    receiverId = receiverId,
                    content = "Message content $i",
                    timestamp = System.currentTimeMillis() - (1000L * i)
                )
            )
        }
        return messages
    }
}
