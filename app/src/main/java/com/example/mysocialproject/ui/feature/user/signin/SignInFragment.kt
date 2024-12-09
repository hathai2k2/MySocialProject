package com.example.mysocialproject.ui.feature.user.signin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mysocialproject.BR
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentSignInBinding
import com.example.mysocialproject.extension.BackPressHandler
import com.example.mysocialproject.model.UserData
import com.example.mysocialproject.ui.base.BaseFragmentWithViewModel
import com.example.mysocialproject.ui.custom_view.DrawableClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : BaseFragmentWithViewModel<FragmentSignInBinding,SignInViewModel>(),SignInNavigation {
    override fun getLayoutId(): Int {
        return R.layout.fragment_sign_in
    }

    override fun getViewModelClass(): Class<SignInViewModel> {
        return SignInViewModel::class.java
    }

    override fun getBindingVariable(): Int {
       return BR.viewModel
    }

    override fun initViewModel(): Lazy<SignInViewModel> {
        return viewModels<SignInViewModel>()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.setNavigator(this)
        mViewBinding.btnSignIn.setButtonClickListener {
            hideKeyboard()
            if (mViewBinding.edtEmail.getText().isNotEmpty() || mViewBinding.edtPassword.getText().isNotEmpty()) {
                mViewModel.signIn(
                    email = mViewBinding.edtEmail.getText(),
                    password = mViewBinding.edtPassword.getText()
                )
            }
        }
        mViewBinding.tvSignUp.setOnClickListener {
            var action = SignInFragmentDirections.actionSignInFragmentToSignUpFragment2()
            findNavController().navigate(action)
        }
        mViewBinding.edtPassword.setRightDrawableClick { position ->
            if (position == DrawableClickListener.DrawablePosition.RIGHT) {
                mViewBinding.edtPassword.togglePasswordVisibility()
            }
        }

        mViewBinding.tvNavForgotPassword.setOnClickListener {
            val directions = SignInFragmentDirections.actionSignInFragmentToForgotPasswordFragment()
            findNavController().navigate(directions)
        }

        // Gọi BackPressHandler để xử lý back press
        BackPressHandler.handleBackPress(viewLifecycleOwner, requireActivity() as AppCompatActivity)
    }

    override fun onSuccess(userData: UserData) {
        val action = SignInFragmentDirections.actionSignInFragmentToHomeFragment()
        findNavController().navigate(action)
    }
}