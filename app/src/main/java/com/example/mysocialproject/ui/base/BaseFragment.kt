package com.example.mysocialproject.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.example.mysocialproject.extension.hideKeyboard

abstract class BaseFragment<V:ViewDataBinding>:Fragment() {
    protected var mActivity:BaseActivity<*>?=null
    lateinit var mViewBinding:V
    protected var hasInitialier = false

    abstract fun getLayoutId():Int

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BaseActivity<*>){
            mActivity = activity as BaseActivity<*>
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(this::mViewBinding.isInitialized.not()){
            mViewBinding = DataBindingUtil.inflate(inflater,getLayoutId(),container,false)
        }
        return mViewBinding.root
    }

    fun hideKeyboard() {
        val focusView = mViewBinding.root.findFocus()
        if (focusView != null) {
            mViewBinding.root.clearFocus()
            requireContext().hideKeyboard(focusView)
        }
    }
    fun onBackPress() {

    }
}