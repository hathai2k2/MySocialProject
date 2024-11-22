package com.example.mysocialproject.ui.base

import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.BuildCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.example.mysocialproject.R

abstract class BaseActivity<V : ViewDataBinding> : AppCompatActivity() {
    lateinit var mViewBinding: V

    @LayoutRes
    abstract fun getLayoutId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = DataBindingUtil.setContentView(this, getLayoutId())
        handlerBackPress()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    inline fun <reified T : Parcelable?> getMyParable(bundle: Bundle, key: String): T? {
        return if (Build.VERSION.SDK_INT >= 33) {
            bundle.getParcelable(key, T::class.java)
        } else {
            @Suppress("DEPRECATION") bundle.getParcelable<T>(key)
        }
    }

    fun gotoBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    fun handlerBackPress() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (Build.VERSION.SDK_INT >= 33) {
                onBackInvokedDispatcher.registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT
                ) {
                    onBack()
                }
            }
        } else {

            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBack()
                }
            })
        }
    }

    open fun onBack() {

    }

    fun showActivity(
        t: Class<*>,
        bundle: Bundle? = null,
        enter: Int = R.anim.slide_in_right,
        exit: Int = R.anim.slide_out_left
    ) {
        Intent(this, t).apply {
            if(bundle != null) {
                putExtras(bundle)
            }
            val animationBundle = ActivityOptions.makeCustomAnimation(
                this@BaseActivity,
                enter,
                exit
            ).toBundle()
            startActivity(this, animationBundle)
        }
    }

    fun checkPermission(
        permissions: List<String>,
        onPermissionGrand: () -> Unit,
        onShowRequestPermissionRationale: () -> Unit,
        onRequestPermission: (Array<String>) -> Unit,
    ) {
        val permissionNotGrant = permissions.filter {
            ContextCompat.checkSelfPermission(
                this,
                it
            ) == PackageManager.PERMISSION_DENIED
        }
        if (permissionNotGrant.isEmpty()) {
            onPermissionGrand()
        } else if (shouldShowRequestPermissionRationale(permissionNotGrant.first())) {
            onShowRequestPermissionRationale()
        } else {
            onRequestPermission(permissionNotGrant.toTypedArray())
        }
    }



//    fun showErrorDialog(throwable: Throwable) {
//        val message = when(throwable) {
//            is ResException -> {
//                if(throwable.params != null) {
//                    this.getString(throwable.strRes, *throwable.params.toTypedArray())
//                } else {
//                    this.getString(throwable.strRes)
//                }
//            }
//            is ApiException -> throwable.errorMessage
//            is NoConnectException -> this.getString(R.string.no_connect)
//            else -> throwable.message ?: this.getString(R.string.error_some_thing_went_wrong_content)
//        }
//        DialogUtil.showCommonDialog(
//            context = this,
//            title = "エラー",
//            message = message ?: this.getString(R.string.error_some_thing_went_wrong_content),
//            btnRight = "OK",
//            onRightButtonClick = {},
//        )
//    }
//
    fun showLoading() {
//        DialogUtil.showLoading(this)
    }

    fun dismissLoading() {
//        DialogUtil.hideLoading()
    }
}


