package com.example.mysocialproject.ui.base

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding

abstract class BaseFragmentWithViewModel<T : ViewDataBinding, V : BaseViewModel<*>> :
    BaseFragment<T>() {
    protected val mViewModel: V by initViewModel()
    abstract fun getViewModelClass(): Class<V>
    abstract fun getBindingVariable(): Int
    abstract fun initViewModel() : Lazy<V>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewBinding.apply {
            setVariable(getBindingVariable(), mViewModel)
            lifecycleOwner = this@BaseFragmentWithViewModel
            executePendingBindings()
        }
        setObserverLoading()
    }

    private fun setObserverLoading() {
        mViewModel.mViewLoading.observe(viewLifecycleOwner) {
            if (it) {
//                showLoading()
            } else {
//                dismissLoading()
            }
        }
    }


}