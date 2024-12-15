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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.AppWidgetTarget
import com.example.mysocialproject.R
import com.example.mysocialproject.ui.feature.post.PostList
import com.example.mysocialproject.ui.feature.viewmodel.widgetViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ocpsoft.prettytime.PrettyTime
import java.util.Locale

class PostWidget() : AppWidgetProvider(), LifecycleOwner {
    private val viewModel = widgetViewModel()

    private lateinit var lifecycleRegistry: LifecycleRegistry

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        viewModel.listenForUpdates(context)
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
                val views = RemoteViews(context.packageName, R.layout.postwidgetlayout)
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
            val post = viewModel.getLatestPostFromFriends()
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
                val postImageTarget = AppWidgetTarget(
                    context,
                    R.id.imageView_post, views, appWidgetId
                )
                Glide.with(context)
                    .asBitmap()
                    .load(post.imageURL)
                    .apply(
                        RequestOptions.bitmapTransform(
                            RoundedCorners(16)
                        )
                    )
                    .into(postImageTarget)

                val avatarImageTarget = AppWidgetTarget(
                    context,
                    R.id.imageView_avatar, views, appWidgetId
                )
                Glide.with(context)
                    .asBitmap()
                    .load(post.userAvatar)
                    .circleCrop()
                    .into(avatarImageTarget)

                val clickIntent = Intent(context, PostList::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    clickIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                views.setOnClickPendingIntent(R.id.layoutwidgetpost, pendingIntent)

            } else {

                views.setTextViewText(R.id.tv_Name_UserPost, "Không có bài đăng")
                views.setTextViewText(R.id.tv_Caption_Post, "")
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
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
