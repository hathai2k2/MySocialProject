package com.example.mysocialproject.ui.custom_view

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ViewEditTextBinding
import com.example.mysocialproject.databinding.ViewHeaderBinding


class AppHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle){
    private val layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val mViewBinding = ViewHeaderBinding.inflate(layoutInflater, this, true)
    init {
        val o = context.obtainStyledAttributes(attrs, R.styleable.AppHeader, 0, 0)
        try {
            val headerTitle = o.getString(R.styleable.AppHeader_headerTitle)
            val ivLeft = o.getDrawable(R.styleable.AppHeader_headerIvLeft)
            val ivRight = o.getDrawable(R.styleable.AppHeader_headerIvRight)

            if (headerTitle ==null){
                mViewBinding.ctFriend.visibility = View.VISIBLE
                mViewBinding.tvTitle.visibility = View.GONE
            }else{
                mViewBinding.ctFriend.visibility = View.INVISIBLE
                mViewBinding.tvTitle.visibility = View.VISIBLE
                mViewBinding.tvTitle.text = headerTitle
            }


            if (ivLeft !=null){
                mViewBinding.ivLeft.setImageDrawable(ivLeft)
            }else{
                mViewBinding.ivLeft.visibility = View.GONE
            }

            if (ivRight !=null){
                mViewBinding.ivRight.setImageDrawable(ivRight)
            }else{
                mViewBinding.ivRight.visibility = View.GONE
            }
        }finally {
            o.recycle()
        }
    }
}