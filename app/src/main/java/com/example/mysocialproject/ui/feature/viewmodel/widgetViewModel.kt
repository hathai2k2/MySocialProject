package com.example.mysocialproject.ui.feature.viewmodel

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.AppWidgetTarget
import com.example.mysocialproject.R
import com.example.mysocialproject.ui.feature.model.Post
import com.example.mysocialproject.ui.feature.widget.PostWidget
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.ocpsoft.prettytime.PrettyTime
import java.util.Locale

class widgetViewModel : ViewModel() {
    private val fireStore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val postsRef = fireStore.collection("posts")

    private var listenJob: Job? = null

    private suspend fun getFriendIds(): List<String> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val query1 = fireStore.collection("friendships")
                .whereEqualTo("uid1", userId)
                .whereEqualTo("state", "Accepted")

            val query2 = fireStore.collection("friendships")
                .whereEqualTo("uid2", userId)
                .whereEqualTo("state", "Accepted")

            val combinedTask = Tasks.whenAllSuccess<QuerySnapshot>(query1.get(), query2.get())
                .await()

            val friendIds = mutableSetOf<String>()
            for (result in combinedTask) {
                result.documents.forEach { doc ->
                    val uid1 = doc.getString("uid1")
                    val uid2 = doc.getString("uid2")
                    if (uid1 != userId && uid1 != null) {
                        friendIds.add(uid1)
                    }
                    if (uid2 != userId && uid2 != null) {
                        friendIds.add(uid2)
                    }
                }
            }
            friendIds.toList()
        } catch (e: Exception) {
            Log.e("PostPagingSource", "Error fetching friend IDs", e)
            emptyList()
        }
    }

    suspend fun getLatestPostFromFriends(): Post? {
        val friendIds = getFriendIds()
        return try {
            postsRef
                .whereIn("userId", friendIds)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.toObject(Post::class.java)
        } catch (e: Exception) {
            Log.e("PostPagingSource", "Error fetching latest post", e)
            null
        }
    }

    fun listenForUpdates(context: Context) {
        listenJob?.cancel()
        listenJob = CoroutineScope(Dispatchers.Main).launch {
            postsRef
                .whereIn("userId", getFriendIds())
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("WidgetViewModel", "Listen failed: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val firstPost = snapshot.documents.first().toObject(Post::class.java)
                        if (firstPost?.imageURL != "") {
                            updateWidgetWithPost(context, firstPost)
                        }
                    } else {
                        updateWidgetWithPost(context, null)
                    }
                }
        }
    }

    private fun updateWidgetWithPost(context: Context, post: Post?) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, PostWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.postwidgetlayout)
            if (post != null) {
                views.setTextViewText(R.id.tv_Name_UserPost, post.userName)
                views.setTextViewText(R.id.tv_Caption_Post, post.content)
                val prettyTime = PrettyTime(Locale("vi"))
                val formattedTime = prettyTime.format(post.createdAt!!.toDate())





                views.setTextViewText(
                    R.id.createAtpost, formattedTime.replace(" trước", "").replace("cách đây ", "")
                        .replace("giây", "vừa xong")
                )
                // Load ảnh bài đăng
                val postImageTarget =
                    AppWidgetTarget(context, R.id.imageView_post, views, appWidgetId)
                Glide.with(context)
                    .asBitmap()
                    .load(post.imageURL)
                    .apply(
                        RequestOptions.bitmapTransform(
                            RoundedCorners(16)
                        )
                    )
                    .into(postImageTarget)

                // Load ảnh đại diện
                val avatarImageTarget =
                    AppWidgetTarget(context, R.id.imageView_avatar, views, appWidgetId)
                Glide.with(context)
                    .asBitmap()
                    .load(post.userAvatar)
                    .circleCrop()
                    .into(avatarImageTarget)
            } else {
                // Xử lý trường hợp không có bài đăng
                views.setTextViewText(R.id.tv_Name_UserPost, "Không có bài đăng")
                views.setTextViewText(R.id.tv_Caption_Post, "")
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    fun cancelListen() {
        listenJob?.cancel()
    }
}
