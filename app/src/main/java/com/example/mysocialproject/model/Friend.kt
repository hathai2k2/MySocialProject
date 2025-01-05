package com.example.mysocialproject.model

import com.google.firebase.Timestamp

data class Friend(
    val id:String?="",
    val userId1: String?="",
    val userId2: String?="",
    val status: String?="",
    val userAvatar:String?="",
    val userName:String?="",
    val createdAt:Timestamp= Timestamp.now()
)
