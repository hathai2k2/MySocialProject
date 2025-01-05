package com.example.mysocialproject.ui.feature.post

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ActivityPostBinding
import com.example.mysocialproject.extension.BackPressHandler
import com.example.mysocialproject.ui.base.BaseActivity
import com.example.mysocialproject.ui.feature.adapter.PostPagingAdapter
import com.example.mysocialproject.ui.feature.adapter.PostPagingAdapter.Companion.VIEW_TYPE_IMAGE
import com.example.mysocialproject.ui.feature.chat.ChatActivity
import com.example.mysocialproject.ui.feature.home.CreatePostActivity
import com.example.mysocialproject.ui.feature.profile.ProfileActivity
import com.example.mysocialproject.ui.feature.viewmodel.MessageViewModel
import com.example.mysocialproject.ui.feature.viewmodel.PostViewModel
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@RequiresApi(Build.VERSION_CODES.O)
class PostActivity : BaseActivity<ActivityPostBinding>() {

    lateinit var postApdapter: PostPagingAdapter
    private val postViewModel: PostViewModel by viewModels()
    private val messViewModel: MessageViewModel by viewModels()
    private var currentPostPosition = -1
    private var isKeyboardVisible = false
    private lateinit var imm: InputMethodManager
    private var previousUserId: String? = null
    private var fabStatus:Boolean = false
    override fun getLayoutId(): Int {
        return R.layout.activity_post
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BackPressHandler.handleBackPress(this, this)
        setupRecyclerView()
        initFAB()
        mViewBinding.swipeRefreshLayout.setOnRefreshListener {
            mViewBinding.swipeRefreshLayout.isRefreshing = true
            mViewBinding.btnNewpost.visibility = View.INVISIBLE
            postViewModel.stopListeningForNewPosts()
            postApdapter.refresh()
            mViewBinding.shimmerLayout.startShimmer()
            mViewBinding.shimmerLayout.visibility = View.VISIBLE

            //  trạng thái tải dữ liệu
            lifecycleScope.launch {
                postApdapter.loadStateFlow.collectLatest { loadStates ->
                    if (loadStates.refresh is LoadState.NotLoading) {
                        mViewBinding.recyclerView.scrollToPosition(0)
                        mViewBinding.swipeRefreshLayout.isRefreshing = false
                        mViewBinding.shimmerLayout.stopShimmer()
                        mViewBinding.shimmerLayout.visibility = View.GONE
                    }
                }
            }
        }
///gan dl vao paging
        lifecycleScope.launch {
            postViewModel.posts
                .collectLatest { pagingData ->
                    postApdapter.submitData(pagingData)
                }
        }


        lifecycleScope.launch {
            postApdapter.loadStateFlow.collectLatest { loadStates ->
                if (loadStates.refresh is LoadState.Loading) {
                    mViewBinding.shimmerLayout.startShimmer()
                    mViewBinding.shimmerLayout.visibility = View.VISIBLE
                } else {
                    mViewBinding.shimmerLayout.stopShimmer()
                    mViewBinding.shimmerLayout.visibility = View.GONE

                    val errorState = when {
                        loadStates.refresh is LoadState.Error -> loadStates.refresh as LoadState.Error
                        loadStates.prepend is LoadState.Error -> loadStates.prepend as LoadState.Error
                        loadStates.append is LoadState.Error -> loadStates.append as LoadState.Error
                        else -> null
                    }

                    if (errorState != null) {
                        mViewBinding.shimmerLayout.stopShimmer()
                        AlertDialog.Builder(this@PostActivity, R.style.AlertDialogTheme)
                            .setTitle("Lỗi kết nối")
                            .setMessage("Không thể kết nối, vui lòng kiểm tra kết nối mạng của bạn.")
                            .setPositiveButton("Thử lại") { it, _ ->
                                postApdapter.retry()
                                it.dismiss()
                            }
                            .setCancelable(true)
                            .show()
                    } else {
                        postApdapter.registerAdapterDataObserver(object :
                            RecyclerView.AdapterDataObserver() {
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
                    }
                }
            }
        }


//lang nghe cuon
        mViewBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val newPosition = layoutManager.findFirstVisibleItemPosition()

                if (newPosition != currentPostPosition) {
                    currentPostPosition = newPosition
                    Log.d("PostListposition", "Current post position: $currentPostPosition")
                    Log.d("PostListposition", "new post position: $newPosition")
                    if (currentPostPosition in 0 until postApdapter.itemCount) {
                        val currentUserId = postApdapter.getPostUserId(currentPostPosition)
                        if (currentUserId != previousUserId) {
                            previousUserId = currentUserId
                            mViewBinding.btnGroupLayout.visibility =
                                if (currentUserId == postViewModel.getcurrentId()) {
                                    View.GONE
                                } else {
                                    View.VISIBLE
                                }
                        }

                    }
                }
            }
        })
        //tạo animation
        val likeAnimation = listOf(
            AnimationUtils.loadAnimation(this, R.anim.heartflyy),
            AnimationUtils.loadAnimation(this, R.anim.heartflyy),
            AnimationUtils.loadAnimation(this, R.anim.heartflyy),
            AnimationUtils.loadAnimation(this, R.anim.heartflyy)
        )

        //set clicked icon
        mViewBinding.apply {
            btnHeart.setOnClickListener {
                val postId = postApdapter.getPostId(currentPostPosition)
                postViewModel.likePost(postId.toString(), "ic_heart")
                imgHeart.startAnimation(likeAnimation[0])
                likeAnimation[0].setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                        imgHeart.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        imgHeart.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation?) {

                    }
                })

            }
            btnHaha.setOnClickListener {
                val postId = postApdapter.getPostId(currentPostPosition)
                postViewModel.likePost(postId.toString(), "ic_haha")
                imgHaha.startAnimation(likeAnimation[1])
                likeAnimation[1].setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                        imgHaha.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        imgHaha.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation?) {

                    }

                })
            }
            btnSad.setOnClickListener {
                val postId = postApdapter.getPostId(currentPostPosition)
                postViewModel.likePost(postId.toString(), "ic_sad")
                imgSad.startAnimation(likeAnimation[2])
                likeAnimation[2].setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                        imgSad.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        imgSad.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation?) {

                    }

                })
            }
            btnAngry.setOnClickListener {
                val postId = postApdapter.getPostId(currentPostPosition)
                postViewModel.likePost(postId.toString(), "ic_angry")
                imgAngry.startAnimation(likeAnimation[3])
                likeAnimation[3].setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                        imgAngry.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        imgAngry.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation?) {

                    }
                })
            }
        }

        imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager


        //
        mViewBinding.fakeedittext.setOnClickListener {
            toggleEditTextVisibility()
        }

        // để ẩn bàn phím
        mViewBinding.realedittextLayout.setOnClickListener {
            hideKeyboard()
            mViewBinding.realedittextLayout.visibility = View.GONE
        }

        mViewBinding.mVmodel = messViewModel

//cmt
        messViewModel.messagesend.observe(this) {
            if (it != null && it!!.length > 0) {
                mViewBinding.btnSend.isEnabled = true
                mViewBinding.btnSend.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.color_active)
            } else {
                mViewBinding.btnSend.isEnabled = false
                mViewBinding.btnSend.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.bg_input)
            }
        }

        //gui di
        mViewBinding.btnSend.setOnClickListener {
            val content = postApdapter.getPost(currentPostPosition)!!.content ?: ""
            val userAvt = postApdapter.getPost(currentPostPosition)!!.userAvatar ?: ""
            val imgUrl = postApdapter.getPost(currentPostPosition)!!.photoURL
            val VoiceUrl = postApdapter.getPost(currentPostPosition)!!.voiceURL
            val receiverId = postApdapter.getPost(currentPostPosition)!!.userId
            val postId = postApdapter.getPost(currentPostPosition)!!.postId
            val timeAgo =
                TimeAgo.using(postApdapter.getPost(currentPostPosition)!!.createdAt!!.toDate().time)
            val createAt = timeAgo


            messViewModel.sendMessage(
                postId,
                receiverId,
                imgUrl.toString(),
                VoiceUrl.toString(),
                content,
                createAt,
                userAvt
            )

            messViewModel.sendSuccess.observe(this) {
                hideKeyboard()
                mViewBinding.realedittextLayout.visibility = View.GONE
                mViewBinding.realedittext.setText("")
                Snackbar.make(mViewBinding.root, "Gửi thành công!", Snackbar.LENGTH_SHORT).show()
            }
        }


        var isObservingAdapterChanges = true
//xoa bai
        postViewModel.deletePost.observe(this) { isDeleted ->
            if (isDeleted == true) {
                postViewModel.invalidatePagingSource()
                postApdapter.refresh()
                Handler(Looper.getMainLooper()).postDelayed({
                    isObservingAdapterChanges = false
                }, 1000)


                Snackbar.make(mViewBinding.root, "Xóa thành công!", Snackbar.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Vui lòng thử lại sau", Toast.LENGTH_SHORT).show()
            }
        }


        val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {

                if (isObservingAdapterChanges) {
                    super.onItemRangeRemoved(positionStart, itemCount)

                    val layoutManager =
                        mViewBinding.recyclerView.layoutManager as LinearLayoutManager
                    val newPosition = layoutManager.findFirstVisibleItemPosition()

                    currentPostPosition = newPosition

                    if (currentPostPosition in 0 until postApdapter.itemCount) {

                        Log.d(
                            "PostListdelete",
                            "new post at position: $currentPostPosition with new id: ${
                                postApdapter.getPostUserId(currentPostPosition)
                            }"
                        )
                        val currentUserId = postApdapter.getPostUserId(currentPostPosition)
                        if (currentUserId != previousUserId) {
                            previousUserId = currentUserId
                            mViewBinding.btnGroupLayout.visibility =
                                if (currentUserId == postViewModel.getcurrentId()) {
                                    View.GONE
                                } else {
                                    View.VISIBLE
                                }
                        }
                    }

                }
            }
        }

        postApdapter.registerAdapterDataObserver(adapterDataObserver)

        //profile
        mViewBinding.btnBottomSheetProfile.setOnClickListener {
            // Đóng activity hiện tại
            finish()
            // Mở ProfileActivity
            val intent = Intent(this, ProfileActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_up, R.anim.slide_out_up
            )
            startActivity(intent, options.toBundle())
        }


//messs

        mViewBinding.btnMessage.setOnClickListener {
            intent = Intent(this, ChatActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_up, R.anim.slide_out_up
            )
            startActivity(intent, options.toBundle())
        }


        //newpost

        postViewModel.isListeningForNewPosts.observe(this) { isListening ->
            if (isListening) {
                postViewModel.newPostCount.observe(this) {
                    if (it > 0) {
                        mViewBinding.btnNewpost.visibility = View.VISIBLE
                        val count = if (it > 9) "9+" else it.toString()
                        mViewBinding.btnNewpost.text = "Mới(${count})"
                    } else {
                        mViewBinding.btnNewpost.visibility = View.INVISIBLE
                    }
                }
            } else {
                mViewBinding.btnNewpost.visibility = View.INVISIBLE
            }
        }

        mViewBinding.btnNewpost.setOnClickListener {
            mViewBinding.swipeRefreshLayout.isRefreshing = true
            postViewModel.stopListeningForNewPosts()
            mViewBinding.btnNewpost.visibility = View.INVISIBLE
            mViewBinding.recyclerView.smoothScrollToPosition(0)
            postApdapter.refresh()
            mViewBinding.shimmerLayout.startShimmer()
            mViewBinding.shimmerLayout.visibility = View.VISIBLE



            lifecycleScope.launch {
                postApdapter.loadStateFlow.collectLatest { loadStates ->
                    if (loadStates.refresh is LoadState.NotLoading) {
                        mViewBinding.swipeRefreshLayout.isRefreshing = false
                        mViewBinding.recyclerView.scrollToPosition(0)
                        mViewBinding.shimmerLayout.stopShimmer()
                        mViewBinding.shimmerLayout.visibility = View.GONE
                    }
                }
            }
        }


    }

    private fun openDialogDelete() {
        val post = postApdapter.getPost(currentPostPosition) ?: return
        val postId = post.postId
        val userPostId = post.userId
        Log.d(
            "PostListdelete",
            "Deleting post at position: $currentPostPosition with postId: $userPostId"
        )
        if (userPostId == postViewModel.getcurrentId()) {
            MaterialAlertDialogBuilder(this@PostActivity, R.style.AlertDialogTheme)
                .setTitle("Xóa bài viết")
                .setMessage("Bạn có chắc chắn muốn xóa bài đăng này?")
                .setPositiveButton("Xóa") { dialog, _ ->
                    postViewModel.deletePost(postId)

                    dialog.dismiss()
                }
                .setNegativeButton("Hủy", null)
                .show()

        } else {
            MaterialAlertDialogBuilder(this@PostActivity, R.style.AlertDialogTheme)
                .setTitle("Xóa bài viết")
                .setMessage("Bài đăng sẽ không hiển thị cho bạn nhưng vẫn có thể hiển thị ở một nơi khác!")
                .setPositiveButton("Ẩn") { dialog, _ ->
                    postViewModel.deletePost(postId)
                    dialog.dismiss()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    private fun initFAB() {
        mViewBinding.apply {
            mainFab.setOnClickListener {
                if (!fabStatus) {
                    expandFAB()
                } else {
                    hideFAB()
                }
            }
        }
    }

    private fun checkHideFAB() {
        if (fabStatus) {
            hideFAB()
        }
    }


    private fun expandFAB() {
        fabStatus = true
        val showFab1 = AnimationUtils.loadAnimation(this, R.anim.fab1_show)
        val showFab2 = AnimationUtils.loadAnimation(this, R.anim.fab2_show)
        val showFab3 = AnimationUtils.loadAnimation(this, R.anim.fab3_show)
        mViewBinding.apply {
            mainFab.animate()
                .rotation(135f)
                .setDuration(200)
                .setInterpolator(LinearInterpolator()).start()
                showItemFAB(aFab.fab1, showFab1, 1.7, 0.0, onClickFAB = {
                val url = postApdapter.getContentFile(currentPostPosition)
                if (url != null) {
                    if (postApdapter.getItemViewType(currentPostPosition) == VIEW_TYPE_IMAGE) {
                        shareImage(this@PostActivity, url)
                    } else {
                        shareAudio(this@PostActivity, url)
                    }
                }
            })
            showItemFAB(aFab.fab2, showFab2, 1.2, 1.2, onClickFAB = {
                this@PostActivity.onStop()
                showActivity(CreatePostActivity::class.java, enter = R.anim.slide_out_down, exit = R.anim.slide_in_down)
            })
            showItemFAB(aFab.fab3, showFab3, 0.0, 1.7, onClickFAB = {
                openDialogDelete()
            })

        }
    }

    private fun hideFAB() {
        fabStatus = false
        val hideFab1 = AnimationUtils.loadAnimation(this, R.anim.fab1_hide)
        val hideFab2 = AnimationUtils.loadAnimation(this, R.anim.fab2_hide)
        val hideFab3 = AnimationUtils.loadAnimation(this, R.anim.fab3_hide)

        mViewBinding.apply {
            mainFab.animate()
                .rotation(0f)
                .setDuration(200)
                .setInterpolator(LinearInterpolator()).start()
            hideItemFAB(aFab.fab1, hideFab1, 1.7, 0.0,)
            hideItemFAB(aFab.fab2, hideFab2, 1.2, 1.2)
            hideItemFAB(aFab.fab3, hideFab3, 0.0, 1.7)
        }
    }
    private fun showItemFAB(
        fab: FloatingActionButton,
        showFab: Animation?,
        width: Double,
        height: Double,
        onClickFAB:()->Unit
    ) {
        mViewBinding.apply {
            val layoutParams = fab.layoutParams as FrameLayout.LayoutParams
            layoutParams.rightMargin += (fab.width * width).toInt()
            layoutParams.bottomMargin += (fab.height * height).toInt()
            fab.layoutParams = layoutParams
            fab.startAnimation(showFab)
            fab.isClickable = true
            fab.visibility = View.VISIBLE
            fab.setOnClickListener{onClickFAB()}
        }
    }

    private fun hideItemFAB(
        fab: FloatingActionButton,
        hideFab: Animation?,
        width: Double,
        height: Double
    ) {
        mViewBinding.apply {
            val layoutParams = fab.layoutParams as FrameLayout.LayoutParams
            layoutParams.rightMargin -= (fab.width * width).toInt()
            layoutParams.bottomMargin -= (fab.height * height).toInt()
            fab.layoutParams = layoutParams
            fab.startAnimation(hideFab)
            fab.isClickable = false
            fab.visibility = View.INVISIBLE
        }

    }

    private fun updateUIBasedOnItemCount() {
        if (postApdapter.itemCount > 0) {
            mViewBinding.postsContainer.visibility = View.VISIBLE
            mViewBinding.emptyListPost.visibility = View.GONE
            mViewBinding.fabCreatePost.visibility = View.GONE
        } else {
            mViewBinding.postsContainer.visibility = View.GONE
            mViewBinding.emptyListPost.visibility = View.VISIBLE
            mViewBinding.fabCreatePost.visibility = View.VISIBLE
            mViewBinding.fabCreatePost.setOnClickListener {
                showActivity(
                    CreatePostActivity::class.java,
                    enter = R.anim.slide_out_down,
                    exit = R.anim.slide_in_down
                )
            }
        }
    }

    private fun toggleEditTextVisibility() {
        if (!isKeyboardVisible) {
            mViewBinding.realedittextLayout.visibility = View.VISIBLE
            mViewBinding.realedittext.hint =
                "Trả lời ${postApdapter.getPost(currentPostPosition)?.userName}..."

            mViewBinding.realedittext.requestFocus()
            showKeyboard()

            isKeyboardVisible = true
        } else {
            hideKeyboard()
            isKeyboardVisible = false
            mViewBinding.realedittextLayout.visibility = View.GONE
        }
    }

    private fun showKeyboard() {
        imm.showSoftInput(mViewBinding.realedittext, InputMethodManager.SHOW_IMPLICIT)
        isKeyboardVisible = true
    }

    private fun hideKeyboard() {
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            currentFocus == null
            isKeyboardVisible = false
        }
    }


    private fun setupRecyclerView() {

        mViewBinding.recyclerView.setHasFixedSize(true)
        mViewBinding.recyclerView.layoutManager = LinearLayoutManager(this)
        val snapHelper: SnapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(mViewBinding.recyclerView)

        postApdapter = PostPagingAdapter(
            isCurrentUser = { post -> post.userId == postViewModel.iscurrentId() },
            postViewModel,
            this,
            this,
            activity = this@PostActivity,
            onPostViewed = { postId -> postViewModel.onPostViewed(postId) }
        )
        mViewBinding.recyclerView.adapter = postApdapter
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down)
    }


    private fun shareImage(context: Context, imageUrl: String) {
        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {//khi anh da san sang
                    try {
                        val file = File(
                            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            "shared_image.png"
                        )
                        val out =
                            FileOutputStream(file)//Mở một luồng để ghi dữ liệu vào tệp.
                        resource.compress(Bitmap.CompressFormat.PNG, 100, out)//nen
                        out.flush()//Đẩy dữ liệu còn lại vào file.
                        out.close()//Đóng luồng đầu ra.
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        shareUri(context, uri, "image/*")
                    } catch (e: IOException) {
                        Log.e("ShareImage", "không thể lưu ảnh", e)
                    } finally {
                        runOnUiThread {
                            checkHideFAB()
                        }
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    //tải file
    private fun shareAudio(context: Context, audioUrl: String) {
        Thread {//tạo luồng tải dưới nền
            try {
                val url = URL(audioUrl)
                val connect = url.openConnection() as HttpURLConnection//mo ket noi http
                connect.requestMethod = "GET"
                connect.connect()

                if (connect.responseCode == HttpURLConnection.HTTP_OK) {
                    val file = File(
                        context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                        "shared_audio.mp3"
                    )
                    val inputStream = connect.inputStream
                    val outputStream = FileOutputStream(file)

                    val buffer =
                        ByteArray(1024)//tao bo nho dem byte de luu tru du lieu vao file am thanh
                    var len: Int //tao bien dee luu du lieu doc dc từ luồng
                    while (inputStream.read(buffer).also { len = it } != -1) {
                        outputStream.write(buffer, 0, len)
                    }//Đọc dữ liệu từ luồng đầu vào vào bộ đệm cho đến khi kết thúc luồng.

                    outputStream.close()//Đóng luồng đầu ra.
                    inputStream.close()//Đóng luồng đầu vào.

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
                runOnUiThread {
                    checkHideFAB()
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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        checkHideFAB()
    }
}

