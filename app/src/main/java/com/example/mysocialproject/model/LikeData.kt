package com.example.mysocialproject.model

import com.google.firebase.Timestamp

data class LikeData(
    val postId: String = "",
    val userId: String = "",
    val ownerId: String = "",
    val reactions: List<String> = listOf(),
    val createdAt: Timestamp?=null,
    var status: LikeStatus = LikeStatus.NEW
)
enum class LikeStatus {
    NEW,
    NOTIFIED
}
