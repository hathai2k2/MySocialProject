package com.example.mysocialproject.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

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

    override fun onDetach() {
        super.onDetach()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


}