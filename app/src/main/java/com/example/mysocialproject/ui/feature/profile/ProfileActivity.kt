package com.example.mysocialproject.ui.feature.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ActivityProfileBinding
import com.example.mysocialproject.ui.base.BaseActivity
import com.example.mysocialproject.ui.feature.home.CreatePostActivity
import com.example.mysocialproject.ui.feature.viewmodel.AuthViewModel
import com.example.mysocialproject.ui.feature.widget.TutorialBottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : BaseActivity<ActivityProfileBinding>() {

    private var currentEditProfileBottomSheet: EditProfileBottomSheet? = null

    override fun getLayoutId(): Int = R.layout.activity_profile

    private lateinit var authViewModel: AuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        mViewBinding.lifecycleOwner = this
        mViewBinding.viewModel = authViewModel
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        authViewModel.getUserResult.observe(this) { user ->
            user?.let {
                updateAvatar(it.avatarUser)
                mViewBinding.tvNameUser.text = it.nameUser
            }
        }

        authViewModel.UpdateError.observe(this) { error ->
            currentEditProfileBottomSheet?.dismissDialogWithMessage(error)
        }

        authViewModel.updateResult.observe(this) { isSuccess ->
            showUpdateResultSnackbar(isSuccess)
        }

        authViewModel.loading.observe(this) { isLoading ->
            toggleLoadingIndicator(isLoading)
        }

    }

    private fun setupListeners() {

        mViewBinding.btnLogout.setOnClickListener {
            authViewModel.logout(this)
        }

        mViewBinding.btnAvatar.setOnClickListener {
            authViewModel.chooseAvatar(this)
        }

        mViewBinding.btnEditName.setOnClickListener {
            showEditProfileDialog(EditProfileBottomSheet.DIALOG_TYPE_NAME)
        }

        mViewBinding.btnEditPassword.setOnClickListener {
            showEditProfileDialog(EditProfileBottomSheet.DIALOG_TYPE_PASSWORD)
        }

        mViewBinding.tvDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }

        mViewBinding.btngoghome.setOnClickListener {
            navigateToCreatePostActivity()
        }

        mViewBinding.chipWidgetPost.setOnClickListener {
            showTutorialDialog(TutorialBottomSheetDialogFragment.DIALOG_TYPE_WIDGETPOST)
        }

        mViewBinding.chipWidgetChat.setOnClickListener {
            showTutorialDialog(TutorialBottomSheetDialogFragment.DIALOG_TYPE_WIDGETCHAT)
        }
    }

    private fun updateAvatar(avatarUrl: String?) {
        avatarUrl?.let {
            Glide.with(this)
                .load(it)
                .error(R.drawable.avt_defaut)
                .transform(CircleCrop())
                .override(300, 200)
                .into(mViewBinding.imgAvtUse)
        }
    }

    private fun showUpdateResultSnackbar(isSuccess: Boolean) {
        val message = if (isSuccess) "Cập nhật đại diện thành công" else "Vui lòng thử lại sau"
        Snackbar.make(mViewBinding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun toggleLoadingIndicator(isLoading: Boolean) {
        mViewBinding.shimmerChat.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) mViewBinding.shimmerChat.startShimmer() else mViewBinding.shimmerChat.stopShimmer()
    }

    private fun showEditProfileDialog(dialogType: Int) {
        val dialog = EditProfileBottomSheet().apply {
            arguments = Bundle().apply { putInt("dialogType", dialogType) }
        }
        currentEditProfileBottomSheet = dialog
        dialog.show(supportFragmentManager, "update_profile_dialog")
    }

    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle("Xác nhận xóa tài khoản")
            .setMessage("Bạn có chắc chắn muốn xóa tài khoản của mình không?")
            .setPositiveButton("Xóa") { dialog, _ ->
                authViewModel.deleteAccount(this)
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun navigateToCreatePostActivity() {
        showActivity(CreatePostActivity::class.java)
        finish()
    }

    private fun showTutorialDialog(dialogType: Int) {
        val dialog = TutorialBottomSheetDialogFragment().apply {
            arguments = Bundle().apply { putInt("dialogTypeWidget", dialogType) }
        }
        dialog.show(supportFragmentManager, "TutorialBottomSheetDialogFragment")
    }

    // Khi kết thúc Activity
    override fun finish() {
        super.finish()
        if (authViewModel.islogined()) {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        authViewModel.handleImageResult(requestCode, resultCode, data,this, mViewBinding.imgAvtUse)
    }

}
