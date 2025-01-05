package com.example.mysocialproject.ui.feature.home

import NotificationService
import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ActivityMainBinding
import com.example.mysocialproject.databinding.FriendRequestDialogBinding
import com.example.mysocialproject.ui.base.BaseActivity
import com.example.mysocialproject.ui.feature.chat.ChatActivity
import com.example.mysocialproject.ui.feature.friend.FriendListBottomSheet
import com.example.mysocialproject.ui.feature.home.CameraFragment.Companion.GALLERY_REQUEST_CODE
import com.example.mysocialproject.ui.feature.post.PostActivity
import com.example.mysocialproject.ui.feature.profile.ProfileActivity
import com.example.mysocialproject.repository.PostRepository
import com.example.mysocialproject.ui.feature.viewmodel.AuthViewModel
import com.example.mysocialproject.ui.feature.viewmodel.FriendViewmodel
import com.example.mysocialproject.ui.feature.viewmodel.MessageViewModel
import com.example.mysocialproject.ui.feature.viewmodel.PostViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class CreatePostActivity : BaseActivity<ActivityMainBinding>() {
    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    private val CAMERA_PERMISSION_REQUEST_CODE = 1002

    lateinit var gestureDetector: GestureDetector
    private var backPressedOnce = false
    private val authViewModel: AuthViewModel by viewModels()
    private val frVModel: FriendViewmodel by viewModels()
    private val postVmodel: PostViewModel by viewModels()
    private val messageViewModel: MessageViewModel by viewModels()
    private var currentFragmentPosition = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT


        checkAndRequestNotificationPermission()



        mViewBinding.lifecycleOwner = this

        mViewBinding.authVModel = authViewModel


        mViewBinding.frVmodel = frVModel

        mViewBinding.btnBottomSheetProfile.setOnClickListener {
            // Đóng activity hiện tại
            showActivity(ProfileActivity::class.java)
            finish()
        }


///go chat
        mViewBinding.btnMessage.setOnClickListener {
            intent = Intent(this, ChatActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this@CreatePostActivity,
                R.anim.slide_in_up, R.anim.slide_out_up
            )
            startActivity(intent, options.toBundle())
        }


        messageViewModel.unreadMessageCount.observe(this) { count ->
            if (count > 0) {
                mViewBinding.countmessage.text = if (count > 9) "+9" else count.toString()
                mViewBinding.countmessage.visibility = View.VISIBLE
            } else {
                mViewBinding.countmessage.text = ""
                mViewBinding.countmessage.visibility = View.GONE
            }
            Log.d("MainActivity_tinnhanmoi", "$count")
        }


        frVModel.listFriend.observe(this) {
            if (it == null) {
                mViewBinding.btnBottomSheetFriends.text = "(0) Bạn bè"
            } else {
                mViewBinding.btnBottomSheetFriends.text = "(${it.size}) Bạn bè"
            }
        }
        frVModel.getFriendAccepted()
        authViewModel.getInfo()


        //viewpp2
        mViewBinding.viewpp.adapter = object : FragmentStateAdapter(this) {
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
        mViewBinding.btnCamera.setOnClickListener {
            if (currentFragmentPosition != 0) {
                mViewBinding.viewpp.setCurrentItem(0, true)
                currentFragmentPosition = 0
                it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.color_active)
                mViewBinding.btnRecord.backgroundTintList = ContextCompat.getColorStateList(
                    this,
                    R.color.colorbtnctive
                )
            }
        }

        mViewBinding.btnRecord.setOnClickListener {
            if (currentFragmentPosition != 1) {
                mViewBinding.viewpp.setCurrentItem(1, true)
                currentFragmentPosition = 1
                it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.color_active)
                mViewBinding.btnCamera.backgroundTintList = ContextCompat.getColorStateList(
                    this,
                    R.color.colorbtnctive
                )
            }
        }

        mViewBinding.viewpp.isUserInputEnabled = false


        gestureDetector = GestureDetector(this, GestureListener())

        // btnckc

        val bottomSheetDialogFragment = FriendListBottomSheet()
        mViewBinding.btnBottomSheetFriends.setOnClickListener {
            frVModel.resetDynamicLink()
            if (!bottomSheetDialogFragment.isAdded) { // Kiểm tra xem Fragment đã được thêm chưa
                bottomSheetDialogFragment.show(supportFragmentManager, "FriendListBottomSheet")
            }
        }


//diALOGKETBAN
        //  `uid`
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val uid = sharedPreferences.getString("uid", null)

        Log.d("MainActivityApplink", "$uid")
        if (uid != null) {
            frVModel.getinforUserSendlink(uid)

            frVModel.infoUserSendlink.observe(this, Observer { result ->
                if (result.first != null && result.second != null) {
                    showFriendRequestDialog(uid, result.first, result.second)
                    sharedPreferences.edit().clear().apply()
                } else {
                    sharedPreferences.edit().clear().apply()
                    Toast.makeText(this, "Lỗi, vui lòng thử lại sau!", Toast.LENGTH_SHORT).show()
                }
            })
        }

        frVModel.friendRequest.observe(this) { isSuccess ->
            if (isSuccess == true) {
                Snackbar.make(mViewBinding.root, "Gửi thành công!", Snackbar.LENGTH_SHORT)
                    .show()
            } else {
                frVModel.errorMessage.observe(this) {

                    Snackbar.make(mViewBinding.root, "$it", Snackbar.LENGTH_SHORT)
                        .show()
                }

            }
        }


        ///////////// xu li sau dang bai
        postVmodel.postResultLiveData.observe(this) { result ->
            when (result) {
                is PostRepository.PostResult.Success -> {
                    val intent = Intent(this@CreatePostActivity, PostActivity::class.java)
                    val options = ActivityOptions.makeCustomAnimation(
                        this@CreatePostActivity,
                        R.anim.slide_in_up, R.anim.slide_out_up
                    )
                    startActivity(intent, options.toBundle())
                }

                else -> {
                    Toast.makeText(this, "Vui lòng thử lại sau", Toast.LENGTH_SHORT).show()
                }
            }
        }


        //go pot
        mViewBinding.xembai.setOnClickListener {
            postVmodel.stopListeningForNewPosts()
            openPost()
        }


///newpost

        postVmodel.isListeningForNewPosts.observe(this) { isListening ->
            if (isListening) {
                postVmodel.newPostCount.observe(this) {
                    if (it > 0) {
                        mViewBinding.newposttxt.visibility = View.VISIBLE
                        val count = if (it > 9) "9+" else it.toString()
                        mViewBinding.newposttxt.text = count
                    } else {
                        mViewBinding.newposttxt.visibility = View.GONE
                    }
                }
            } else {
                mViewBinding.newposttxt.visibility = View.GONE
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedOnce) {

                    finishAffinity()
                } else {

                    backPressedOnce = true
                    Toast.makeText(
                        this@CreatePostActivity,
                        "Nhấn lần nữa để thoát",
                        Toast.LENGTH_SHORT
                    )
                        .show()

                    Handler(Looper.getMainLooper()).postDelayed({
                        backPressedOnce = false
                    }, 2000)
                }
            }
        })


    }

    private fun openPost() {
        val options = ActivityOptions.makeCustomAnimation(
            this@CreatePostActivity,
            R.anim.slide_in_up, R.anim.slide_out_up
        )
        showActivity(PostActivity::class.java, options.toBundle())
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event!!)
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

                    postVmodel.stopListeningForNewPosts()
                    openPost()
                    return true
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY)
        }
    }


    private fun showFriendRequestDialog(senderuid: String, userName: String?, userAvt: String?) {
        val dialogBinding = FriendRequestDialogBinding.inflate(layoutInflater)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        Glide.with(this).load(userAvt).error(R.drawable.avt_base).into(dialogBinding.imgAvtUserPost)
        dialogBinding.nameFrRequets.text = userName

        dialogBinding.btnSend.setOnClickListener {
            frVModel.handleFriendRequest(senderuid)
            frVModel.errorMessage.observe(this) {
                dialogBinding.btnSend.text = "Bạn bè"
            }
            frVModel.loading.observe(this) { isLoading ->
                if (isLoading == true) {
                    dialogBinding.progressBar.isVisible = isLoading
                    dialogBinding.btnSend.text = ""
                } else {
                    dialogBinding.progressBar.isVisible = isLoading
                }
            }

            frVModel.friendRequest.observe(this) { isSuccess ->
                if (isSuccess == true) {
                    dialogBinding.btnSend.text = "Đã gửi"
                    dialog.dismiss()
                } else {
                    dialogBinding.btnSend.text = ""
                    dialog.dismiss()
                }
            }
        }
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            } else {
                checkAndRequestCameraPermissions()
            }
        } else {
            checkAndRequestCameraPermissions()
        }
    }

    private fun checkAndRequestCameraPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        CameraFragment.getRequiredPermissions().forEach { permission ->
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                CAMERA_PERMISSION_REQUEST_CODE
            )
            val intent = Intent(this, NotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(this, NotificationService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }
                    checkAndRequestCameraPermissions()
                } else {
                    Toast.makeText(this, "Bạn chưa cấp quyền để nhận thông báo", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            CAMERA_PERMISSION_REQUEST_CODE -> {
                val cameraPermissionsGranted =
                    CameraFragment.getRequiredPermissions().all { permission ->
                        ContextCompat.checkSelfPermission(
                            this,
                            permission
                        ) == PackageManager.PERMISSION_GRANTED
                    }
                if (!cameraPermissionsGranted) {
                    Toast.makeText(this, "Bạn chưa cấp quyền để sử dụng camera", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

}