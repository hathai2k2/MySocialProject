package com.example.mysocialproject.ui.feature.user.profile

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mysocialproject.BR
import com.example.mysocialproject.MainViewModel
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentSignupProfileBinding
import com.example.mysocialproject.extension.BackPressHandler
import com.example.mysocialproject.model.UserData
import com.example.mysocialproject.ui.base.BaseFragmentWithViewModel
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class SignupProfileFragment : BaseFragmentWithViewModel<FragmentSignupProfileBinding, ProfileViewModel>(), ProfileNavigation {

    private val mainViewModel: MainViewModel by activityViewModels() // Get ViewModel from Activity
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private var imageUri: Uri? = null

    override fun getLayoutId(): Int = R.layout.fragment_signup_profile

    override fun getViewModelClass(): Class<ProfileViewModel> = ProfileViewModel::class.java

    override fun getBindingVariable(): Int = BR.viewModel

    override fun initViewModel(): Lazy<ProfileViewModel> = viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.setNavigator(this)
        mainViewModel.logUserData()
        resetImageUriIfLoggedOut()
        setupUI()
        // Gọi BackPressHandler để xử lý back press
        BackPressHandler.handleBackPress(viewLifecycleOwner, requireActivity() as AppCompatActivity)
        // Initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                mainViewModel.handleImageResult(requireContext(), it)
            } ?: run {
                Log.e("TAG", "No URI returned.")
            }
        }

        // Observe image URI
        observeImageUri()

        // Observe image URI initialization
        observeImageUriInitialization()
    }

    private fun setupUI() {
        mViewBinding.apply {
            appHeader.onClickLeftIcon {
                findNavController().popBackStack()
            }

            ctAvatar.setOnClickListener {
                chooseAvatar()
            }

            btnSave.setButtonClickListener {
                val userName = edtName.getText()
                if (userName.isNotEmpty()) {
                    imageUri?.let { imgUri ->
                        mViewModel.createProfile(userName, imgUri)
                    }
                }
            }
        }
    }

    private fun chooseAvatar() {
        imagePickerLauncher.launch("image/*")
    }

    private fun observeImageUri() {
        mainViewModel.imgUri.observe(viewLifecycleOwner, Observer { uri ->
            uri?.let {
                Glide.with(requireContext())
                    .load(it)
                    .centerCrop()
                    .circleCrop()
                    .into(mViewBinding.ivImage)
                imageUri = it
            }
        })
    }

    private fun observeImageUriInitialization() {
        mainViewModel.imgUriInitialized.observe(viewLifecycleOwner, Observer { isInitialized ->
            if (isInitialized) {
                // Optionally perform actions after image URI is initialized
            }
        })
    }
    // Reset imageUri if user is logged out
    private fun resetImageUriIfLoggedOut() {
        // Assuming you have some way to check if the user is logged out, for example from ViewModel or Auth
        val isLoggedOut = mainViewModel.isUserLoggedOut() // This could be a function that checks login status
        if (isLoggedOut) {
            imageUri = null
            Glide.with(requireContext())
                .load(R.drawable.ic_user) // Optionally, show a default avatar
                .centerCrop()
                .circleCrop()
                .into(mViewBinding.ivImage)
        }
    }
    override fun onSuccess(userData: UserData) {
        val directions = SignupProfileFragmentDirections.actionGlobalHomeFragment()
        findNavController().navigate(directions)
    }

    override fun onDestroy() {
        super.onDestroy()
        imageUri = null
    }
}
