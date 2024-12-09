package com.example.mysocialproject.ui.feature.chat

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mysocialproject.BR
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ActivityChatBinding
import com.example.mysocialproject.extension.onSingleClick
import com.example.mysocialproject.ui.base.BaseActivityWithViewModel

class ChatActivity : BaseActivityWithViewModel<ActivityChatBinding, ChatViewModel>() {
    override fun getLayoutId() = R.layout.activity_chat
    override fun getViewModelClass(): Class<ChatViewModel> {
        return ChatViewModel::class.java
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }


    private var chatAdapter: ChatAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewBinding.appHeader.onClickLeftIcon {

        }
        mViewBinding.rcvUser.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = ChatAdapter(emptyList(), onCLick = {})
            adapter = chatAdapter
        }

        mViewModel.listUser.observe(this) { user ->
            chatAdapter = ChatAdapter(user, onCLick = {
//                val directions = ChatActivityDirections.actionGlobalMessageFragment(WallPagerChat.EK)
//                findNavController().navigate(directions)
            })
            mViewBinding.rcvUser.adapter = chatAdapter
        }
        mViewBinding.ctBot.onSingleClick {
//            val directions = ChatFragmentDirections.actionGlobalMessageFragment(WallPagerChat.NOTHING)
//            findNavController().navigate(directions)

        }
    }
}