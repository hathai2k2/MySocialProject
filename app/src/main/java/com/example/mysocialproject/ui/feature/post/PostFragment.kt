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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.mysocialproject.BR
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentPostBinding
import com.example.mysocialproject.model.PostData
import com.example.mysocialproject.ui.base.BaseFragmentWithViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@AndroidEntryPoint
class PostListFragment : BaseFragmentWithViewModel<FragmentPostBinding, PostViewModel>() {

    private lateinit var postAdapter: PostPagingAdapter

    private var currentPostPosition = -1
    private var isKeyboardVisible = false

    private lateinit var imm: InputMethodManager
    private var previousUserId: String? = null

    private val samplePostDataLists = listOf(
        PostData("1", "User 1", "https://example.com/image1.jpg", "Sample content 1"),
        PostData("2", "User 2", "https://example.com/image2.jpg", "Sample content 2"),
        PostData("3", "User 3", "https://example.com/image3.jpg", "Sample content 3")
    )

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        setupRecyclerView()

//
//
//        mViewBinding.swipeRefreshLayout.setOnRefreshListener {
//            mViewBinding.swipeRefreshLayout.isRefreshing = true
//
//            postAdapter.refresh()
//            mViewBinding.shimmerLayout.startShimmer()
//            mViewBinding.shimmerLayout.visibility = View.VISIBLE
//
//            lifecycleScope.launch {
//                postAdapter.loadStateFlow.collectLatest { loadStates ->
//                    if (loadStates.refresh is LoadState.NotLoading) {
//                        mViewBinding.rcvPost.scrollToPosition(0)
//                        mViewBinding.swipeRefreshLayout.isRefreshing = false
//                        mViewBinding.shimmerLayout.stopShimmer()
//                        mViewBinding.shimmerLayout.visibility = View.GONE
//                    }
//                }
//            }
//        }
//
//        lifecycleScope.launch {
//            postAdapter.loadStateFlow.collectLatest { loadStates ->
//                if (loadStates.refresh is LoadState.Loading) {
//                    mViewBinding.shimmerLayout.startShimmer()
//                    mViewBinding.shimmerLayout.visibility = View.VISIBLE
//                } else {
//                    mViewBinding.shimmerLayout.stopShimmer()
//                    mViewBinding.shimmerLayout.visibility = View.GONE
//
//                    val errorState = when {
//                        loadStates.refresh is LoadState.Error -> loadStates.refresh as LoadState.Error
//                        loadStates.prepend is LoadState.Error -> loadStates.prepend as LoadState.Error
//                        loadStates.append is LoadState.Error -> loadStates.append as LoadState.Error
//                        else -> null
//                    }
//
//                    if (errorState != null) {
//                        mViewBinding.shimmerLayout.stopShimmer()
//                        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
//                            .setTitle("Lỗi kết nối")
//                            .setMessage("Không thể kết nối, vui lòng kiểm tra kết nối mạng của bạn.")
//                            .setPositiveButton("Thử lại") { it, _ ->
//                                postAdapter.retry()
//                                it.dismiss()
//                            }
//                            .setCancelable(true)
//                            .show()
//                    } else {
//                        postAdapter.registerAdapterDataObserver(object :
//                            RecyclerView.AdapterDataObserver() {
//                            override fun onChanged() {
//                                super.onChanged()
//                                updateUIBasedOnItemCount()
//                            }
//
//                            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//                                super.onItemRangeInserted(positionStart, itemCount)
//                                updateUIBasedOnItemCount()
//                            }
//
//                            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
//                                super.onItemRangeRemoved(positionStart, itemCount)
//                                updateUIBasedOnItemCount()
//                            }
//                        })
//                        updateUIBasedOnItemCount()
//                    }
//                }
//            }
//        }
//
//        mViewBinding.rcvPost.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//                val newPosition = layoutManager.findFirstVisibleItemPosition()
//
//                if (newPosition != currentPostPosition) {
//                    currentPostPosition = newPosition
//                    Log.d("PostListposition", "Current post position: $currentPostPosition")
//                    Log.d("PostListposition", "new post position: $newPosition")
//                    if (currentPostPosition in 0 until postAdapter.itemCount) {
//                        val currentUserId = postAdapter.getPostUserId(currentPostPosition)
//                        if (currentUserId != previousUserId) {
//                            previousUserId = currentUserId
//                            mViewBinding.btnGroupLayout.visibility = View.GONE
//                        }
//
//                        mViewBinding.appBottomNavBar2.onClickLeftIcon {
//                            val url = postAdapter.getContentFile(currentPostPosition)
//                            if (url != null) {
//                                if (postAdapter.getItemViewType(currentPostPosition) == VIEW_TYPE_IMAGE) {
//                                    shareImage(requireContext(), url)
//                                } else {
//                                    shareAudio(requireContext(), url)
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        })
//
//        mViewBinding.appBottomNavBar2.onClickCenterIcon {
//            findNavController().popBackStack()
//        }
//        imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//
//        mViewBinding.fakeedittext.setOnClickListener {
//            toggleEditTextVisibility()
//        }
//
//        mViewBinding.realedittextLayout.setOnClickListener {
//            hideKeyboard()
//            mViewBinding.realedittextLayout.visibility = View.GONE
//        }
//
//        var isObservingAdapterChanges = true
//
//        val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
//            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
//                if (isObservingAdapterChanges) {
//                    super.onItemRangeRemoved(positionStart, itemCount)
//
//                    val layoutManager = mViewBinding.rcvPost.layoutManager as LinearLayoutManager
//                    val newPosition = layoutManager.findFirstVisibleItemPosition()
//
//                    currentPostPosition = newPosition
//
//                    if (currentPostPosition in 0 until postAdapter.itemCount) {
//                        Log.d(
//                            "PostListdelete",
//                            "new post at position: $currentPostPosition with new id: ${
//                                postAdapter.getPostUserId(currentPostPosition)
//                            }"
//                        )
//                        val currentUserId = postAdapter.getPostUserId(currentPostPosition)
//                        if (currentUserId != previousUserId) {
//                            previousUserId = currentUserId
//                            mViewBinding.btnGroupLayout.visibility = View.GONE
//                        }
//                    }
//                }
//            }
//        }
//
//        postAdapter.registerAdapterDataObserver(adapterDataObserver)
//
//        mViewBinding.appBottomNavBar2.onClickRightIcon {
//            val post = postAdapter.getPost(currentPostPosition) ?: return@onClickRightIcon
//            val postId = post.postId
//            val userPostId = post.userId
//            MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
//                .setTitle("Xóa bài viết")
//                .setMessage("Bạn có chắc chắn muốn xóa bài đăng này?")
//                .setPositiveButton("Xóa") { dialog, _ ->
//                    dialog.dismiss()
//                }
//                .setNegativeButton("Hủy", null)
//                .show()
//        }
//
//        mViewBinding.appHeader3.onClickCenter {
//            //show profile bottom
//        }

        mViewBinding.appHeader3.onClickRightIcon {
            val direction = PostFragmentDirections.actionGlobalChatFragment()
            findNavController().navigate(direction)
        }
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
            isCurrentUser = { post -> post.userId == "user_1" },
            lifecycleOwner = viewLifecycleOwner,
            context = requireContext(),
            activity = requireActivity(),
            onPostViewed = { postId -> }
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
}
