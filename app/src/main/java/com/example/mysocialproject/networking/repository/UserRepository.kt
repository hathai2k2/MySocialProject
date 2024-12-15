package com.example.mysocialproject.networking.repository

import android.net.Uri
import com.example.mysocialproject.ui.feature.model.User

interface UserRepository {
    suspend fun signUp(email: String, password: String): Result<Boolean>
    suspend fun createAvtandNameUser(imageUri: Uri, name: String): Result<Boolean>
    suspend fun login(email: String, password: String): Result<Boolean>
    fun isUserLoggedIn(): Boolean
    fun isAdmin(): Boolean
    suspend fun getInfoUser(): Result<User>
    fun getCurrentUid(): String
    suspend fun forgotPassword(email: String): Result<Boolean>
    suspend fun updateUserAvatarInPosts(userId: String, newAvatarUrl: String)
    suspend fun updateAvatar(imageUri: Uri): Result<Boolean>
    suspend fun updateUserNameInPosts(userId: String, newName: String)
    suspend fun updateName(newName: String): Result<Boolean>
    suspend fun updatePassword(newPassword: String): Result<Boolean>
    suspend fun deleteAccount(): Result<Boolean>
    suspend fun deletenewAccount(): Boolean
    fun logout()
}