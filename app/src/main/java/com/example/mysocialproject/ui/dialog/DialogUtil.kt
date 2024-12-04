package com.example.mysocialproject.ui.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager

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
}