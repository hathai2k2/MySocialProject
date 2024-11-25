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
import com.example.mysocialproject.databinding.FragmentChatBinding
import com.example.mysocialproject.ui.base.BaseFragment
import com.example.mysocialproject.ui.base.BaseFragmentWithViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : BaseFragmentWithViewModel<FragmentChatBinding, ChatViewModel>() {
    override fun getLayoutId()=R.layout.fragment_chat
    override fun getViewModelClass(): Class<ChatViewModel> {
        return ChatViewModel::class.java
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun initViewModel(): Lazy<ChatViewModel> {
        return viewModels<ChatViewModel>()
    }

    private var chatAdapter: ChatAdapter?=null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewBinding.appHeader.onClickLeftIcon {
            findNavController().popBackStack()
        }
        mViewBinding.rcvUser.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ChatAdapter(emptyList(), onCLick = {})
            adapter = chatAdapter
        }

        mViewModel.listUser.observe(viewLifecycleOwner){user->
            chatAdapter = ChatAdapter(user, onCLick = {
                val directions = ChatFragmentDirections.actionGlobalMessageFragment(WallPagerChat.EK)
                findNavController().navigate(directions)
            })
            mViewBinding.rcvUser.adapter = chatAdapter
        }
    }

}