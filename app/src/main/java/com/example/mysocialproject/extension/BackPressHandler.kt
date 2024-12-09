package com.example.mysocialproject.extension

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner

object BackPressHandler {

    private var backPressedOnce = false

    fun handleBackPress(viewLifecycleOwner: LifecycleOwner, requireActivity: AppCompatActivity) {
        requireActivity.onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (backPressedOnce) {
                        // Nếu nhấn back lần nữa, thoát ứng dụng
                        requireActivity.finishAffinity() // Kết thúc Activity và ứng dụng
                    } else {
                        // Nếu nhấn lần đầu, hiển thị Toast thông báo
                        backPressedOnce = true
                        Toast.makeText(
                            requireActivity,
                            "Nhấn lần nữa để thoát",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Reset lại biến backPressedOnce sau 2 giây
                        Handler(Looper.getMainLooper()).postDelayed({
                            backPressedOnce = false
                        }, 2000)
                    }
                }
            })
    }
}
