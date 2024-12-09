package com.example.mysocialproject.networking.repository

import android.content.Intent
import android.net.Uri
import com.example.mysocialproject.model.FriendshipData
import com.example.mysocialproject.model.UserData
import com.google.android.gms.tasks.Task
import com.google.firebase.dynamiclinks.ShortDynamicLink

interface FriendRepository {
    fun createFriendRequestLink(
        userId: String, userName: String,
        userAvt: Uri
    ): Task<ShortDynamicLink>
    suspend fun createDynamicLink(): String
    fun handleDynamicLink(intent: Intent, callback: (String?) -> Unit)
    suspend fun getAvatarUserSendLink(userId: String?): Pair<String?, String?>
    suspend fun handleFriendRequest(senderId: String?): Result<FriendshipData>
    fun getFriendships(callback: (Result<MutableList<FriendshipData>?>) -> Unit)
    suspend fun updateStateFriendship(state: String, friendshipId: String): Result<FriendshipData?>
    fun getFriendAccepted(onSuccess: (MutableList<UserData>) -> Unit, onFailure: (Exception) -> Unit)
    suspend fun removeFriend(friendId: String, position: Int): Result<Unit>
    suspend fun getFriendId(position: Int): Triple<String?, String?, String?>?
}