package com.example.mysocialproject.model.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val userId:String,
    val username:String
)
