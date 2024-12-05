package com.example.mysocialproject.ui.feature.post

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mysocialproject.model.PostData
import com.example.mysocialproject.model.PostResult
import com.example.mysocialproject.networking.AppDataHelper
import com.example.mysocialproject.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val appDataHelper: AppDataHelper
): BaseViewModel<Any>()
{
    private val _postResultLiveData = MutableLiveData<PostResult>()
    val postResultLiveData: LiveData<PostResult> get() = _postResultLiveData

    fun addPost(contentUri: Uri, content: String, isImage: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = appDataHelper.addPost(contentUri, content, isImage)

            _postResultLiveData.postValue(result)

        }
    }
    val posts: Flow<PagingData<PostData>> = appDataHelper.getPosts().cachedIn(viewModelScope)

    fun getUserId() = appDataHelper.getCurrentId()
    fun onPostViewed(postId: String) {
        viewModelScope.launch {
            val success = appDataHelper.updateViewedBy(postId)
            if (success) {
                Log.e("PostViewModel", "Updated viewed by for post: $postId")

            } else {
                Log.e("PostViewModel", "Failed to update viewed by for post: $postId")

            }
        }
    }

}

