//package com.example.mysocialproject.networking.room
//
//import androidx.room.Dao
//import androidx.room.Insert
//import androidx.room.OnConflictStrategy
//import androidx.room.Query
//import com.example.mysocialproject.model.room.UserEntity
//@Dao
//interface UserDao {
//    @Insert
//    suspend fun insertUser(user: List<UserEntity>)
//
//    @Query("SELECT * FROM user")
//    suspend fun getAllUser():List<UserEntity>
//
//    @Query("SELECT * FROM user WHERE userId = :userId ")
//    suspend fun getUserById(userId:String):UserEntity
//
//    @Query("DELETE FROM user")
//    suspend fun clearUser()
//}