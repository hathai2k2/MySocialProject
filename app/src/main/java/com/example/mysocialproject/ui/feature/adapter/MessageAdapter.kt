package com.example.mysocialproject.ui.feature.adapter

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ItemReceiMessBinding
import com.example.mysocialproject.databinding.ItemSendMessBinding
import com.example.mysocialproject.model.Message
import com.example.mysocialproject.model.MessageStatus
import com.example.mysocialproject.ui.feature.viewmodel.MessageViewModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.internal.format
import org.ocpsoft.prettytime.PrettyTime
import java.io.File
import java.io.IOException
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

class MessageAdapter(
    private val userId: String,
    private val image: String,
    private var messages: List<Message>,
    private val messageViewModel: MessageViewModel,
    private val lifecycle: LifecycleOwner
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_SENT = 0
        const val VIEW_TYPE_RECEIVED = 1
        const val TIME_STEP = 40 * 60 * 1000
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages.isNotEmpty() && messages[position].senderId == userId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val binding = ItemSendMessBinding.inflate(inflater, parent, false)
                SentMessageViewHolder(binding)
            }

            VIEW_TYPE_RECEIVED -> {
                val binding = ItemReceiMessBinding.inflate(inflater, parent, false)
                ReceivedMessageViewHolder(binding)
            }

            else -> throw IllegalArgumentException("loi khong the inflate")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val previousMessage = if (position > 0) messages[position - 1] else null
        val showTime = shouldShowTime(message, previousMessage)
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message, image, showTime)
            is ReceivedMessageViewHolder -> holder.bind(
                message,
                image,
                showTime,
                messageViewModel,
                lifecycle
            )
        }
    }

    private fun shouldShowTime(currentMessage: Message, previousMessage: Message?): Boolean {
        if (previousMessage == null) return true
        val currentTimestamp = currentMessage.createdAt?.toLongOrNull() ?: return false
        val previousTimestamp = previousMessage.createdAt?.toLongOrNull() ?: return true
        return (currentTimestamp - previousTimestamp) > TIME_STEP
    }

    override fun getItemCount() = messages.size

    class SentMessageViewHolder(private val binding: ItemSendMessBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var mediaPlayer = MediaPlayer()
        private val handler = Handler(Looper.getMainLooper())
        private var isplay: Boolean = false
        fun bind(message: Message, image: String, showTime: Boolean) {

            if (message.photoUrl == "") {
                binding.comment.visibility = View.GONE
            } else {
                binding.comment.visibility = View.VISIBLE
                Glide.with(binding.root).load(message.photoUrl).into(binding.imageViewPost)
                Glide.with(binding.root).load(message.avtpost).into(binding.avtpost)
                if (message.content == "") {
                    binding.tvDescPost.visibility = View.GONE
                }
                if (message.avtpost == "") {
                    binding.avtpost.visibility = View.GONE
                }
                if (message.timestamp == "") {
                    binding.timeImg.visibility = View.GONE
                }

            }

            if (message.voiceUrl == "") {
                binding.voiceCmt.visibility = View.GONE
            } else {
                binding.voiceCmt.visibility = View.VISIBLE

                Glide.with(binding.root).load(message.avtpost).into(binding.avtuser)
                message.voiceUrl?.let { downloadAudio(it) }
                if (message.content == "") {
                    binding.txtcontent.visibility = View.GONE
                }
                if (message.avtpost == "") {
                    binding.avtuser.visibility = View.GONE
                }
                if (message.timestamp == "") {
                    binding.txttime.visibility = View.GONE
                }
            }

            if (message.message?.trim()?.isNotEmpty() == true) {
                binding.messagetxt.visibility = View.VISIBLE
            } else {
                binding.messagetxt.visibility = View.GONE
            }


            if (showTime) {
                binding.tvCreatedAt.text =
                    "-" + formatTimestamp(message.createdAt.toString()) + "-"
                binding.tvCreatedAt.visibility = View.VISIBLE
            } else {
                binding.tvCreatedAt.visibility = View.GONE
            }



            binding.root.setOnClickListener {
                binding.subCreateAt.visibility = View.VISIBLE
                val prettyTime = PrettyTime(Locale("vi"))
                val formattedTime = prettyTime.format(Date(message.createdAt?.toLong() ?: 0))


                binding.subCreateAt.text =
                    formattedTime.replace(" trước", "").replace("cách đây ", "")
                        .replace("giây", "vừa xong")

                binding.state.visibility = View.VISIBLE

                val messageStatus = when (message.status) {
                    MessageStatus.SENDING -> "Đang gửi"
                    MessageStatus.SENT -> "Đã gửi"
                    MessageStatus.DELIVERED -> "Đã nhận"
                    else -> "Đã xem"
                }

                binding.state.text = messageStatus
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.subCreateAt.visibility = View.GONE
                    binding.state.visibility = View.GONE
                }, 2000)
            }
            binding.chat = message
            binding.executePendingBindings()
        }

        fun formatTimestamp(timestamp: String): String {
            return try {
                val timeInMillis = timestamp.toLong()
                val date = Date(timeInMillis)
                val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                format.format(date)
            } catch (e: Exception) {
                "Invalid date"
            }
        }

        private fun downloadAudio(url: String) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url)
                    val localFile = File.createTempFile("audio", "aac")
                    storageRef.getFile(localFile).await()

                    withContext(Dispatchers.Main) {
                        Log.d("VoiceViewHolder", "Audio file downloaded successfully")
                        binding.wave.setRawData(localFile.readBytes())

                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(localFile.absolutePath)
                        mediaPlayer.prepare()
                    }
//                        binding.tvTimer.text = formatTime(time)
                    binding.play.setOnClickListener {

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
                                binding.play.text = "dừng"
                                handler.post(updateWaveform)
                            } catch (e: IOException) {
                                Log.e("VoiceViewHolder", "loi", e)
                            }

                        } else {
                            stopAudio()
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
                binding.play.text = "phát"
                handler.removeCallbacks(updateWaveform)
                binding.wave.progress = 0f
            }
        }

        private val updateWaveform = object : Runnable {
            override fun run() {
                mediaPlayer?.let { player ->
                    if (isplay) {
                        val progress = (player.currentPosition.toFloat() / player.duration) * 100
                        Log.d("TAG", "$progress , ${player.duration}")
                        binding.wave.progress = progress
                        handler.postDelayed(this, 1)
                    }

                }
            }
        }
    }

    class ReceivedMessageViewHolder(private val binding: ItemReceiMessBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var mediaPlayer = MediaPlayer()
        private val handler = Handler(Looper.getMainLooper())
        private var isplay: Boolean = false
        fun bind(
            message: Message,
            image: String,
            showTime: Boolean,
            messageViewModel: MessageViewModel,
            lifecycle: LifecycleOwner
        ) {
            if (message.photoUrl == "") {
                binding.comment.visibility = View.GONE
            } else {
                binding.comment.visibility = View.VISIBLE
                Glide.with(binding.root).load(message.avtpost).into(binding.avtpost)
                Glide.with(binding.root).load(message.photoUrl).into(binding.imageViewPost)
                if (message.content == "") {
                    binding.tvDescPost.visibility = View.GONE
                }
                if (message.avtpost == "") {
                    binding.avtpost.visibility = View.GONE
                }
                if (message.timestamp == "") {
                    binding.timeImg.visibility = View.GONE
                }
            }


            if (message.voiceUrl == "") {
                binding.voiceCmt.visibility = View.GONE
            } else {
                Glide.with(binding.root).load(message.avtpost).into(binding.avtuser)
                binding.voiceCmt.visibility = View.VISIBLE
                message.voiceUrl?.let { downloadAudio(it) }
                if (message.content == "") {
                    binding.txtcontent.visibility = View.GONE
                }
                if (message.avtpost == "") {
                    binding.avtuser.visibility = View.GONE
                }
                if (message.timestamp == "") {
                    binding.txttime.visibility = View.GONE
                }

            }

            if (message.message?.trim()?.isNotEmpty() == true) {
                binding.messagetxt.visibility = View.VISIBLE
            } else {
                binding.messagetxt.visibility = View.GONE
            }

            if (message.senderId == "Gemini") {
                binding.avtRequest.setImageResource(R.drawable.ic_gemini)
            } else {
                Glide.with(binding.root).load(image).into(binding.avtRequest)
            }
            if (showTime) {
                binding.tvCreatedAt.text =
                    "-" + formatTimestamp(message.createdAt.toString()) + "-"
                binding.tvCreatedAt.visibility = View.VISIBLE
            } else {
                binding.tvCreatedAt.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                binding.subCreateAt.visibility = View.VISIBLE
                val prettyTime = PrettyTime(Locale("vi"))
                val formattedTime = prettyTime.format(Date(message.createdAt?.toLong() ?: 0))


                binding.subCreateAt.text =
                    formattedTime.replace(" trước", "").replace("cách đây ", "")
                        .replace("giây", "vừa xong")




                binding.state.visibility = View.VISIBLE

                val messageStatus = when (message.status) {
                    MessageStatus.SENDING -> "Đang gửi"
                    MessageStatus.SENT -> "Đã gửi"
                    MessageStatus.DELIVERED -> "Đã nhận"
                    else -> "Đã xem"
                }

                binding.state.text = messageStatus
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.subCreateAt.visibility = View.GONE
                    binding.state.visibility = View.GONE
                }, 2000)

            }



            binding.chat = message

            binding.executePendingBindings()
        }

        fun formatTimestamp(timestamp: String): String {
            return try {
                val timeInMillis = timestamp.toLong()
                val date = Date(timeInMillis)
                val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                format.format(date)
            } catch (e: Exception) {
                "Invalid date"
            }
        }

        private fun downloadAudio(url: String) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url)
                    val localFile = File.createTempFile("audio", "aac")
                    storageRef.getFile(localFile).await()

                    withContext(Dispatchers.Main) {
                        Log.d("VoiceViewHolder", "Audio file downloaded successfully")
                        binding.wave.setRawData(localFile.readBytes())

                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(localFile.absolutePath)
                        mediaPlayer.prepare()
                    }
//                        binding.tvTimer.text = formatTime(time)
                    binding.play.setOnClickListener {

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
                                binding.play.text = "dừng"
                                handler.post(updateWaveform)
                            } catch (e: IOException) {
                                Log.e("VoiceViewHolder", "loi", e)
                            }

                        } else {
                            stopAudio()
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
                binding.play.text = "phát"
                handler.removeCallbacks(updateWaveform)
                binding.wave.progress = 0f
            }
        }

        private val updateWaveform = object : Runnable {
            override fun run() {
                mediaPlayer?.let { player ->
                    if (isplay) {
                        val progress = (player.currentPosition.toFloat() / player.duration) * 100
                        Log.d("TAGY", "$progress , ${player.duration}")
                        binding.wave.progress = progress
                        handler.postDelayed(this, 1)
                    }

                }
            }
        }
    }

    fun submitList(newMessages: List<Message>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = messages.size
            override fun getNewListSize(): Int = newMessages.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return messages[oldItemPosition].messageId == newMessages[newItemPosition].messageId
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return messages[oldItemPosition] == newMessages[newItemPosition]
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        messages = newMessages
        diffResult.dispatchUpdatesTo(this)
    }
}
