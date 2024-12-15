package com.example.mysocialproject.ui.feature.manageraccount

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.mysocialproject.R
import com.example.mysocialproject.StartAppActivity
import com.example.mysocialproject.databinding.ActivitySignUpBinding
import com.example.mysocialproject.ui.base.BaseActivity
import com.example.mysocialproject.ui.feature.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUp : BaseActivity<ActivitySignUpBinding>() {
    private lateinit var authViewModel: AuthViewModel
    override fun getLayoutId(): Int {
        return R.layout.activity_sign_up
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        mViewBinding.authVModel = authViewModel
        mViewBinding.lifecycleOwner = this
        authViewModel.email.observe(this, { email ->
            authViewModel.updateEmailHelperText(mViewBinding.emailCreate.text.toString() ?: "")
        })


        authViewModel.password.observe(this, { password ->
            authViewModel.updatePasswordHelperText(mViewBinding.pwCreate.text.toString() ?: "")
        })


        authViewModel.loading.observe(this, { loading ->
            mViewBinding.apply {
                if (loading == true) {
                    btnSignup.text = ""
                    btnSignup.icon = null
                } else {
                    btnSignup.text = "Tiếp tục"
                    btnSignup.setIconResource(R.drawable.ic_next)
                }

            }
        })

        authViewModel.emailHelperText.observe(this) { email ->
            authViewModel.passwordHelperText.observe(this, { pw ->
                mViewBinding.apply {
                    if (pw == null && email == null) {
                        btnSignup.isEnabled = true
                        btnSignup.backgroundTintList =
                            ActivityCompat.getColorStateList(baseContext, R.color.color_active)
                    } else {
                        btnSignup.isEnabled = false
                        btnSignup.backgroundTintList =
                            ActivityCompat.getColorStateList(baseContext, R.color.btndf)
                    }
                }
            })
        }

        mViewBinding.btnBack.setOnClickListener {
            val intent = Intent(this, StartAppActivity::class.java)
            startActivity(intent)
        }
        mViewBinding.btnSignup.setOnClickListener {
            authViewModel.signUp(this)
        }
    }


}