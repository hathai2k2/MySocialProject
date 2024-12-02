package com.example.mysocialproject.ui.feature.home

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentHomeBinding
import com.example.mysocialproject.ui.base.BaseFragment
import com.example.mysocialproject.ui.feature.friend.FriendBottomSheet
import com.example.mysocialproject.ui.feature.user.profile.ProfileBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }

    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    private val CAMERA_PERMISSION_REQUEST_CODE = 1002
    private var currentFragmentPosition = 0
    lateinit var gestureDetector: GestureDetector
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewBinding.header.apply {
            onClickRightIcon {
                val action = HomeFragmentDirections.actionGlobalChatFragment()
                findNavController().navigate(action)
            }
            onClickLeftIcon {
                ProfileBottomSheet.show(
                    fragmentManager = childFragmentManager,
                    onclick = {}
                )
            }
            onClickCenter {
                FriendBottomSheet.show(
                    fragmentManager = childFragmentManager,
                    onOpenShareLink = {}
                )
            }
        }
        mViewBinding.viewPager2.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 2
            }


            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> CameraFragment()
                    else -> RecordFragment()
                }
            }
        }
        mViewBinding.appBottomNavBar.apply {
            onClickLeftIcon {
                if (currentFragmentPosition != 0) {
                    mViewBinding.viewPager2.setCurrentItem(0, true)
                    currentFragmentPosition = 0
                }
            }
            onClickRightIcon {
                if (currentFragmentPosition != 1) {
                    mViewBinding.viewPager2.setCurrentItem(1, true)
                    currentFragmentPosition = 1
                }
            }
            onClickCenterIcon {
//                openPost()
            }
        }
        mViewBinding.viewPager2.isUserInputEnabled = false

        gestureDetector = GestureDetector(requireContext(), GestureListener())
    }

    private fun openPost() {
        val directions = HomeFragmentDirections.actionHomeFragmentToPostFragment()
        findNavController().navigate(directions)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            // Kiểm tra e1 và e2 có null không trước khi sử dụng
            if (e1 != null && e2 != null) {
                // Xác định hướng và tốc độ vuốt
                if (e1.y - e2.y > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {

                    openPost()
                    return true
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY)
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        mViewBinding.viewPager2.adapter = null
    }

}