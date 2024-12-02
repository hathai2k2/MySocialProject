package com.example.mysocialproject.model

data class MessageData(
    val messageId: String? = "",
    val senderId: String? = "",
    val receiverId: String? = "",
    val message: String? = "",
    val postId: String = "",
    val imageUrl: String? = "",
    val voiceUrl: String?= "",
    val timestamp: String? = "",
    val content: String? = "",
    val avtpost:String? = "",
    val createdAt: String? = "",
    var status: MessageStatus = MessageStatus.SENDING
)
enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}