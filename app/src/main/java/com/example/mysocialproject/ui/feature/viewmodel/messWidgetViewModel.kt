package com.example.mysocialproject.ui.feature.viewmodel

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.RemoteViews
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.AppWidgetTarget
import com.example.mysocialproject.R
import com.example.mysocialproject.model.Message
import com.example.mysocialproject.model.User
import com.example.mysocialproject.ui.feature.widget.MessageWidget
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.ocpsoft.prettytime.PrettyTime
import java.util.Date
import java.util.Locale

class messWidgetViewModel() : ViewModel() {
    private val fireStore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    private var listenJob: Job? = null


    fun getMessagesFromFriends(): Flow<List<Message>> = callbackFlow {
        val listenerRegistration = fireStore.collection("messages")
            .whereEqualTo("receiverId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }
                val messages = snapshot?.toObjects(Message::class.java) ?: emptyList()
                val decodedMessages = messages.map { message ->
                    try {
                        val decodedMessage =
                            String(Base64.decode(message.message, Base64.NO_WRAP), Charsets.UTF_8)
                        message.copy(message = decodedMessage)
                    } catch (e: IllegalArgumentException) {
                        Log.e(
                            "MessageDecodeError",
                            "Failed to decode message ID: ${message.messageId}, message: ${message.message}",
                            e
                        )
                        message
                    }
                }
                trySend(decodedMessages)
            }
        awaitClose { listenerRegistration.remove() }
    }

    fun listenForLatestMessage(context: Context) {
        listenJob?.cancel()
        listenJob = CoroutineScope(Dispatchers.Main).launch {
            val userId = auth.currentUser?.uid ?: return@launch
            getMessagesFromFriends()
                .collect { messages ->
                    val latestMessage = messages.maxByOrNull { it.createdAt.toString() }
                    if (latestMessage != null) {
                        viewModelScope.launch {
                            val sender = getSenderInfo(latestMessage.senderId ?: "")
                            updateWidgetWithLatestMessage(context, latestMessage, sender)
                        }
                    } else {
                        updateWidgetWithLatestMessage(context, null, null)
                    }
                }
        }
    }

    suspend fun getSenderInfo(senderId: String): User? {
        return try {
            fireStore.collection("users").document(senderId).get().await()
                .toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("messWidgetViewModel", "Error fetching sender info", e)
            null
        }
    }

    private fun updateWidgetWithLatestMessage(context: Context, message: Message?, sender: User?) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, MessageWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.newmessagewidgetlayout)
            if (message != null && sender != null) {
                views.setTextViewText(R.id.tv_Name_UserPost, sender.nameUser)
                views.setTextViewText(R.id.tv_message, message.message.toString())

                val prettyTime = PrettyTime(Locale("vi"))
                val formattedTime = prettyTime.format(Date(message.createdAt?.toLongOrNull() ?: 0L))

                views.setTextViewText(
                    R.id.createAt, formattedTime.replace(" trước", "").replace("cách đây ", "")
                        .replace("giây", "vừa xong")
                )

                val avatarImageTarget =
                    AppWidgetTarget(context, R.id.imageView_avatar, views, appWidgetId)
                Glide.with(context)
                    .asBitmap()
                    .load(sender.avatarUser)
                    .circleCrop()
                    .into(avatarImageTarget)


            } else {
                views.setTextViewText(R.id.tv_Name_UserPost, "")
                views.setTextViewText(R.id.tv_Caption_Post, "Không có tin nhắn mới")
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }


    fun cancelListen() {
        listenJob?.cancel()
    }
}