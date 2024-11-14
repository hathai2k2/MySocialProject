package com.example.mysocialproject.ui.base

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.example.mysocialproject.R

abstract class BaseActivityWithViewModel<T : ViewDataBinding, V : BaseViewModel<*>> : BaseActivity<T>(){
    val mViewModel: V by lazy { ViewModelProvider(this)[getViewModelClass()] }
    abstract fun getViewModelClass(): Class<V>
    abstract fun getBindingVariable(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        performDataBinding()
//        setObserverLoading()
    }


    private fun performDataBinding() {
        mViewBinding.let {
            it.setVariable(getBindingVariable(), mViewModel)
            it.lifecycleOwner = this
            it.executePendingBindings()
        }
    }

//    private fun setObserverLoading() {
//        mViewModel.mViewLoading.observe(this) {
//            if (it) {
//                DialogUtil.showLoading(this)
//            } else {
//                DialogUtil.hideLoading()
//            }
//        }
//    }
}