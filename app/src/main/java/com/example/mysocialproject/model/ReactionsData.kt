package com.example.mysocialproject.model

import com.google.firebase.Timestamp

data class ReactionsData(
    val postId: String = "",
    val userId: String = "",
    val targetId: String = "",
    val emoji: List<String> = listOf(),
    val createdAt: Timestamp?=null,
    var status: ReactionsStatus = ReactionsStatus.NEW
)
enum class ReactionsStatus {
    NEW,
    NOTIFIED
}
