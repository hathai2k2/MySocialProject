package com.example.mysocialproject.ui.feature.post

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.mysocialproject.BR
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentPostBinding
import com.example.mysocialproject.ui.base.BaseFragmentWithViewModel
import com.example.mysocialproject.ui.dialog.DialogUtil
import com.example.mysocialproject.ui.feature.friend.FriendBottomSheet
import com.example.mysocialproject.ui.feature.friend.FriendViewModel
import com.example.mysocialproject.ui.feature.home.HomeFragmentDirections
import com.example.mysocialproject.ui.feature.home.HomeNavigation
import com.example.mysocialproject.ui.feature.home.HomeViewModel
import com.example.mysocialproject.ui.feature.post.PostPagingAdapter.Companion.VIEW_TYPE_IMAGE
import com.example.mysocialproject.ui.feature.user.profile.ProfileBottomSheet
import com.example.mysocialproject.ui.feature.user.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@AndroidEntryPoint
class PostFragment : BaseFragmentWithViewModel<FragmentPostBinding, PostViewModel>(),HomeNavigation {

    private lateinit var postAdapter: PostPagingAdapter

    private var currentPostPosition = -1
    private var isKeyboardVisible = false

    private lateinit var imm: InputMethodManager
    private var previousUserId: String? = null
    private val homeViewModel: HomeViewModel by viewModels()
    private val profileViewModel : ProfileViewModel by viewModels()
    private val friendViewModel : FriendViewModel by viewModels()

    override fun getViewModelClass(): Class<PostViewModel> {
        return PostViewModel::class.java
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun initViewModel(): Lazy<PostViewModel> {
        return viewModels()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_post
    }

    override fun onResume() {
        super.onResume()
        profileViewModel.getInfo()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.setNavigator(this)
        profileViewModel.getInfo()
        mViewBinding.appHeader3.apply {
            profileViewModel.getUserResult.observe(viewLifecycleOwner) {
                it?.avatarUser?.toUri()?.let { it1 -> setAvatarUri(it1) }
                onClickRightIcon {

                }
                onClickLeftIcon {
                    if (it != null) {
                        ProfileBottomSheet.show(
                            fragmentManager = childFragmentManager,
                            onChangeAvatar = { uri ->
                                if (uri != null) {
                                    profileViewModel.updateAvatar(uri)
                                    mViewBinding.appHeader3.setAvatarUri(uri)
                                }
                            },
                            onChangeName = {
                                DialogUtil.showChangeTextDialog(
                                    context = requireContext(),
                                    label = context.getString(R.string.label_change_name),
                                    onConfirm = { name ->
                                        profileViewModel.updateName(name)
                                        hideKeyboard()
                                    }
                                )
                            },
                            onChangePassword = {
                                DialogUtil.showChangeTextDialog(
                                    context = requireContext(),
                                    label = context.getString(R.string.label_change_password),
                                    onConfirm = { password ->
                                        profileViewModel.updatePassword(password)
                                    }
                                )
                            },
                            onLogout = {
                                homeViewModel.logout()
                            },
                            userData = it
                        )
                    }
                }
            }

        }
        mViewBinding.appBottomNavBar2.apply {
            onTitleClick {
                profileViewModel.getUserResult.observe(viewLifecycleOwner){userData->
                    val action = userData?.let { PostFragmentDirections.actionGlobalHomeFragment() }
                    action?.let { findNavController().navigate(it) }
                }

            }
            onClickLeftIcon {

                val url = postAdapter.getContentFile(currentPostPosition)
                if (url != null) {
                    if (postAdapter.getItemViewType(currentPostPosition) == VIEW_TYPE_IMAGE) {
                        shareImage(requireContext(), url)
                    } else {
                        shareAudio(requireContext(), url)

                    }
                }
            }
            onClickRightIcon {  }
        }
        setupRecyclerView()

        mViewBinding.appHeader3.onClickRightIcon {

        }
        lifecycleScope.launch {
            mViewModel.posts.collectLatest { pagingData ->
                postAdapter.submitData(pagingData)
            }
        }


        postAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                updateUIBasedOnItemCount()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                updateUIBasedOnItemCount()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                updateUIBasedOnItemCount()
            }
        })
        updateUIBasedOnItemCount()

        //lang nghe cuon
        mViewBinding.rcvPost.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val newPosition = layoutManager.findFirstVisibleItemPosition()

                if (newPosition != currentPostPosition) {
                    currentPostPosition = newPosition
                    Log.d("PostListposition", "Current post position: $currentPostPosition")
                    Log.d("PostListposition", "new post position: $newPosition")
                    if (currentPostPosition in 0 until postAdapter.itemCount) {
                        val currentUserId = postAdapter.getPostUserId(currentPostPosition)
                        if (currentUserId != previousUserId) {
                            previousUserId = currentUserId
                            mViewBinding.btnGroupLayout.visibility =
                                if (currentUserId == mViewModel.getUserId()) {
                                    View.GONE
                                } else {
                                    View.VISIBLE
                                }
                        }

                    }
                }
            }
        })
        var isObservingAdapterChanges = true
        val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                if (isObservingAdapterChanges) {
                    // Cập nhật vị trí của bài đăng hiện tại sau khi có thay đổi
                    val layoutManager = mViewBinding.rcvPost.layoutManager as LinearLayoutManager
                    val newPosition = layoutManager.findFirstVisibleItemPosition()
                    currentPostPosition = newPosition

                    // Kiểm tra nếu vị trí bài đăng hợp lệ
                    if (currentPostPosition in 0 until postAdapter.itemCount) {
                        // Lấy User ID của bài đăng hiện tại
                        val currentUserId = postAdapter.getPostUserId(currentPostPosition)
                        if (currentUserId != previousUserId) {
                            previousUserId = currentUserId

                            // Cập nhật hiển thị nút nhóm
                            mViewBinding.btnGroupLayout.visibility =
                                if (currentUserId == mViewModel.getUserId()) {
                                    View.GONE
                                } else {
                                    View.VISIBLE
                                }

                            Log.d(
                                "PostListdelete",
                                "new post at position: $currentPostPosition with new id: $currentUserId"
                            )
                        }
                    }
                }
            }
        }

// Đăng ký đối tượng Observer
        postAdapter.registerAdapterDataObserver(adapterDataObserver)

    }

    private fun updateUIBasedOnItemCount() {
        if (postAdapter.itemCount > 0) {
            mViewBinding.postsContainer.visibility = View.VISIBLE
            mViewBinding.emptyListPost.visibility = View.GONE
        } else {
            mViewBinding.postsContainer.visibility = View.GONE
            mViewBinding.emptyListPost.visibility = View.VISIBLE
        }
    }

    private fun toggleEditTextVisibility() {
        if (!isKeyboardVisible) {
            mViewBinding.realedittextLayout.visibility = View.VISIBLE
            mViewBinding.edtComment.setHint("Trả lời ${postAdapter.getPost(currentPostPosition)?.userName}...")

            mViewBinding.edtComment.clearText()
            showKeyboard()

            isKeyboardVisible = true
        } else {
            hideKeyboard()
            isKeyboardVisible = false
            mViewBinding.realedittextLayout.visibility = View.GONE
        }
    }

    private fun showKeyboard() {
        imm.showSoftInput(mViewBinding.edtComment, InputMethodManager.SHOW_IMPLICIT)
        isKeyboardVisible = true
    }

    private fun setupRecyclerView() {
        mViewBinding.rcvPost.setHasFixedSize(true)
        mViewBinding.rcvPost.layoutManager = LinearLayoutManager(requireContext())
        val snapHelper: SnapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(mViewBinding.rcvPost)

        postAdapter = PostPagingAdapter(
            isCurrentUser = { post -> post.userId == mViewModel.getUserId() },
            lifecycleOwner = viewLifecycleOwner,
            context = requireContext(),
            onPostViewed = { postId -> mViewModel.onPostViewed(postId) }
        )
        mViewBinding.rcvPost.adapter = postAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun shareImage(context: Context, imageUrl: String) {
        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    try {
                        val file = File(
                            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            "shared_image.png"
                        )
                        val out =
                            FileOutputStream(file)
                        resource.compress(Bitmap.CompressFormat.PNG, 100, out)
                        out.flush()
                        out.close()
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        shareUri(context, uri, "image/*")
                    } catch (e: IOException) {
                        Log.e("ShareImage", "không thể lưu ảnh", e)
                    } finally {
                        requireActivity().runOnUiThread {
                        }
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun shareAudio(context: Context, audioUrl: String) {
        Thread {
            try {
                val url = URL(audioUrl)
                val connect = url.openConnection() as HttpURLConnection
                connect.requestMethod = "GET"
                connect.connect()

                if (connect.responseCode == HttpURLConnection.HTTP_OK) {
                    val file = File(
                        context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                        "shared_audio.mp3"
                    )
                    val inputStream = connect.inputStream
                    val outputStream = FileOutputStream(file)

                    val buffer = ByteArray(1024)
                    var len: Int
                    while (inputStream.read(buffer).also { len = it } != -1) {
                        outputStream.write(buffer, 0, len)
                    }

                    outputStream.close()
                    inputStream.close()

                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    shareUri(context, uri, "audio/*")
                }
            } catch (e: IOException) {
                Log.e("ShareAudio", "không thể lưu voice", e)
            } finally {
                requireActivity().runOnUiThread {
                }
            }
        }.start()
    }

    private fun shareUri(context: Context, uri: Uri, ty: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = ty
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, null))
    }

    override fun onLogOut() {
        val action = PostFragmentDirections.actionGlobalSplashFragment()
        findNavController().navigate(action)
    }

    override fun onOpenPost() {

    }
}
