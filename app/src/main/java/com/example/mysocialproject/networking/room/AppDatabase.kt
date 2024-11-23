package com.example.mysocialproject.networking.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mysocialproject.model.room.MessageEntity
import com.example.mysocialproject.model.room.UserEntity

@Database(entities = [MessageEntity::class, UserEntity::class], version = 16)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao
}
