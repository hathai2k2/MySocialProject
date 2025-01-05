package com.example.mysocialproject.ui.feature.chat

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ActivityItemChatBinding
import com.example.mysocialproject.ui.base.BaseActivity
import com.example.mysocialproject.ui.feature.adapter.MessageAdapter
import com.example.mysocialproject.ui.feature.viewmodel.AuthViewModel
import com.example.mysocialproject.ui.feature.viewmodel.MessageViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class ItemChatActivity : BaseActivity<ActivityItemChatBinding>() {

    private lateinit var messageAdapter: MessageAdapter
    private val authViewModel: AuthViewModel by viewModels()
    private val messviewModel: MessageViewModel by viewModels()
    private lateinit var friendId: String
    private lateinit var friendName: String
    private lateinit var friendAvatar: String
    override fun getLayoutId(): Int {
        return R.layout.activity_item_chat
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        friendId = intent.getStringExtra("FRIEND_ID") ?: return
        friendName = intent.getStringExtra("FRIEND_NAME") ?: return
        friendAvatar = intent.getStringExtra("FRIEND_AVATAR") ?: return

        if (friendId == "Gemini") {
            mViewBinding.namefr.text = "Snap chat"
            mViewBinding.avtRequest.setImageResource(R.drawable.ic_gemini)
            mViewBinding.avtRequest.scaleType = ImageView.ScaleType.CENTER_INSIDE
            mViewBinding.avtRequest.strokeWidth = 0f
        } else {
            mViewBinding.namefr.text = friendName
            Glide.with(this).load(friendAvatar).into(mViewBinding.avtRequest)
        }


        val currentId = authViewModel.getCurrentId()
        messageAdapter = MessageAdapter(currentId, friendAvatar, listOf(), messviewModel, this)
        mViewBinding.rcvFromchat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        mViewBinding.rcvFromchat.adapter = messageAdapter

        mViewBinding.lifecycleOwner = this
        mViewBinding.messageVmdol = messviewModel
        messviewModel.getMessages(currentId, friendId.toString())
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                messviewModel.messages.collect { messages ->
                    mViewBinding.rcvFromchat.layoutManager?.scrollToPosition(messageAdapter.itemCount - 1)
                    messageAdapter.submitList(messages)

                }
            }
        }



        mViewBinding.rcvFromchat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                messviewModel.updateMessagesToSeen(friendId)
                if (friendId == "Gemini") {
                    messviewModel.updateMessagesToReadtu(friendId)
                }

            }

        })


        messviewModel.messagesend.observe(this) {
            val trimmedMessage = it?.trim()
            if (trimmedMessage?.isNotEmpty() == true) {
                mViewBinding.btnSend.isEnabled = true
                mViewBinding.btnSend.backgroundTintList =
                    ActivityCompat.getColorStateList(baseContext, R.color.color_active)
            } else {
                mViewBinding.btnSend.isEnabled = false
                mViewBinding.btnSend.backgroundTintList =
                    ActivityCompat.getColorStateList(baseContext, R.color.bg_input)
            }
        }

        mViewBinding.btnSend.setOnClickListener {
            if (friendId == "Gemini") {
                messviewModel.sendMessageToGemini()
            } else {
                messviewModel.sendMessage("", friendId, "", "", "", "", "")
            }
            mViewBinding.message.text.clear()

        }
        mViewBinding.btnBack.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }



        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(
                    this@ItemChatActivity,
                    ChatActivity::class.java
                )
                startActivity(intent)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        messviewModel.updateMessagesToSeen(friendId)
    }

}
