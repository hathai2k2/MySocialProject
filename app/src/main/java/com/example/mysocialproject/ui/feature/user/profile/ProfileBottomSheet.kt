package com.example.mysocialproject.ui.feature.user.profile

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateViewModelFactory
import com.bumptech.glide.Glide
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ViewProfileBottomSheetBinding
import com.example.mysocialproject.model.UserData
import com.example.mysocialproject.ui.base.BaseBottomSheetFragment
import com.example.mysocialproject.ui.feature.friend.FriendViewModel


class ProfileBottomSheet(
    private val onChangeAvatar: (Uri?) -> Unit,
    private val onChangeName: () -> Unit,
    private val onChangePassword: () -> Unit,
    private val onLogout: () -> Unit,
    private val userData: UserData
) : BaseBottomSheetFragment<ViewProfileBottomSheetBinding>() {
    override fun getLayoutId()=R.layout.view_profile_bottom_sheet

    private var avatarUri: Uri? = null

    companion object{
        fun show(
            fragmentManager: FragmentManager,
            onChangeAvatar: (Uri?) -> Unit,
            onChangeName: () -> Unit,
            onChangePassword: () -> Unit,
            onLogout: () -> Unit,
            userData: UserData
        ){
            val dialog= ProfileBottomSheet(onChangeAvatar, onChangeName, onChangePassword, onLogout,userData)
            dialog.show(fragmentManager, ProfileBottomSheet::class.java.name)
        }
    }



    // Cập nhật việc chọn ảnh với Glide khi có URI mới
    // Use the correct contract to pick media
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            // Ẩn image 'no-image'
            mViewBinding.ivNoImage.visibility = View.GONE
            // Hiển thị image avatar
            mViewBinding.imAvatar.visibility = View.VISIBLE

            // Sử dụng Glide để tải ảnh vào ImageView
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .circleCrop()
                .placeholder(R.drawable.bg_circle_image_gray)
                .into(mViewBinding.imAvatar)

            // Lưu lại URI của ảnh
            avatarUri = uri
            // Gọi onChangeAvatar và trả về URI mới
            onChangeAvatar(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
            avatarUri = userData.avatarUser.toUri()
            Log.d("TAG", "Avatar URI: $avatarUri")
            mViewBinding.tvName.text = userData.nameUser
            mViewBinding.tvEmail.text = userData.emailUser

        if (avatarUri!= null) {
            mViewBinding.ivNoImage.visibility = View.GONE
            mViewBinding.imAvatar.visibility = View.VISIBLE
            Glide.with(requireContext())
                .load(avatarUri)
                .centerCrop()
                .circleCrop()
                .placeholder(R.drawable.bg_circle_image_gray)
                .error(R.drawable.ic_loading)
                .fallback(R.drawable.ic_loading)
                .into(mViewBinding.imAvatar)
            Log.d("TAG","avatarUser: $avatarUri")
        } else {
            mViewBinding.ivNoImage.visibility = View.VISIBLE
            mViewBinding.imAvatar.visibility = View.GONE
            Log.d("TAG"," NO avatarUser")
        }


        // Khi người dùng click vào ảnh avatar
        mViewBinding.imAvatar.setOnClickListener {

            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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