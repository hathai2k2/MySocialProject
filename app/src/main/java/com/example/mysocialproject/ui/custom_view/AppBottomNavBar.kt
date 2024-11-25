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
import com.example.mysocialproject.databinding.ViewBottomNavBarBinding
import com.example.mysocialproject.databinding.ViewHeaderBinding


class AppBottomNavBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle){
    private val layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val mViewBinding = ViewBottomNavBarBinding.inflate(layoutInflater, this, true)
    init {
        val o = context.obtainStyledAttributes(attrs, R.styleable.AppBottomNavBar, 0, 0)
        try {
            val bottomNavBarTitle = o.getString(R.styleable.AppBottomNavBar_BottomNavBarTitle)
            val ivLeft = o.getDrawable(R.styleable.AppBottomNavBar_BottomNavBarIvLeft)
            val ivRight = o.getDrawable(R.styleable.AppBottomNavBar_BottomNavBarIvRight)

            if (bottomNavBarTitle ==null){
                mViewBinding.tvTitle.visibility = View.INVISIBLE
                mViewBinding.ivCenter.visibility = View.VISIBLE
            }else{
                mViewBinding.tvTitle.visibility = View.VISIBLE
                mViewBinding.ivCenter.visibility = View.INVISIBLE
                mViewBinding.tvTitle.text = bottomNavBarTitle
            }


            if (ivLeft !=null){
                mViewBinding.ivLeft.setImageDrawable(ivLeft)
            }else{
                mViewBinding.ivLeft.visibility = View.INVISIBLE
            }

            if (ivRight !=null){
                mViewBinding.ivRight.setImageDrawable(ivRight)
            }else{
                mViewBinding.ivRight.visibility = View.INVISIBLE
            }
        }finally {
            o.recycle()
        }
    }
    fun onClickRightIcon(onClick:()->Unit){
        mViewBinding.ivRight.setOnClickListener { onClick() }
    }
    fun onClickLeftIcon(onClick: () -> Unit){
        mViewBinding.ivLeft.setOnClickListener { onClick() }
    }
}