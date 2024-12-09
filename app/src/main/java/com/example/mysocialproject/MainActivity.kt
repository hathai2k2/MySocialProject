package com.example.mysocialproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.mysocialproject.databinding.ActivityMainBinding
import com.example.mysocialproject.ui.base.BaseActivity
import com.example.mysocialproject.ui.base.BaseActivityWithViewModel
import com.example.mysocialproject.ui.feature.friend.FriendViewModel
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivityWithViewModel<ActivityMainBinding,MainViewModel>() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    private fun handleIntent(intent: Intent) {
        Log.d("TAG", "handleIntent: $intent")
        intent.data?.let {
            mViewModel.handleDynamicLink(intent)
        }
    }
}