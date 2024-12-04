package com.example.mysocialproject.ui.feature.user.forgot_password

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mysocialproject.BR
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentForgotPasswordBinding
import com.example.mysocialproject.ui.base.BaseFragment
import com.example.mysocialproject.ui.base.BaseFragmentWithViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordFragment : BaseFragmentWithViewModel<FragmentForgotPasswordBinding,ForgotPasswordViewModel>(),ForgotPasswordNavigation {
    override fun getLayoutId(): Int {
        return R.layout.fragment_forgot_password
    }

    override fun getViewModelClass() = ForgotPasswordViewModel::class.java
    override fun getBindingVariable() = BR.viewModel
    override fun initViewModel() = viewModels<ForgotPasswordViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel.setNavigator(this)
        mViewBinding.appHeader.onClickLeftIcon {
            findNavController().popBackStack()
        }
        mViewBinding.btnNext.setButtonClickListener {
            hideKeyboard()
            if (mViewBinding.appEditText2.getText().isNotEmpty()){
                mViewModel.forgotPassword(mViewBinding.appEditText2.getText())
            }
        }
    }

    override fun onSuccess() {
        findNavController().popBackStack()
    }


}