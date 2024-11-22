package com.example.mysocialproject.ui.feature.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mysocialproject.BR
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentMessageBinding
import com.example.mysocialproject.ui.base.BaseFragmentWithViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MessageFragment : BaseFragmentWithViewModel<FragmentMessageBinding,MessageViewModel>() {
    override fun getViewModelClass() = MessageViewModel::class.java

    override fun getBindingVariable() = BR.viewModel

    override fun initViewModel() = viewModels<MessageViewModel>()

    override fun getLayoutId() = R.layout.fragment_message

    private lateinit var adapter: MessageAdapter
    private var currentUserId: String = "user_2"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
// Initialize RecyclerView
        mViewBinding.rcvMessage.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = MessageAdapter(emptyList(), currentUserId)
            adapter = adapter
        }
        mViewBinding.appHeader2.onClickLeftIcon {
            findNavController().popBackStack()
        }

        // Load messages from ViewModel
        val receiverId = "user_1" // ID của người nhận
        mViewModel.loadMessages(currentUserId, receiverId)

        // Observe messages and update UI
        mViewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter = MessageAdapter(messages, currentUserId) // Cập nhật adapter
            mViewBinding.rcvMessage.adapter = adapter
            mViewBinding.rcvMessage.scrollToPosition(adapter.itemCount - 1) // Tự động cuộn xuống
        }

        // Send a new message (giả lập gửi tin nhắn)
        mViewBinding.appEditText.setRightDrawableClick  {
            val messageContent = mViewBinding.appEditText.getText()
            if (messageContent.isNotEmpty()) {
                mViewModel.sendMessage(currentUserId, receiverId, messageContent)
                mViewBinding.appEditText.clearText()
            }
            hideKeyboard()
        }
        mViewBinding.appEditText1.setRightDrawableClick  {
            val messageContent = mViewBinding.appEditText1.getText()
            if (messageContent.isNotEmpty()) {
                mViewModel.sendMessage(receiverId, currentUserId, messageContent)
                mViewBinding.appEditText1.clearText()
            }
            hideKeyboard()
        }
    }

}