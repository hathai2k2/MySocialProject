package com.example.mysocialproject.ui.feature.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.navigation.fragment.findNavController
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentSplashBinding
import com.example.mysocialproject.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding>() {
    override fun getLayoutId(): Int {
        return R.layout.fragment_splash
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        Handler(Looper.getMainLooper()).postDelayed({
            val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.text_splash)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {

                }

                override fun onAnimationEnd(p0: Animation?) {
                    var action = SplashFragmentDirections.actionSplashFragmentToSignInFragment()
                    findNavController().navigate(action)
                }

                override fun onAnimationRepeat(p0: Animation?) {
                }

            })
            mViewBinding.tvSplash.animation = animation
            mViewBinding.tvSplash.visibility = View.VISIBLE
        }, 2000)
    }

}