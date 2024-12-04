package com.example.mysocialproject.ui.feature.user.signup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mysocialproject.BR
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentSignUpBinding
import com.example.mysocialproject.ui.base.BaseFragment
import com.example.mysocialproject.ui.base.BaseFragmentWithViewModel
import com.example.mysocialproject.ui.custom_view.DrawableClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : BaseFragmentWithViewModel<FragmentSignUpBinding,SignUpViewModel>(),SignUpNavigation {
    override fun getLayoutId(): Int {
        return R.layout.fragment_sign_up
    }

    override fun getViewModelClass()= SignUpViewModel::class.java

    override fun getBindingVariable() = BR.viewModel

    override fun initViewModel() = viewModels<SignUpViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.setNavigator(this)
        mViewBinding.btnSignUp.setButtonClickListener {
            hideKeyboard()
            if (mViewBinding.edtEmail.getText().isNotEmpty() || mViewBinding.edtPassword.getText().isNotEmpty()) {
                mViewModel.signUp(
                    email = mViewBinding.edtEmail.getText(),
                    password = mViewBinding.edtPassword.getText()
                )
            }
        }
        mViewBinding.tvSignIn.setOnClickListener {
            var action = SignUpFragmentDirections.actionSignUpFragmentToSignInFragment()
            findNavController().navigate(action)
        }
        mViewBinding.edtPassword.setRightDrawableClick { position ->
            if (position == DrawableClickListener.DrawablePosition.RIGHT) {
                mViewBinding.edtPassword.togglePasswordVisibility()
            }
        }
    }

    override fun onSuccess() {
        val action = SignUpFragmentDirections.actionSignUpFragmentToSignupProfileFragment()
        findNavController().navigate(action)
    }
}