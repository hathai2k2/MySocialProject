package com.example.mysocialproject.ui.feature.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentChatBinding
import com.example.mysocialproject.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : BaseFragment<FragmentChatBinding>() {
    override fun getLayoutId()=R.layout.fragment_chat


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}