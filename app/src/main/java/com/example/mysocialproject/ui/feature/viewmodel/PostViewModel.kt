package com.example.mysocialproject.ui.feature.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mysocialproject.model.Post
import com.example.mysocialproject.repository.PostRepository
import com.example.mysocialproject.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.O)
class PostViewModel() : ViewModel(), Observable {

    private val postRepository = PostRepository()
    private val authRepository = UserRepository()

    @Bindable
    var content = MutableLiveData<String?>()

    private val _likeEvent = MutableLiveData<String>()
    val likeEvent: LiveData<String> get() = _likeEvent


    private val _postResultLiveData = MutableLiveData<PostRepository.PostResult>()
    val postResultLiveData: LiveData<PostRepository.PostResult> get() = _postResultLiveData


    fun addPost(contentUri: Uri, content: String, isImage: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = postRepository.addPost(contentUri, content, isImage)

            _postResultLiveData.postValue(result)

        }
    }

    fun iscurrentId(): String {
        return postRepository.iscurrentId()
    }

    val posts: Flow<PagingData<Post>> = postRepository.getPosts().cachedIn(viewModelScope)

    fun invalidatePagingSource() {
        postRepository.invalidatePagingSource()
    }

    fun likePost(postId: String, icon: String) {
        _likeEvent.value = postId
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.updateLikePost(postId, icon)
        }
    }


    private val _likesDataMap =
        mutableMapOf<String, MutableLiveData<List<Pair<String, List<String>>>>>()

    fun getLikes(postId: String): LiveData<List<Pair<String, List<String>>>> {
        if (!_likesDataMap.contains(postId)) {
            _likesDataMap[postId] = MutableLiveData()
            loadLikes(postId)
        }
        return _likesDataMap[postId]!!
        Log.d("PostViewModel", "Like $postId: ${_likesDataMap[postId]}")
    }

    private fun loadLikes(postId: String) {
        postRepository.getlikepost(postId) { likeList ->
            _likesDataMap[postId]?.value = likeList
        }
    }

    fun getcurrentId(): String {
        return authRepository.getCurrentUid()
    }

    private val _deletePost = MutableLiveData<Boolean>()
    val deletePost: LiveData<Boolean> = _deletePost
    fun deletePost(postId: String) {
        viewModelScope.launch {
            val success = postRepository.deletePost(postId)
            if (success) {
                _deletePost.postValue(true)
            } else {
                _deletePost.postValue(false)
            }
        }
    }


    @Bindable
    val contentgena = MutableLiveData<String>().apply {
        value = "Nội dung được hiển thị ở đây."
    }

    fun generateContent(imageBitmap: Bitmap) {
        contentgena.value = "Bạn chờ xíu nhé!"
        viewModelScope.launch {
            try {
                contentgena.postValue(postRepository.generateContentFromImage(imageBitmap))
            } catch (e: Exception) {
                contentgena.postValue("Không thể phân tích được nội dung, vui lòng thử lại!.")
                Log.d("PostViewModel", "generateContent: ${e.message}")
            }
        }
    }

    @Bindable
    val contentvoice = MutableLiveData<String>()
    fun generateContentVoice(prompt: String) {
        contentvoice.value = "Bạn chờ xíu nhé!"
        viewModelScope.launch {
            try {
                contentvoice.postValue(postRepository.generateContentFromText(prompt))
            } catch (e: Exception) {
                contentvoice.postValue("Không thể phân tích được nội dung, vui lòng thử lại!.")
                Log.d("PostViewModel", "generateContent: ${e.message}")
            }
        }
    }

    private val _newPostCount = MutableLiveData<Int>(0)
    val newPostCount: LiveData<Int> = _newPostCount

    private val _recognizedText = MutableLiveData<String>()
    val recognizedText: LiveData<String> = _recognizedText

    fun genarateTemp(prompt: String) {
        viewModelScope.launch {
            try {
                _recognizedText.postValue(postRepository.addPunctuation(prompt))
            } catch (e: Exception) {
                _recognizedText.postValue("Không thể phân tích được nội dung, vui lòng thử lại!.")
                Log.d("PostViewModel", "generateContent: ${e.message}")
            }
        }
    }


    fun onPostViewed(postId: String) {
        viewModelScope.launch {
            val success = postRepository.updateViewByUser(postId)
            if (success) {
                Log.d("PostViewModel", "Updated viewed by for post: $postId")

            } else {
                Log.e("PostViewModel", "Failed to update viewed by for post: $postId")

            }
        }
    }

    private val _isListeningForNewPosts = MutableLiveData(true)
    val isListeningForNewPosts: LiveData<Boolean> = _isListeningForNewPosts

    init {
        if (_isListeningForNewPosts.value == true) {
            onNewpost()
        }
    }


    fun onNewpost() {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                postRepository.listenForNewPosts() { newCount ->
                    _newPostCount.postValue(newCount)
                }
            }
        }
    }

    fun stopListeningForNewPosts() {
        _isListeningForNewPosts.value = false
        postRepository.stopListeningForNewPosts()
        _newPostCount.postValue(0)
    }


    fun clearContentgena() {
        contentgena.value = ""
    }

    fun clearContentvoice() {
        contentvoice.value = ""
    }

    fun clearContentemp() {
        _recognizedText.value = ""
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }


}