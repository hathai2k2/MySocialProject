package com.example.mysocialproject

import android.os.Bundle
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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivityWithViewModel<ActivityMainBinding,MainViewModel>() {
    override fun getViewModelClass(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }
    private val mainViewModel: MainViewModel by viewModels()
    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    private val navHost by lazy { (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment) }
    val navController by lazy { navHost.navController }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}