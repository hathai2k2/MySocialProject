package com.example.mysocialproject.ui.feature.manageraccount

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ActivityForgotPwactivityBinding
import com.example.mysocialproject.ui.base.BaseActivity
import com.example.mysocialproject.ui.feature.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPWActivity :
    BaseActivity<ActivityForgotPwactivityBinding>() {
    override fun getLayoutId(): Int {
        return R.layout.activity_forgot_pwactivity
    }


    private lateinit var authViewModel: AuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        mViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_pwactivity)
        mViewBinding.lifecycleOwner = this
        mViewBinding.viewModel = authViewModel
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)


        mViewBinding.forgotpassword.setOnClickListener {
            authViewModel.forgotPassword()
        }
        authViewModel.btntext.observe(this) {
            mViewBinding.forgotpassword.text = it
        }

        authViewModel.emailforgot.observe(this) {
            if (it != null && it.matches(Regex("^[A-Za-z0-9+_.-]{2,}@[A-Za-z0-9.-]{2,}\\.[A-Za-z0-9-]{2,}\$"))) {
                mViewBinding.forgotpassword.isEnabled = true
                mViewBinding.forgotpassword.backgroundTintList =
                    this.getColorStateList(R.color.color_active)
            } else {
                mViewBinding.forgotpassword.isEnabled = false
                mViewBinding.forgotpassword.backgroundTintList =
                    this.getColorStateList(R.color.colorbtnctive)
            }
        }
    }
}