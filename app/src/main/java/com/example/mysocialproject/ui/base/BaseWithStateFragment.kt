package com.example.mysocialproject.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

abstract class BaseWithStateFragment<V : ViewDataBinding> : Fragment() {
    protected var mActivity: BaseActivity<*>? = null
    lateinit var mViewBinding: V
    private var onBackPress = {}
    protected var hasInitialier = false

    protected val savedStateHandle by lazy { findNavController().currentBackStackEntry?.savedStateHandle }

    abstract fun getLayoutId(): Int

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BaseActivity<*>) {
            mActivity = activity as BaseActivity<*>
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (this::mViewBinding.isInitialized.not()) {
            mViewBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        }
        return mViewBinding.root
    }

    fun showLoading() {
        mActivity?.showLoading()
    }

    fun dismissLoading() {
        mActivity?.dismissLoading()
    }

    fun getBack(onBackPress: () -> Unit) {
        this.onBackPress = onBackPress
    }

    fun hideKeyboard() {
        val focusView = mViewBinding.root.findFocus()
        if (focusView != null) {
            mViewBinding.root.clearFocus()
//            requireContext().hideKeyboard(focusView)
        }
    }


//    fun addBackPress(onBack: () -> Unit) {
//        val callback = requireActivity().onBackPressedDispatcher.addCallback(
//            viewLifecycleOwner,
//            object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    onBack()
//                }
//            })
//    }

    fun setBindingVariable(variableId: Int, value: Any) {
        mViewBinding.apply {
            setVariable(variableId, value)
            lifecycleOwner = this@BaseWithStateFragment
            executePendingBindings()
        }

    }

    fun popBackStackWithArgument(args: Map<String, Any>) {
        val navController = findNavController()
        val saveStateHandler = navController.previousBackStackEntry?.savedStateHandle
        args.forEach { (t, u) ->
            saveStateHandler?.set(t, u)
        }

        navController.popBackStack()
    }

    fun <T> clearArgumentBackstack(key: String) {
        savedStateHandle?.remove<T>(key)
    }
}