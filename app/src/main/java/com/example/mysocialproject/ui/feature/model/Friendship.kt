package com.example.mysocialproject.ui.feature.model

import com.google.firebase.Timestamp

data class Friendship(
    val id:String?="",
    val uid1: String?="",
    val uid2: String?="",
    val state: String?="",
    val userAvt:String?="",
    val userName:String?="",
    val timeStamp:Timestamp= Timestamp.now()
)
