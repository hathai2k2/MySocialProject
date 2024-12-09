package com.example.mysocialproject.networking

import android.content.Intent
import android.net.Uri
import androidx.paging.PagingData
import com.example.mysocialproject.model.FriendshipData
import com.example.mysocialproject.model.PostData
import com.example.mysocialproject.model.PostResult
import com.example.mysocialproject.model.UserData
import com.example.mysocialproject.networking.pref.PrefHelper
import com.example.mysocialproject.networking.repository.FriendRepository
import com.example.mysocialproject.networking.repository.MessageRepository
import com.example.mysocialproject.networking.repository.PostRepository
import com.example.mysocialproject.networking.repository.UserRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.dynamiclinks.ShortDynamicLink
import kotlinx.coroutines.flow.Flow
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

    override suspend fun clearApp() {
        return sharePreference.clearApp()
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

    override fun isAdmin(): Boolean {
        return userRepo.isAdmin()
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

    override suspend fun LogData(userId: String): String {
        return userRepo.LogData(userId)
    }

    override fun getCurrentId(): String {
        return userRepo.getCurrentId()
    }

    override suspend fun deleteAccount(): Result<Boolean> {
        return userRepo.deleteAccount()
    }

    override suspend fun deletenewAccount(): Boolean {
        return userRepo.deletenewAccount()
    }

    override suspend fun addPost(contentUri: Uri, content: String, isImage: Boolean): PostResult {
        return postRepo.addPost(contentUri, content, isImage)
    }

    override fun getPosts(): Flow<PagingData<PostData>> {
        return postRepo.getPosts()
    }

    override suspend fun updateViewedBy(postId: String): Boolean {
        return postRepo.updateViewedBy(postId)
    }

    //FriendRepository
    override fun createFriendRequestLink(
        userId: String,
        userName: String,
        userAvt: Uri
    ): Task<ShortDynamicLink> {
       return friendRepository.createFriendRequestLink(userId, userName, userAvt)
    }

    override suspend fun createDynamicLink(): String {
        return friendRepository.createDynamicLink()
    }

    override fun handleDynamicLink(intent: Intent, callback: (String?) -> Unit) {
        return friendRepository.handleDynamicLink(intent,callback)
    }

    override suspend fun getAvatarUserSendLink(userId: String?): Pair<String?, String?> {
        return friendRepository.getAvatarUserSendLink(userId)
    }

    override suspend fun handleFriendRequest(senderId: String?): Result<FriendshipData> {
        return friendRepository.handleFriendRequest(senderId)
    }

    override fun getFriendships(callback: (Result<MutableList<FriendshipData>?>) -> Unit) {
        return friendRepository.getFriendships { callback-> }
    }

    override suspend fun updateStateFriendship(
        state: String,
        friendshipId: String
    ): Result<FriendshipData?> {
        return friendRepository.updateStateFriendship(state,friendshipId)
    }

    override fun getFriendAccepted(
        onSuccess: (MutableList<UserData>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        return friendRepository.getFriendAccepted(onSuccess,onFailure)
    }

    override suspend fun removeFriend(friendId: String, position: Int): Result<Unit> {
        return friendRepository.removeFriend(friendId,position)
    }

    override suspend fun getFriendId(position: Int): Triple<String?, String?, String?>? {
        return friendRepository.getFriendId(position)
    }

}