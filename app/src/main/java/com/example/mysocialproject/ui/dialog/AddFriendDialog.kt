package com.example.mysocialproject.ui.dialog

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.DialogAddFriendBinding
import com.example.mysocialproject.ui.feature.friend.FriendViewModel

class AddFriendDialog(
    private val context: Context,
    private val friendViewModel: FriendViewModel, // Chỉ nhận ViewModel
    private val onConfirm: () -> Unit
) : AlertDialog(context) {

    private lateinit var mViewBinding: DialogAddFriendBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = DialogAddFriendBinding.inflate(layoutInflater)
        setContentView(mViewBinding.root)

        val userName = friendViewModel.infoUserSendlink.value?.first
        val avatar = friendViewModel.infoUserSendlink.value?.second
//        val avatar = "https://firebasestorage.googleapis.com/v0/b/mysocialproject-61c0e.firebasestorage.app/o/gwrWVygvE2OWMHg1H8jc63RzASk2%2Favatar%2Favt_gwrWVygvE2OWMHg1H8jc63RzASk2_6ace63fc-9c7c-4089-958c-0a6c46282825?alt=media&token=e95b57aa-3bbc-4708-9b26-189753df8980"
//        val userName = "user"

        if (userName != null && avatar != null) {
            mViewBinding.tvName.text = userName
            Glide.with(context)
                .load(avatar)
                .centerCrop()
                .circleCrop()
                .error(R.drawable.ic_loading)
                .placeholder(R.drawable.ic_user)
                .into(mViewBinding.ivAvatar)
        }

        mViewBinding.btnConfirm.setButtonClickListener {
            onConfirm()
            dismiss()
        }

        mViewBinding.btnCancel.setButtonClickListener {
            dismiss()
        }


//        observeViewModel()
    }

    private fun observeViewModel() {
        friendViewModel.errorMessage.observe(context as LifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                mViewBinding.btnConfirm.setText("Bạn bè")
            }
        }

        friendViewModel.loading.observe(context as LifecycleOwner) { isLoading ->
            mViewBinding.btnConfirm.showProgressBar(isLoading)
            if (isLoading == true) {
                mViewBinding.btnConfirm.setText("")
            }
        }

        friendViewModel.friendRequest.observe(context as LifecycleOwner) { isSuccess ->
            if (isSuccess == true) {
                mViewBinding.btnConfirm.setText("Đã gửi")
                dismiss()
            } else {
                mViewBinding.btnConfirm.setText("")
                dismiss()
            }
        }
    }
}