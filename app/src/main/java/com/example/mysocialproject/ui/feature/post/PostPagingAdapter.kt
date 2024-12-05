package com.example.mysocialproject.ui.feature.post

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ItemPostImageBinding
import com.example.mysocialproject.databinding.ItemPostRecordBinding

import com.example.mysocialproject.model.PostData
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.ocpsoft.prettytime.PrettyTime
import rm.com.audiowave.OnProgressListener
import java.io.File
import java.io.IOException
import java.util.Locale


class PostPagingAdapter(
    private val isCurrentUser: (PostData) -> Boolean,
    private val lifecycleOwner: LifecycleOwner,
    private val context: Context,
    private val onPostViewed: (String) -> Unit
) :
    PagingDataAdapter<PostData, RecyclerView.ViewHolder>(POST_Data_COMPARATOR) {

    companion object {
        const val VIEW_TYPE_IMAGE = 0
        const val VIEW_TYPE_VOICE = 1

        private val POST_Data_COMPARATOR = object : DiffUtil.ItemCallback<PostData>() {
            override fun areItemsTheSame(oldItem: PostData, newItem: PostData): Boolean {
                return oldItem.postId == newItem.postId
            }

            override fun areContentsTheSame(oldItem: PostData, newItem: PostData): Boolean {
                return oldItem == newItem
            }
        }
    }


    init {
        addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached) {
                snapshot().items.forEach { post ->
                    prefetchImage(post.imageURL)
                    prefetchImage(post.userAvatar)
                }
            }
        }
    }

    private fun prefetchImage(imageUrl: String?) {
        if (imageUrl != null) {
            Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .onlyRetrieveFromCache(true)
                .preload()
        }
    }

    class ImageViewHolder(val itemBinding: ItemPostImageBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        private var currentLikesLiveData: LiveData<List<Pair<String, List<String>>>>? = null

        fun bind(
            postData: PostData, isCurrentUserForPost: Boolean,
            lifecycleOwner: LifecycleOwner, context: Context
        ) {
            if (postData.userAvatar != null) {
                Glide.with(itemBinding.root.context)
                    .load(postData.userAvatar)
                    .thumbnail(0.25f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.ic_user)
                    .centerCrop()
                    .circleCrop()
                    .transition(DrawableTransitionOptions.withCrossFade(100))
                    .into(itemBinding.imgAvtUserPost)
            } else {
                itemBinding.imgAvtUserPost.setImageResource(R.drawable.ic_user)
            }

            // Tải ảnh bài đăng
            if (postData.imageURL != null) {
                Glide.with(itemBinding.root.context)
                    .load(postData.imageURL)
                    .centerCrop()
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(150)))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.ic_loading)
                    .signature(ObjectKey(postData.postId))
                    .transition(DrawableTransitionOptions.withCrossFade(100))
                    .into(itemBinding.ivPost)
            } else {
                itemBinding.ivPost.setImageResource(R.drawable.ic_loading)
            }
            if (postData.content?.isNotEmpty() == true){
                itemBinding.tvContent.text = postData.content
            }else{
                itemBinding.tvContent.visibility = View.GONE
            }

            Log.d("checkiscurrentposst", "isCurrentUserForPost: \${isCurrentUserForPost}")
            if (isCurrentUserForPost) {

                itemBinding.tvName.text = "Bạn"
                itemBinding.tvActivity.visibility = View.VISIBLE
                currentLikesLiveData?.removeObservers(lifecycleOwner)
//                itemBinding.tvActivity.setOnTouchListener { v, event ->
//                    if (event.action == MotionEvent.ACTION_UP) {
//                        val dialog = LikesBottomSheetDialog(post.postId)
//                        dialog.show(activity.supportFragmentManager, "LikesBottomSheetDialog")
//                    }
//                    true
//                }
                currentLikesLiveData?.observe(lifecycleOwner) { likesData ->
                    if (likesData.isNotEmpty()) {
                        itemBinding.tvActivity.text = "Có \${likesData.size} hoạt động 💖 \n" +
                                "Ấn vào để xem"
                    } else {
                        itemBinding.tvActivity.text = "Không có hoạt động nào ✨"
                    }

                }

            } else {
                itemBinding.tvName.text = postData.userName
                itemBinding.tvActivity.visibility = View.GONE
            }
            val prettyTime = PrettyTime(Locale("vi"))
            val formattedTime = prettyTime.format(postData.createdAt!!.toDate())
            itemBinding.tvTimer.text = formattedTime.replace("cách đây ", "")
                .replace("giây", "vừa xong")

            itemBinding.postxml = postData
        }

    }

    class VoiceViewHolder(val itemBinding: ItemPostRecordBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        private var mediaPlayer = MediaPlayer()
        private val handler = Handler(Looper.getMainLooper())
        private var isplay: Boolean = false
        private var time = 0

        init {
            setupWaveformView()
        }

        private var currentLikesLiveData: LiveData<List<Pair<String, List<String>>>>? = null
        fun bind(
            postData: PostData,
            isCurrentUserForPost: Boolean,
            lifecycleOwner: LifecycleOwner,
        ) {
            if (postData.userAvatar != null) {
                Glide.with(itemBinding.root.context)
                    .load(postData.userAvatar)
                    .thumbnail(0.25f)
                    .centerCrop()
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.ic_user)
                    .transition(DrawableTransitionOptions.withCrossFade(100))
                    .override(100, 100)
                    .into(itemBinding.imgAvtUserPost)
            } else {
                itemBinding.imgAvtUserPost.setImageResource(R.drawable.ic_user)
            }

            postData.voiceURL?.let { url ->
                downloadAudio(url)
            } ?: run {
                Log.e("VoiceViewHolder", "Voice URL is null")
            }

            Log.e("checkiscurrentposst", "isCurrentUserForPost: \${isCurrentUserForPost}")
            if (isCurrentUserForPost) {
                itemBinding.tvNameUserPost.text = "Bạn"

                itemBinding.tvActivity.visibility = View.VISIBLE
                currentLikesLiveData?.removeObservers(lifecycleOwner)
//                itemBinding.tvActivity.setOnTouchListener { v, event ->
//                    if (event.action == MotionEvent.ACTION_UP) {
//                        Log.d("PostPagingAdapter", "Button clicked for post: \${post.postId}")
//                        val dialog = LikesBottomSheetDialog(post.postId)
//                        dialog.show(activity.supportFragmentManager, "LikesBottomSheetDialog")
//                    }
//                    true
//                }
                currentLikesLiveData?.observe(lifecycleOwner) { likesData ->
                    if (likesData.isNotEmpty()) {
                        itemBinding.tvActivity.text =
                            "Có \${likesData.size} hoạt động 💖 \nẤn vào để xem "
                    } else {
                        itemBinding.tvActivity.text = "Không có hoạt động nào ✨"
                    }
                }

            } else {
                itemBinding.tvNameUserPost.text = postData.userName
                itemBinding.tvActivity.visibility = View.GONE
            }
            val prettyTime = PrettyTime(Locale("vi"))
            val formattedTime = prettyTime.format(postData.createdAt!!.toDate())
            itemBinding.timeStamp.text = formattedTime.replace(" trước", "").replace("cách đây ", "")
                .replace("giây", "vừa xong")

            itemBinding.postvoicexml = postData
        }

        private fun downloadAudio(url: String) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url)
                    val localFile = File.createTempFile("audio", "aac")
                    storageRef.getFile(localFile).await()

                    withContext(Dispatchers.Main) {
                        Log.d("VoiceViewHolder", "Audio file downloaded successfully")
                        itemBinding.wave.setRawData(localFile.readBytes())

                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(localFile.absolutePath)
                        mediaPlayer.prepare()

                        Log.d("TAGY", "\${mediaPlayer.duration}")
                        time = (mediaPlayer.duration / 1000).toInt()
                        if (time == 60) {
                            itemBinding.tvTimer.text = "01:00"
                        } else if (time >= 10) {
                            itemBinding.tvTimer.text = "00:${time.toString()}"
                        } else {
                            itemBinding.tvTimer.text = "00:0${time.toString()}"
                        }
                        itemBinding.play.setOnClickListener {

                            if (!isplay) {
                                try {
                                    mediaPlayer.reset()
                                    mediaPlayer.setDataSource(localFile.absolutePath)
                                    mediaPlayer.prepare()
                                    mediaPlayer.start()
                                    mediaPlayer.setOnCompletionListener {
                                        stopAudio()
                                    }
                                    isplay = true
                                    itemBinding.play.text = "Dừng"
                                    handler.post(updateWaveform)
                                } catch (e: IOException) {
                                    Log.e("VoiceViewHolder", "loi", e)
                                }

                            } else {
                                stopAudio()
                            }


                        }
                    }
                } catch (e: Exception) {
                    Log.e("VoiceViewHolder", "loi gi dau xanh", e)
                }
            }
        }

        private fun stopAudio() {
            if (isplay) {
                mediaPlayer.stop()
                mediaPlayer.reset()
                isplay = false
                itemBinding.play.text = "Phát"
                handler.removeCallbacks(updateWaveform)
                itemBinding.wave.progress = 0f
            }
        }

        private val updateWaveform = object : Runnable {
            override fun run() {
                mediaPlayer?.let { player ->
                    if (isplay) {
                        val progress = (player.currentPosition.toFloat() / player.duration) * 100
                        Log.d("TAGY", "\$progress , \${player.duration}")
                        itemBinding.wave.progress = progress
                        handler.postDelayed(this, 1)
                    }

                }
            }
        }

        private fun setupWaveformView() {
            itemBinding.wave.onProgressListener = object : OnProgressListener {
                override fun onProgressChanged(progress: Float, byUser: Boolean) {
                    Log.d("TAG", "Progress set: \$progress, and it's \$byUser that user did this")
                    if (byUser && isplay) {
                        val seekToPosition = (mediaPlayer!!.duration * progress / 100.0).toInt()

                        Log.d("TAG", "Seeking to position: \$seekToPosition")
                        mediaPlayer?.seekTo(seekToPosition)
                    }
                }

                override fun onStartTracking(progress: Float) {
                    Log.d("TAG", "Started tracking from \$progress")
                }

                override fun onStopTracking(progress: Float) {
                    if (isplay) {
                        val seekToPosition = (mediaPlayer!!.duration * progress / 100.0).toInt()
                        Log.d("TAG", "Seeking to position: \$seekToPosition")
                        mediaPlayer?.seekTo(seekToPosition)
                    }
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_IMAGE) {
            val itemBinding = ItemPostImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ImageViewHolder(itemBinding)

        } else {
            val itemBinding =
                ItemPostRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            VoiceViewHolder(itemBinding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val post = getItem(position)
        if (post != null) {
            Log.d("checkiscurrentposst", "in position: \${position}")
            val isCurrentUserForPost = isCurrentUser(post)
            when (holder) {
                is ImageViewHolder -> holder.bind(
                    post,
                    isCurrentUserForPost,
                    lifecycleOwner,
                    context,
                )

                is VoiceViewHolder -> holder.bind(
                    post, isCurrentUserForPost, lifecycleOwner,
                )
            }
            onPostViewed(post.postId)
        } else {
            Toast.makeText(context, "Không còn bài viết nào", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemViewType(position: Int): Int {
        val post = getItem(position)
        return if (post?.imageURL?.isNotEmpty() == true && post.voiceURL.isNullOrEmpty()) {
            VIEW_TYPE_IMAGE
        } else {
            VIEW_TYPE_VOICE
        }
    }

    fun getPostId(position: Int): String? {
        return getItem(position)?.postId
    }

    fun getPostUserId(position: Int): String? {
        return getItem(position)?.userId
    }

    fun getContentFile(position: Int): String? {
        if (getItemViewType(position) == VIEW_TYPE_IMAGE) {
            return getItem(position)?.imageURL
        } else {
            return getItem(position)?.voiceURL
        }
    }

    fun getPost(position: Int): PostData? {
        return getItem(position)
    }

}
