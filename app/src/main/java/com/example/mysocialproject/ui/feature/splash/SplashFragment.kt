package com.example.mysocialproject.ui.feature.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.mysocialproject.BR
import com.example.mysocialproject.MainViewModel
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentSplashBinding
import com.example.mysocialproject.model.UserData
import com.example.mysocialproject.ui.base.BaseFragment
import com.example.mysocialproject.ui.base.BaseFragmentWithViewModel
import com.example.mysocialproject.ui.feature.friend.FriendViewModel
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashFragment : BaseFragmentWithViewModel<FragmentSplashBinding, SplashViewModel>(),SplashNavigation {
    override fun getLayoutId(): Int {
        return R.layout.fragment_splash
    }

    override fun getViewModelClass(): Class<SplashViewModel> {
        return SplashViewModel::class.java
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun initViewModel(): Lazy<SplashViewModel> {
        return viewModels<SplashViewModel>()
    }

    private val friendViewModel: FriendViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.setNavigator(this)
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        openApp()
    }

    private fun openApp() {
        Handler(Looper.getMainLooper()).postDelayed({
            val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.text_splash)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {

                }

                override fun onAnimationEnd(p0: Animation?) {

                    mViewModel.checkLogin()
                }

                override fun onAnimationRepeat(p0: Animation?) {
                }

            })
            mViewBinding.tvSplash.animation = animation
            mViewBinding.tvSplash.visibility = View.VISIBLE
        }, 2000)
    }

    override fun openHome(uid:String,userData: UserData) {

        val action = SplashFragmentDirections.actionSplashFragmentToHomeFragment(
            curentId = uid,
        )
        findNavController().navigate(action , NavOptions.Builder().setPopUpTo(R.id.splashFragment,true).build())
    }

    override fun openLogin() {
        val action = SplashFragmentDirections.actionSplashFragmentToSignInFragment()
        findNavController().navigate(action)
    }

    override fun openSignupProfile() {
        val action = SplashFragmentDirections.actionGlobalSignupProfileFragment()
        findNavController().navigate(action)
    }


}