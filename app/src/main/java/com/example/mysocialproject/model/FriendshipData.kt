package com.example.mysocialproject.model

import com.google.firebase.Timestamp

data class FriendshipData(
    val id:String?="",
    val uid1: String?="",
    val uid2: String?="",
    val state: String?="",
    val userAvt:String?="",
    val userName:String?="",
    val timeStamp: Timestamp = Timestamp.now()
)
