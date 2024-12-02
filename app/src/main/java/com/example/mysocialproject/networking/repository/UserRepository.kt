package com.example.mysocialproject.networking.repository

import android.util.Log
import com.example.mysocialproject.model.UserData
import com.example.mysocialproject.model.room.UserEntity
import com.example.mysocialproject.networking.room.UserDao
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


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


    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    ////dang ki tai khoan
    suspend fun signUp(email: String, password: String): Result<Boolean> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()

            val userId = auth.currentUser?.uid
            if (userId != null) {
                val user = UserData(
                    userId = userId,
                    emailUser = email,
                    avatarUser = "",
                    nameUser = "",
                    passwordUser = "",
                )
                fireStore.collection("users").document(userId).set(user).await()
                Result.success(true)
            } else {
                Result.failure(Exception("User ID is null"))
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    ////dang nhap
    suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()

            Result.success(true)
        } catch (e: Exception) {
            when (e) {

                is FirebaseAuthInvalidCredentialsException -> {
                    Log.e("LoginError", "Invalid credentials: ${e.message}")
                    Result.failure(e)
                }

                is FirebaseNetworkException -> {
                    Log.e("LoginError", "Network error: ${e.message}")
                    Result.failure(e)
                }

                else -> {
                    Log.e("LoginError", "Other error: ${e.message}")
                    Result.failure(e)
                }

            }
        }
    }
}