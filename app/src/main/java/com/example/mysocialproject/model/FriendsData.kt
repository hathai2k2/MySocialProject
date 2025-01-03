package com.example.mysocialproject.model

import com.google.firebase.Timestamp

data class FriendsData(
    val id:String?="",
    val uid1: String?="",
    val uid2: String?="",
    val state: String?="",
    val userAvt:String?="",
    val userName:String?="",
    val createdAt: Timestamp = Timestamp.now()
)
