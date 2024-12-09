package com.example.mysocialproject.ui.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import com.example.mysocialproject.ui.feature.friend.FriendViewModel

object DialogUtil {
    fun showChangeTextDialog(
        context: Context,
        label: String,
        onConfirm: (String) -> Unit
    ):AppChangeTextDialog{
        return AppChangeTextDialog(context, label, onConfirm).apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

    fun showConfirmationDialog(
        context: Context,
        label: String,
        message: String,
        onConfirm: () -> Unit,
       ):AppConfirmationDialog{
        return AppConfirmationDialog(context,label,message,onConfirm).apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

    fun showAddFriendDialog(
        context: Context,
        friendViewModel: FriendViewModel,
        onConfirm: () -> Unit,
    ):AddFriendDialog{
        return AddFriendDialog(context,friendViewModel,onConfirm).apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }
}