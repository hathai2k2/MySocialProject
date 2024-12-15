package com.example.mysocialproject.ui.feature.manageraccount

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ActivityInitProfileBinding
import com.example.mysocialproject.ui.base.BaseActivity
import com.example.mysocialproject.ui.feature.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InitProfileActivity : BaseActivity<ActivityInitProfileBinding>() {
    private lateinit var authViewModel: AuthViewModel
    override fun getLayoutId(): Int {
        return R.layout.activity_init_profile
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        mViewBinding.authVModel = authViewModel
        mViewBinding.lifecycleOwner = this


        authViewModel.nameUser.observe(this, { name ->
            authViewModel.updatenamehelper(mViewBinding.nameCreate.text.toString() ?: "")
        })

        authViewModel.loading.observe(this, { loading ->
            mViewBinding.apply {
                if (loading != false) {
                    btnNextSignUp.text = ""
                    btnNextSignUp.icon = null
                    progressBar.visibility = View.VISIBLE
                } else {
                    progressBar.visibility = View.INVISIBLE
                    btnNextSignUp.text = "Tiếp tục"
                    btnNextSignUp.setIconResource(R.drawable.ic_next)
                }

            }
        })


        authViewModel.namehelper.observe(this, { name ->
            mViewBinding.apply {
                if (name == "") {
                    btnNextSignUp.isEnabled = true
                    btnNextSignUp.backgroundTintList = ActivityCompat.getColorStateList(
                        baseContext,
                        R.color.color_active
                    )
                }
            }
        })

        mViewBinding.imgvAvtUser.setOnClickListener {
            authViewModel.chooseAvatar(this)
        }

        mViewBinding.btnNextSignUp.setOnClickListener {
            authViewModel.updateAvtAndNameUser(this)
        }

        mViewBinding.btnBack.setOnClickListener {

            huydki()
        }


        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                huydki()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        authViewModel.handleImageResult(requestCode, resultCode, data,this, mViewBinding.imgvAvtUser)
    }

    private fun huydki() {
        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle("Xác nhận")
            .setMessage("Bạn có chắc chắn muốn hủy đăng ký không?")
            .setPositiveButton("Đồng ý") { dialog, _ ->
                authViewModel.deletenewAccount(this)
                finish()
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


}