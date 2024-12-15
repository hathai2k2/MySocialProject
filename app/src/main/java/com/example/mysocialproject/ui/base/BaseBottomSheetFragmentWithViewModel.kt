package com.example.mysocialproject.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
abstract class BaseBottomSheetFragmentWithViewModel : BaseBottomSheetFragment<ViewDataBinding>() {

    lateinit var mViewModel: BaseViewModel<*>

    abstract fun getViewModelClass(): Class<out BaseViewModel<*>>
    abstract fun getBindingVariable(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mViewModel = ViewModelProvider(this).get(getViewModelClass())
        performDataBinding()
        return view
    }

    private fun performDataBinding() {
        mViewBinding?.let {
            it.setVariable(getBindingVariable(), mViewModel)
            it.lifecycleOwner = viewLifecycleOwner
            it.executePendingBindings()
        }
    }
}
