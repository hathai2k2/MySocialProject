package com.example.mysocialproject.ui.feature.manageraccount

import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.mysocialproject.R
import com.example.mysocialproject.StartAppActivity
import com.example.mysocialproject.databinding.ActivitySignInBinding
import com.example.mysocialproject.ui.base.BaseActivity
import com.example.mysocialproject.ui.feature.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity : BaseActivity<ActivitySignInBinding>() {

    private lateinit var authViewModel: AuthViewModel
    override fun getLayoutId(): Int {
        return R.layout.activity_sign_in
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        mViewBinding.authVModel = authViewModel
        mViewBinding.lifecycleOwner = this

        authViewModel.email.observe(this, { email ->
            authViewModel.validateLogin(mViewBinding.emailCreate.text.toString() ?: "")
        })
        authViewModel.password.observe(this, { password ->
            authViewModel.validateLoginpw(mViewBinding.pwCreate.text.toString() ?: "")
        })

        authViewModel.loading.observe(this, { loading ->
            mViewBinding.apply {
                if (loading == true) {
                    btnSignin.text = ""
                    btnSignin.icon = null
                } else {
                    btnSignin.text = "Tiếp tục"
                    btnSignin.setIconResource(R.drawable.ic_next)
                }

            }
        })
        mViewBinding.btnSignin.setOnClickListener {
            authViewModel.login(this)
        }
        authViewModel.email.observe(this) {


            authViewModel.passwordHelperTextLg.observe(this, { pw ->
                mViewBinding.apply {
                    if (pw == "" && it != "" && it != null) {
                        btnSignin.isEnabled = true
                        btnSignin.backgroundTintList =
                            ActivityCompat.getColorStateList(baseContext, R.color.color_active)
                    } else {
                        btnSignin.isEnabled = false
                        btnSignin.backgroundTintList =
                            ActivityCompat.getColorStateList(baseContext, R.color.btndf)
                    }
                }
            })
        }


        mViewBinding.btnForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPWActivity::class.java))
        }
        mViewBinding.btnBack.setOnClickListener {
            val intent = Intent(this, StartAppActivity::class.java)
            startActivity(intent)
        }

    }

}