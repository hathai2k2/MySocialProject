package com.example.mysocialproject.ui.feature.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.AppWidgetTarget
import com.example.mysocialproject.R
import com.example.mysocialproject.ui.feature.chat.ItemChatActivity
import com.example.mysocialproject.ui.feature.viewmodel.messWidgetViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ocpsoft.prettytime.PrettyTime
import java.util.Date
import java.util.Locale

class MessageWidget() : AppWidgetProvider(), LifecycleOwner {
    private val viewModel = messWidgetViewModel()

    private lateinit var lifecycleRegistry: LifecycleRegistry

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        viewModel.listenForLatestMessage(context)
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d("PostWidget", "onUpdate called")

        if (context != null && appWidgetManager != null && appWidgetIds != null) {
            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.newmessagewidgetlayout)
                updateWidget(context, appWidgetManager, views, appWidgetId)
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        views: RemoteViews,
        appWidgetId: Int
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.getMessagesFromFriends().collect { messages ->
                val latestMessage = messages.firstOrNull() // Lấy tin nhắn đầu tiên (mới nhất)
                if (latestMessage != null) {
                    val sender = viewModel.getSenderInfo(latestMessage.senderId ?: "")
                    if (sender != null) {
                        views.setTextViewText(R.id.tv_Name_UserPost, sender.nameUser)
                        views.setTextViewText(R.id.tv_message, latestMessage.message)
                        val prettyTime = PrettyTime(Locale("vi"))
                        val formattedTime =
                            prettyTime.format(Date(latestMessage.createdAt?.toLongOrNull() ?: 0L))

                        views.setTextViewText(
                            R.id.createAt,
                            formattedTime.replace(" trước", "").replace("cách đây ", "")
                                .replace("giây", "vừa xong")
                        )
                        // Load ảnh đại diện
                        val avatarImageTarget =
                            AppWidgetTarget(context, R.id.imageView_avatar, views, appWidgetId)
                        Glide.with(context)
                            .asBitmap()
                            .load(sender.avatarUser)
                            .circleCrop()
                            .into(avatarImageTarget)

                        val clickIntent = Intent(context, ItemChatActivity::class.java).apply {
                            putExtra("FRIEND_ID", sender.userId)
                            putExtra("FRIEND_NAME", sender.nameUser)
                            putExtra("FRIEND_AVATAR", sender.avatarUser)
                        }

                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            clickIntent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        views.setOnClickPendingIntent(R.id.layoutwidgetMess, pendingIntent)
                    }

                } else {
                    views.setTextViewText(R.id.tv_Name_UserPost, "Không có tin nhắn mới")
                    views.setTextViewText(R.id.tv_message, "")
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        viewModel.cancelListen()

        if (::lifecycleRegistry.isInitialized) { // Check if initialized
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
}