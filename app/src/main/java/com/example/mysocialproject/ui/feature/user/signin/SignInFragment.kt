package com.example.mysocialproject.ui.feature.user.signin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentSignInBinding
import com.example.mysocialproject.ui.base.BaseFragment


class SignInFragment : BaseFragment<FragmentSignInBinding>() {
    override fun getLayoutId(): Int {
        return R.layout.fragment_sign_in
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewBinding.btnSignIn.setButtonClickListener {
            var action = SignInFragmentDirections.actionGlobalHomeFragment()
            findNavController().navigate(action)
        }
        mViewBinding.tvSignUp.setOnClickListener {
            var action = SignInFragmentDirections.actionSignInFragmentToSignUpFragment2()
            findNavController().navigate(action)
        }

    }
}