package com.example.mysocialproject.model

import com.google.firebase.Timestamp

data class Reaction(
    val postId: String = "",
    val userId: String = "",
    val targetId: String = "",
    val emoji: List<String> = listOf(),
    val createdAt: Timestamp?=null,
    var status: ReactionStatus = ReactionStatus.NEW
)
enum class ReactionStatus {
    NEW,
    NOTIFIED
}