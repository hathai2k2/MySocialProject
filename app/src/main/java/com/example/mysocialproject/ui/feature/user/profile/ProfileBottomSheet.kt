package com.example.mysocialproject.ui.feature.user.profile

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ViewProfileBottomSheetBinding
import com.example.mysocialproject.ui.base.BaseBottomSheetFragment


class ProfileBottomSheet(
    private val onChangeAvatar: () -> Unit,
    private val onChangeName: () -> Unit,
    private val onChangePassword: () -> Unit,
    private val onLogout: () -> Unit,
) : BaseBottomSheetFragment<ViewProfileBottomSheetBinding>() {
    override fun getLayoutId()=R.layout.view_profile_bottom_sheet

    companion object{
        fun show(
            fragmentManager: FragmentManager,
            onChangeAvatar: () -> Unit,
            onChangeName: () -> Unit,
            onChangePassword: () -> Unit,
            onLogout: () -> Unit,
        ){
            val dialog= ProfileBottomSheet(onChangeAvatar, onChangeName, onChangePassword, onLogout)
            dialog.show(fragmentManager, ProfileBottomSheet::class.java.name)
        }
    }
    private var avatarUri: Uri? = null


    // Cập nhật việc chọn ảnh với Glide khi có URI mới
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                // Ẩn image 'no-image'
                mViewBinding.ivNoImage.visibility = View.GONE
                // Hiển thị image avatar
                mViewBinding.imAvatar.visibility = View.VISIBLE

                // Sử dụng Glide để tải ảnh vào ImageView
                Glide.with(this)
                    .load(uri)
                    .into(mViewBinding.imAvatar)

                // Lưu lại URI của ảnh
                avatarUri = uri
            }
        }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set a click listener to trigger image picking
        mViewBinding.imAvatar.setOnClickListener {
            onChangeAvatar()
        }
        mViewBinding.groupChangePassword.setOnClickListener { 
            onChangePassword()
        }
        mViewBinding.ivEdtName.setOnClickListener {
            onChangeName()
        }
        mViewBinding.groupLogOut.setOnClickListener {
            onLogout()
        }

    }

}