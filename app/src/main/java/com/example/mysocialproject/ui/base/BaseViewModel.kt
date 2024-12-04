package com.example.mysocialproject.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.lang.ref.WeakReference

private const val TAG = "BaseViewModel"

abstract class BaseViewModel<N> : ViewModel() {

    protected var mNavigator: WeakReference<N>? = null
    var mViewLoading = MutableLiveData<Boolean>()

    private var _error = MutableLiveData<Throwable>()
    var error: LiveData<Throwable> = _error

    fun getNavigator() = mNavigator?.get()

    fun setNavigator(navigator: N) {
        mNavigator = WeakReference(navigator)
    }


    fun showLoading() {
        if (mViewLoading.value != true) {
            mViewLoading.postValue(true)
        }
    }

    fun hideLoading() {
        mViewLoading.postValue(false)
    }



    fun setError(error: Throwable) {
        this@BaseViewModel._error.value = error
    }


    protected fun handleError(
        throwable: Throwable,
    ) {

    }

}