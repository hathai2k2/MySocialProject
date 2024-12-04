package com.example.mysocialproject.ui.custom_view

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bumptech.glide.Glide
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
                mViewBinding.tvTitle.visibility = View.INVISIBLE
            }else{
                mViewBinding.ctFriend.visibility = View.INVISIBLE
                mViewBinding.tvTitle.visibility = View.VISIBLE
                mViewBinding.tvTitle.text = headerTitle
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
    fun onClickCenter(onClick: () -> Unit){
        mViewBinding.ctFriend.setOnClickListener { onClick() }
    }

    fun setAvatarUri(uri:Uri){
        Glide.with(context)
            .load(uri)
            .centerCrop()
            .circleCrop()
            .placeholder(R.drawable.bg_gray_radius_10)
            .error(R.drawable.ic_account_circle)
            .into(mViewBinding.ivLeft)
    }
}