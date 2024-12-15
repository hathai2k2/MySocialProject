package com.example.mysocialproject.ui.feature.Admin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ActivityAdminBinding
import com.example.mysocialproject.ui.feature.viewmodel.AuthViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class AdminActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var binding: ActivityAdminBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_admin)

        binding.lifecycleOwner = this
        binding.viewModel = authViewModel

        //viewpp2
        binding.viewpp.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 2 // Số lượng Fragment
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> UsersFragment() // Fragment users
                    else -> PostsFragment() // Fragment bai dang
                }
            }
        }
        TabLayoutMediator(binding.tabLayout, binding.viewpp) { tab, position ->
            tab.text = when (position) {
                0 -> "Danh sách người dùng"
                else -> "Danh sách Bài đăng"
            }
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.viewpp.currentItem = tab?.position ?: 0
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })


        binding.btnGoAppAnalytics.setOnClickListener {
            val appPackageName = "com.google.android.apps.giant"

            val launchIntent = packageManager.getLaunchIntentForPackage(appPackageName)
            if (launchIntent != null) {

                startActivity(launchIntent)
            } else {

                val playStoreIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
                startActivity(playStoreIntent)
            }
        }
        binding.btnGoweb.setOnClickListener {
            val url =
                "https://analytics.google.com/analytics/web/#/p467386640/reports/intelligenthome"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        binding.btnLogout.setOnClickListener {
            authViewModel.logout(this)
        }


    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (authViewModel.islogined()) {
            finishAffinity()
        } else {

        }
    }
}