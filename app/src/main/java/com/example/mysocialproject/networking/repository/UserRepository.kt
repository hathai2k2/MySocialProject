package com.example.mysocialproject.networking.repository

import com.example.mysocialproject.model.room.UserEntity
import com.example.mysocialproject.networking.room.UserDao
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao
) {

    suspend fun addUser(user:List<UserEntity>){
        userDao.insertUser(user)
    }

    suspend fun getAllUser():List<UserEntity>{
        return userDao.getAllUser()
    }

    suspend fun getUserById(userId:String):UserEntity{
        return userDao.getUserById(userId)
    }

}