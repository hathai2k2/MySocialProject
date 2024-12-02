package com.example.mysocialproject.ui.feature.user.forgot_password

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentForgotPasswordBinding
import com.example.mysocialproject.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordFragment : BaseFragment<FragmentForgotPasswordBinding>() {
    override fun getLayoutId(): Int {
        return R.layout.fragment_forgot_password
    }



}