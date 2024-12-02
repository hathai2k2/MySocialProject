package com.example.mysocialproject.ui.feature.user.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentSignupProfileBinding
import com.example.mysocialproject.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignupProfileFragment : BaseFragment<FragmentSignupProfileBinding>() {
    override fun getLayoutId(): Int {
        return R.layout.fragment_signup_profile
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewBinding.appHeader.onClickLeftIcon {
            findNavController().popBackStack()
        }
    }

}