package com.example.mysocialproject.ui.feature.chat

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ActivityChatBinding
import com.example.mysocialproject.ui.base.BaseActivity
import com.example.mysocialproject.ui.feature.adapter.friendlist_chatAdapter
import com.example.mysocialproject.ui.feature.home.CreatePostActivity
import com.example.mysocialproject.ui.feature.viewmodel.AuthViewModel
import com.example.mysocialproject.ui.feature.viewmodel.MessageViewModel

@RequiresApi(Build.VERSION_CODES.O)
class ChatActivity : BaseActivity<ActivityChatBinding>() {
    private lateinit var friendsAdapter: friendlist_chatAdapter
    private val messageViewModel: MessageViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    override fun getLayoutId(): Int {
        return R.layout.activity_chat
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding.recyclerView.layoutManager = LinearLayoutManager(this)

        friendsAdapter = friendlist_chatAdapter(
            this,
            listOf(),
            mapOf(),
            onGetIdfriend = { userId, nameUser, avatarUser ->
                openChatWithFriend(userId, nameUser, avatarUser)
            },
            updatestate = { userId -> messageViewModel.updateMessagesToSeen(userId) },
            messageViewModel
        )
        mViewBinding.recyclerView.adapter = friendsAdapter


        messageViewModel.friendsWithMessages.observe(this, Observer { friendsAndMessages ->
            val (friends, lastMessages) = friendsAndMessages
            friendsAdapter.updateFriends(friends, lastMessages)
            friendsAdapter.notifyDataSetChanged()
        })
        messageViewModel.fetchFriendsWithLastMessages()


        mViewBinding.chatbot.setOnClickListener {
            val intent = Intent(this, ItemChatActivity::class.java).apply {
                putExtra("FRIEND_ID", "Gemini")
                putExtra("FRIEND_NAME", "")
                putExtra("FRIEND_AVATAR", "")
            }
            startActivity(intent)
        }

        mViewBinding.btnBack.setOnClickListener {
            val intent = Intent(this, CreatePostActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_down, R.anim.slide_out_down
            )
            startActivity(intent, options.toBundle())
            finish()
        }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(
                    this@ChatActivity, CreatePostActivity::class.java
                )
                val options = ActivityOptions.makeCustomAnimation(
                    this@ChatActivity,
                    R.anim.slide_in_down, R.anim.slide_out_down
                )
                startActivity(intent, options.toBundle())
                finish()
            }
        })

    }

    private fun openChatWithFriend(friendId: String, friendName: String, friendAvatar: String) {

        val intent = Intent(this, ItemChatActivity::class.java).apply {
            putExtra("FRIEND_ID", friendId)
            putExtra("FRIEND_NAME", friendName)
            putExtra("FRIEND_AVATAR", friendAvatar)
        }
        startActivity(intent)
    }


}