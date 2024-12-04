package com.example.mysocialproject.networking

import android.net.Uri
import com.example.mysocialproject.model.UserData
import com.example.mysocialproject.networking.pref.PrefHelper
import com.example.mysocialproject.networking.repository.FriendRepository
import com.example.mysocialproject.networking.repository.MessageRepository
import com.example.mysocialproject.networking.repository.PostRepository
import com.example.mysocialproject.networking.repository.UserRepository
import javax.inject.Inject

class AppDataManager @Inject constructor(
    private val sharePreference: PrefHelper,
    private val userRepo: UserRepository,
    private val postRepo: PostRepository,
    private val messageRepo: MessageRepository,
    private val friendRepository: FriendRepository,
) : AppDataHelper {
    override suspend fun setUserId(id: String?) {
        sharePreference.setUserId(id)
    }

    override suspend fun getUserId(): String? {
        return sharePreference.getUserId()
    }

    //userRepository
    override suspend fun signUp(email: String, password: String): Result<Boolean> {
        return userRepo.signUp(email, password)
    }

    override suspend fun signIn(email: String, password: String): Result<Boolean> {
        return userRepo.signIn(email, password)
    }

    override suspend fun forgotPassword(email: String): Result<Boolean> {
        return userRepo.forgotPassword(email)
    }

    override fun isUserLoggedIn(): Boolean {
        return userRepo.isUserLoggedIn()
    }

    override fun logout() {
        return userRepo.logout()
    }

    override suspend fun updateAvatar(imageUri: Uri): Result<Boolean> {
        return userRepo.updateAvatar(imageUri)
    }

    override suspend fun createProfile(imageUri: Uri, name: String): Result<Boolean> {
        return userRepo.createProfile(imageUri, name)
    }

    override suspend fun getInfoUser(): Result<UserData> {
        return userRepo.getInfoUser()
    }

    override suspend fun updateName(newName: String): Result<Boolean> {
        return userRepo.updateName(newName)
    }

    override suspend fun updatePassword(newPassword: String): Result<Boolean> {
        return userRepo.updatePassword(newPassword)
    }

    override suspend fun checkIfUserFieldsEmpty(userId: String): Boolean {
        return userRepo.checkIfUserFieldsEmpty(userId)
    }

    override suspend fun showData(userId: String): String {
        return userRepo.showData(userId)
    }

}