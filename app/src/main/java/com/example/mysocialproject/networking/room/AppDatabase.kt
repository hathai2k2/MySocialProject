package com.example.mysocialproject.networking.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mysocialproject.model.room.MessageEntity

@Database(entities = [MessageEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}
