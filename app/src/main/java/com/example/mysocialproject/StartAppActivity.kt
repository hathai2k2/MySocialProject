package com.example.mysocialproject

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.mysocialproject.databinding.ActivityStartAppBinding
import com.example.mysocialproject.ui.base.BaseActivity
import com.example.mysocialproject.ui.feature.Admin.AdminActivity
import com.example.mysocialproject.ui.feature.home.CreatePostActivity
import com.example.mysocialproject.ui.feature.manageraccount.SignInActivity
import com.example.mysocialproject.ui.feature.manageraccount.SignUp
import com.example.mysocialproject.ui.feature.viewmodel.AuthViewModel
import com.example.mysocialproject.ui.feature.viewmodel.FriendViewmodel
import dagger.hilt.android.AndroidEntryPoint

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class StartAppActivity : BaseActivity<ActivityStartAppBinding>() {

    private  val authViewModel: AuthViewModel by viewModels()
    private val frVModel: FriendViewmodel by viewModels()
    private var isDynamicLinkHandled = false
    override fun getLayoutId(): Int {
        return R.layout.activity_start_app
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)

        frVModel.friendshipResult.observe(this@StartAppActivity) { uid ->
            Log.d("StartAppActivity", "handleIntent: $uid")
            if (uid != null) {
                val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
                sharedPreferences.edit().putString("uid", uid).apply()
                isDynamicLinkHandled = true
                navigateToNextActivity()
            } else {
                isDynamicLinkHandled = true
                navigateToNextActivity()
            }
        }

        mViewBinding.btnDk.setOnClickListener {
            showActivity(SignUp::class.java)
        }

        mViewBinding.btnDn.setOnClickListener {
            showActivity(SignInActivity::class.java)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channel_id",
                "Channel name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

    }

    private fun handleIntent(intent: Intent) {
        intent.data?.let {
            frVModel.handleDynamicLink(intent)
        } ?: run {
            isDynamicLinkHandled = true
            navigateToNextActivity()
        }
    }

    private fun navigateToNextActivity() {
        if (!isDynamicLinkHandled) return

        if (authViewModel.islogined() && !authViewModel.isadmin()) {
            val bundle = Bundle()
            showActivity(CreatePostActivity::class.java, bundle)
            finish()
        } else {
            if (authViewModel.isadmin()) {
                val bundle = Bundle()
                showActivity(
                   AdminActivity::class.java,
                    bundle
                )
                finish()
            }
        }
    }
}