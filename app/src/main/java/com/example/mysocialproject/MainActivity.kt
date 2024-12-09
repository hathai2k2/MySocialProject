package com.example.mysocialproject

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.example.mysocialproject.databinding.ActivityMainBinding
import com.example.mysocialproject.ui.base.BaseActivityWithViewModel
import com.example.mysocialproject.ui.dialog.DialogUtil
import com.example.mysocialproject.ui.feature.friend.FriendViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivityWithViewModel<ActivityMainBinding, MainViewModel>(),
    MainNavigation {

    override fun getViewModelClass(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    private val navHost by lazy { (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment) }
    val navController by lazy { navHost.navController }

    private val friendViewModel: FriendViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.setNavigator(this)

        // Xử lý Intent khi ứng dụng được khởi tạo
        intent?.let {
            handleIntent(it)
        }
        Log.d("TAG", "onCreate Intent handled")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("TAG", "onNewIntent called")
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        Log.d("TAG", "handleIntent: $intent")
        intent.data?.let {
            mViewModel.handleDynamicLink(intent)
        }

    }

    override fun setCurrentId(uid: String) {
        showDialog(uid)

    }

    fun showDialog(uid: String) {
        Log.d("TAG", "showDialog: $uid")
        friendViewModel.getinforUserSendlink(uid)

        friendViewModel.infoUserSendlink.observe(this) { result ->
            val userName = result.first
            val avatar = result.second
            Handler(Looper.getMainLooper()).postDelayed({
                if (userName != null && avatar != null) {
                    DialogUtil.showAddFriendDialog(
                        context = this,
                        friendViewModel = friendViewModel,
                        onConfirm = {
                            Log.d("TAG", "onConfirm: $uid")
                            friendViewModel.handleFriendRequest(uid)
                        }
                    )
                    mViewModel.clearPref()
                } else {
                    Log.d("TAG", "NULL DIALOG")
                    Toast.makeText(
                        this,
                        "Lỗi, vui lòng thử lại sau!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, 300)
        }
    }
    fun logoutAndReset() {
        // Clear preferences (hoặc trạng thái đăng nhập)
        mViewModel.clearPref()

        // Khởi tạo Intent với các flags để tái tạo lại Activity
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Bắt đầu lại MainActivity
        startActivity(intent)

        // Đóng Activity hiện tại (MainActivity hoặc Activity khác)
        finish()
    }
}
