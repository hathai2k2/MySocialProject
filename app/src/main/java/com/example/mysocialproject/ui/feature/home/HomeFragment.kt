package com.example.mysocialproject.ui.feature.home

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mysocialproject.BR
import com.example.mysocialproject.MainViewModel
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentHomeBinding
import com.example.mysocialproject.model.PostResult
import com.example.mysocialproject.ui.base.BaseFragment
import com.example.mysocialproject.ui.base.BaseFragmentWithViewModel
import com.example.mysocialproject.ui.dialog.DialogUtil
import com.example.mysocialproject.ui.feature.friend.FriendBottomSheet
import com.example.mysocialproject.ui.feature.post.PostViewModel
import com.example.mysocialproject.ui.feature.user.profile.ProfileBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragmentWithViewModel<FragmentHomeBinding,HomeViewModel>(),HomeNavigation {
    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }
    private val postViewModel: PostViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    private val CAMERA_PERMISSION_REQUEST_CODE = 1002
    private var currentFragmentPosition = 0
    lateinit var gestureDetector: GestureDetector
    override fun getViewModelClass(): Class<HomeViewModel> {
        return HomeViewModel::class.java
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun initViewModel(): Lazy<HomeViewModel> {
        return viewModels()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.setNavigator(this)
        mainViewModel.show()
        mViewModel.getInfo()
        mViewBinding.header.apply {
            mViewModel.getUserResult.observe(viewLifecycleOwner){
                it?.avatarUser?.toUri()?.let { it1 -> setAvatarUri(it1) }
            }
            onClickRightIcon {
                val action = HomeFragmentDirections.actionGlobalChatFragment()
                findNavController().navigate(action)
            }
            onClickLeftIcon {
                ProfileBottomSheet.show(
                    fragmentManager = childFragmentManager,
                    onChangeAvatar = {uri->
                        if (uri != null) {
                            mViewModel.updateAvatar(uri)
                            mViewBinding.header.setAvatarUri(uri)
                        }
                    },
                    onChangeName = {
                        DialogUtil.showChangeTextDialog(
                            context = requireContext(),
                            label = context.getString(R.string.label_change_name),
                            onConfirm = {name->
                                mViewModel.updateName(name)
                                hideKeyboard()
                            }
                        )
                    },
                    onChangePassword = {
                        DialogUtil.showChangeTextDialog(
                            context = requireContext(),
                            label = context.getString(R.string.label_change_password),
                            onConfirm = {password->
                                mViewModel.updatePassword(password)
                            }
                        )
                    },
                    onLogout = {
                        mViewModel.logout()
                    },
                    userData = mViewModel.getUserResult.value
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
                openPost()
            }
        }
        mViewBinding.viewPager2.isUserInputEnabled = false

        gestureDetector = GestureDetector(requireContext(), GestureListener())
        mViewBinding.root.setOnTouchListener{v,event->
            gestureDetector.onTouchEvent(event)
            true
        }

        postViewModel.postResultLiveData.observe(viewLifecycleOwner) { result ->
            when (result) {
                is PostResult.Success -> {
                    openPost()
                }
                else-> {
                    Toast.makeText(requireContext(), "Vui lòng thử lại sau", Toast.LENGTH_SHORT).show()
                }
            }
        }
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

    override fun onLogOut() {
        val action = HomeFragmentDirections.actionGlobalSplashFragment()
        findNavController().navigate(action)
    }

    override fun onOpenPost() {

    }

}