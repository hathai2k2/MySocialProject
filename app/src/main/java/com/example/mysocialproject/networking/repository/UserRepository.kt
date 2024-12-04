package com.example.mysocialproject.networking.repository

import android.net.Uri
import com.example.mysocialproject.model.UserData

interface UserRepository {
    suspend fun signUp(email: String, password: String): Result<Boolean>
    suspend fun signIn(email: String, password: String): Result<Boolean>
    suspend fun forgotPassword(email: String): Result<Boolean>
    fun isUserLoggedIn(): Boolean
    fun logout()
    suspend fun updateAvatar(imageUri: Uri): Result<Boolean>
    suspend fun createProfile(imageUri: Uri, name: String): Result<Boolean>
    suspend fun getInfoUser(): Result<UserData>
    suspend fun updateName(newName: String): Result<Boolean>
    suspend fun updatePassword(newPassword: String): Result<Boolean>
    suspend fun checkIfUserFieldsEmpty(userId: String): Boolean
    suspend fun showData(userId: String): String
}