package com.example.mysocialproject.model

import com.google.firebase.Timestamp

data class PostData(
    val postId:String="",
    val userId: String="",
    val userName: String?="",
    val userAvatar: String?="",
    val content: String?="",
    val photoURL: String?="",
    val voiceURL: String?="",
    val createdAt: Timestamp?=null,
    val hiddenForUsers: List<String> = emptyList(),
    val viewByUser: List<String> = emptyList()
)

sealed class PostResult {
    data class Success(val postId: String) : PostResult()
    data class Failure(val error: String) : PostResult()
}
