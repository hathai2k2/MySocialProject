package com.example.mysocialproject.networking.repository

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.mysocialproject.model.FriendshipData
import com.example.mysocialproject.model.UserData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@Suppress("DEPRECATION")
class FriendRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val pendingDynamicLinkData: FirebaseDynamicLinks,
):FriendRepository {
    override fun createFriendRequestLink(
        userId: String, userName: String,
        userAvt: Uri
    ): Task<ShortDynamicLink> {
        val deepLink =
            "https://mysocialproject-61c0e.web.app/friendRequest?userId=$userId"

        // Build dynamic link
        val dynamicLink = pendingDynamicLinkData.createDynamicLink()
            .setDomainUriPrefix("https://mysocialproject.page.link")
            .setLink(Uri.parse(deepLink))
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder("com.example.mysocialproject")
                    .build()
            )
            .setSocialMetaTagParameters(
                DynamicLink.SocialMetaTagParameters.Builder()
                    .setTitle("Kết bạn với mình trên SnapMoment 💌")
                    .setDescription("SnapMoment.Story❣️")
                    .setImageUrl(userAvt)
                    .build()
            )
            .setNavigationInfoParameters(
                DynamicLink.NavigationInfoParameters.Builder()
                    .setForcedRedirectEnabled(true)
                    .build()
            )


        return dynamicLink.buildShortDynamicLink()
    }
    //create dynamic link
    override suspend fun createDynamicLink(): String {
        val currentUser = auth.currentUser
        val currentUserId = currentUser?.uid
        return if (currentUserId != null) {
            val docRef = fireStore.collection("users").document(currentUserId)
            val snapshot = docRef.get().await()
            val userName = snapshot.getString("nameUser") ?: ""
            val userAvt = snapshot.getString("avatarUser") ?: ""
            Log.d("ImageAvt", "id = $currentUserId, img = $userAvt")

            val shortDynamicLink =
                createFriendRequestLink(currentUserId, userName, Uri.parse(userAvt)).await()
            shortDynamicLink.shortLink.toString()
        } else {
            throw Exception("User not logged in")
        }
    }
    //query param tu dynamiclink roi/ app link
    override fun handleDynamicLink(intent: Intent, callback: (String?) -> Unit) {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener { pendingDynamicLinkData ->
                val deepLink = pendingDynamicLinkData?.link
                Log.d("DeepLink", "deepLink: $deepLink")
                if (deepLink != null) {
                    val userId = deepLink?.getQueryParameter("userId")
                    Log.d("DeepLink", "userId: $userId")
                    callback(userId)
                }
                else {
                    val appLinkAction: String? = intent.action
                    val appLinkData: Uri? = intent.data
                    if (appLinkAction == Intent.ACTION_VIEW && appLinkData != null) {
                        val userId = appLinkData.getQueryParameter("userId")
                        Log.d("AppLink", "userId: $userId")
                        callback(userId)
                    } else {
                        callback(null)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("LinkHandling", "getDynamicLink:onFailure", e)
                callback(null)
            }
    }
    //lay tt nguoi gui link
    override suspend fun getAvatarUserSendLink(userId: String?): Pair<String?, String?> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null && currentUser.uid != userId) {
                val getdoc =
                    userId?.let { fireStore.collection("users").document(it).get().await() }
                val avtUser = getdoc?.getString("avatarUser")
                val nameUser = getdoc!!.getString("nameUser")
                return Pair(nameUser, avtUser)
            } else {
                return Pair(null, null)
            }

        } catch (e: FirebaseException) {
            return Pair(null, null)
        }
    }

    override suspend fun handleFriendRequest(senderId: String?): Result<FriendshipData> {
        return try {
            val currentUserId = auth.currentUser?.uid

            if (senderId != null && currentUserId != null) {

                val friendCountQueryCurrentUser = fireStore.collection("friendships")
                    .whereEqualTo("uid1", currentUserId)
                    .whereEqualTo("state", "Accepted")
                    .get()
                    .await()
                val friendCountCurrentUser = friendCountQueryCurrentUser.documents.size

                val friendCountQuerySender = fireStore.collection("friendships")
                    .whereEqualTo("uid1", senderId)
                    .whereEqualTo("state", "Accepted")
                    .get()
                    .await()
                val friendCountSender = friendCountQuerySender.documents.size

                val friendCountQueryCurrentUserInUid2 = fireStore.collection("friendships")
                    .whereEqualTo("uid2", currentUserId)
                    .whereEqualTo("state", "Accepted")
                    .get()
                    .await()
                val friendCountCurrentUserInUid2 = friendCountQueryCurrentUserInUid2.documents.size


                val friendCountQuerySenderInUid2 = fireStore.collection("friendships")
                    .whereEqualTo("uid2", senderId)
                    .whereEqualTo("state", "Accepted")
                    .get()
                    .await()
                val friendCountSenderInUid2 = friendCountQuerySenderInUid2.documents.size

                val totalFriendCountCurrentUser = friendCountCurrentUser + friendCountCurrentUserInUid2
                val totalFriendCountSender = friendCountSender + friendCountSenderInUid2

                if (totalFriendCountCurrentUser >= 20 || totalFriendCountSender >= 20) {
                    return Result.failure(Exception("Bạn hoặc người bạn muốn kết bạn đã đạt giới hạn 20 bạn bè"))
                    Log.d("FriendRepository", "Bạn hoặc người bạn muốn kết bạn đã đạt giới hạn 20 bạn bè")
                }


                val existingFriendship = fireStore.collection("friendships")
                    .whereEqualTo("uid1", currentUserId)
                    .whereEqualTo("uid2", senderId)
                    .whereEqualTo("state", "Accepted")
                    .get()
                    .await()
                    .documents
                    .firstOrNull()
                if (existingFriendship != null) {
                    Result.failure(Exception("Đã là bạn bè rồi nhé"))
                } else {

                    val existingPendingFriendshipRef = fireStore.collection("friendships")
                        .whereEqualTo("uid1", currentUserId)
                        .whereEqualTo("uid2", senderId)
                        .whereEqualTo("state", "pending")
                        .get()
                        .await()
                        .documents
                        .firstOrNull()?.reference

                    if (existingPendingFriendshipRef != null) {
                        fireStore.runTransaction { transaction ->
                            transaction.update(existingPendingFriendshipRef, "timeStamp", Timestamp.now())
                        }.await()
                        return Result.failure(Exception("Đã gửi lời mời kết bạn!"))
                    }

                    val getdoc = fireStore.collection("users").document(currentUserId).get().await()
                    val newPostRef = fireStore.collection("friendships").document()
                    val id = newPostRef.id
                    val friendship = fireStore.runTransaction { transaction ->
                        val friendship = FriendshipData(
                            id = id,
                            uid1 = currentUserId,
                            uid2 = senderId,
                            state = "pending",
                            userAvt = getdoc?.getString("avatarUser"),
                            userName = getdoc!!.getString("nameUser"),
                            timeStamp = Timestamp.now()
                        )
                        transaction.set(newPostRef, friendship)
                        friendship
                    }.await()
                    Result.success(friendship)
                }
            } else {
                Result.failure(Exception("Vui lòng đăng nhập!"))
            }
        } catch (e: Exception) {
            // Xử lý lỗi transaction
            Result.failure(e)
        }
    }

    override fun getFriendships(callback: (Result<MutableList<FriendshipData>?>) -> Unit) {
        val currentUserId = auth.currentUser?.uid
            ?: return callback(Result.failure(Exception(" Vui lòng đăng nhập!")))
        fireStore.collection("friendships")
            .whereEqualTo("uid2", currentUserId)
            .whereEqualTo("state", "pending")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    callback(Result.failure(e))
                    return@addSnapshotListener
                }
                val friendships = mutableListOf<FriendshipData>()
                snapshot?.documents?.forEach {
                    val friendship = it.toObject(FriendshipData::class.java)
                    friendship?.let { friendships.add(it) }
                }
                callback(Result.success(friendships))
            }
    }

    override suspend fun updateStateFriendship(state: String, friendshipId: String): Result<FriendshipData?> {
        return try {
            val currentUserId =
                auth.currentUser?.uid ?: return Result.failure(Exception("Vui lòng đăng nhập!"))

            val result = fireStore.runTransaction { transaction ->
                val friendshipRef = fireStore.collection("friendships").document(friendshipId)
                val friendshipSnapshot = transaction.get(friendshipRef)

                if (friendshipSnapshot.exists()) {
                    val currentFriendship = friendshipSnapshot.toObject(FriendshipData::class.java)
                    if (currentFriendship?.state == "pending") {
                        if (state == "Declined") {
                            // Xóa
                            transaction.delete(friendshipRef)
                            null
                        } else if (state == "Accepted") {
                            val updates = mapOf("state" to state)
                            transaction.update(friendshipRef, updates)
                            currentFriendship
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                } else {
                    null
                }
            }.await()

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override fun getFriendAccepted(onSuccess: (MutableList<UserData>) -> Unit, onFailure: (Exception) -> Unit) {
        try {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                Log.d("FriendRepository", "Người dùng chưa đăng nhập")
                return
            }

            Log.d("FriendRepository", "Đang lấy danh sách bạn bè cho người dùng $currentUserId")

            val query1 = fireStore.collection("friendships")
                .whereEqualTo("uid1", currentUserId)
                .whereEqualTo("state", "Accepted")
                .orderBy("timeStamp", Query.Direction.DESCENDING)

            val query2 = fireStore.collection("friendships")
                .whereEqualTo("uid2", currentUserId)
                .whereEqualTo("state", "Accepted")
                .orderBy("timeStamp", Query.Direction.DESCENDING)

            val combinedTask = Tasks.whenAllSuccess<QuerySnapshot>(query1.get(), query2.get())
            combinedTask.addOnSuccessListener { results ->
                val friendIds = mutableListOf<String>()
                for (result in results) {
                    result.documents.forEach { doc ->
                        val uid1 = doc.getString("uid1")
                        val uid2 = doc.getString("uid2")
                        if (uid1 != currentUserId && uid1 != null) {
                            friendIds.add(uid1)
                        } else if (uid2 != currentUserId && uid2 != null) {
                            friendIds.add(uid2)
                        }
                    }
                }

                Log.d("FriendRepository", "Tìm thấy ${friendIds.size} bạn bè")

                if (friendIds.isNotEmpty()) {
                    val friendRefs = friendIds.map { friendId ->
                        fireStore.collection("users").document(friendId)
                    }
                    val friendSnapshots = mutableListOf<UserData>()
                    var count = 0
                    friendRefs.forEach { ref ->
                        ref.get().addOnSuccessListener { friendSnapshot ->
                            friendSnapshot.toObject(UserData::class.java)?.let { friend ->
                                friendSnapshots.add(friend)
                            }
                            count++
                            if (count == friendRefs.size) {
                                onSuccess(friendSnapshots)
                            }
                        }.addOnFailureListener { exception ->
                            onFailure(exception)
                        }
                    }
                } else {
                    Log.d("FriendRepository", "Không có bạn bè nào")
                    onSuccess(mutableListOf())
                }
            }.addOnFailureListener { exception ->
                Log.e("FriendRepository", "Lỗi khi lấy danh sách bạn bè", exception)
                onFailure(exception)
            }
        } catch (e: Exception) {
            Log.e("FriendRepository", "Lỗi khi lấy danh sách bạn bè", e)
            onFailure(e)
        }
    }



    override suspend fun removeFriend(friendId: String, position: Int): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Vui lòng đăng nhập!"))

            val friendsCollection = fireStore.collection("friendships")
            val query1 =
                friendsCollection.whereEqualTo("uid1", currentUserId).whereEqualTo("uid2", friendId)
            val query2 =
                friendsCollection.whereEqualTo("uid1", friendId).whereEqualTo("uid2", currentUserId)

            val batch = fireStore.batch()
            val query1Snapshot = query1.get().await()
            for (docu in query1Snapshot.documents) {
                batch.delete(docu.reference)
            }

            val query2Snapshot = query2.get().await()
            for (docu in query2Snapshot.documents) {
                batch.delete(docu.reference)
            }

            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FriendRepository", "Lỗi khi xóa bạn bè", e)
            Result.failure(e)
        }
    }


    override suspend fun getFriendId(position: Int): Triple<String?, String?, String?>? {
        return try {
            val currentUser = auth.currentUser
            val currentUserId = currentUser?.uid ?: return null

            val friendIds = mutableListOf<String>()
            val friendships1 = fireStore.collection("friendships")
                .whereEqualTo("uid1", currentUserId)
                .whereEqualTo("state", "Accepted")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .get()
                .await()
            friendships1.documents.mapNotNullTo(friendIds) { it.getString("uid2") }

            val friendships2 = fireStore.collection("friendships")
                .whereEqualTo("uid2", currentUserId)
                .whereEqualTo("state", "Accepted")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .get()
                .await()
            friendships2.documents.mapNotNullTo(friendIds) { it.getString("uid1") }

            if (position in friendIds.indices) {
                val friendId = friendIds[position]
                val friendSnapshot = fireStore.collection("users").document(friendId).get().await()
                val friend = friendSnapshot.toObject(UserData::class.java)
                Triple(friendId, friend?.nameUser, friend?.avatarUser)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.d("FriendRepository", "$e")
            null
        }
    }
}