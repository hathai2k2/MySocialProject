package com.example.mysocialproject.extension

import android.os.SystemClock
import android.view.View

abstract class OnSingleClickListener(val timeDelay: Long = 500) : View.OnClickListener {
    private var mLastClickTime: Long = 0
    abstract fun onSingleClick(v: View?)
    override fun onClick(v: View) {
        val currentClickTime = SystemClock.uptimeMillis()
        val elapsedTime = currentClickTime - mLastClickTime
        if (elapsedTime <= timeDelay) return
        mLastClickTime = currentClickTime
        onSingleClick(v)
    }

}