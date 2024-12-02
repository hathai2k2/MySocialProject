package com.example.mysocialproject.ui.feature.friend

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ViewFriendBottomSheetBinding
import com.example.mysocialproject.ui.base.BaseBottomSheetFragment


class FriendBottomSheet(
    private val onOpenShareLink: () -> Unit
) : BaseBottomSheetFragment<ViewFriendBottomSheetBinding>() {
    override fun getLayoutId(): Int {
        return R.layout.view_friend_bottom_sheet
    }
    companion object{
        fun show(
            fragmentManager: FragmentManager,
            onOpenShareLink: () -> Unit
        ){
            val fragment = FriendBottomSheet(onOpenShareLink)
            fragment.show(fragmentManager,FriendBottomSheet::class.java.name)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewBinding.btnShareLink.setOnClickListener {
            onOpenShareLink()
        }

    }
}