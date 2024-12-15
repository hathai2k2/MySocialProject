package com.example.mysocialproject.ui.feature.Admin

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mysocialproject.ui.feature.model.Post
import com.example.mysocialproject.ui.feature.model.User
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val adminRepository = com.example.mysocialproject.ui.feature.Admin.AdminRepository()
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts

    init {
        getPosts()  // Lấy danh sách bài đăng khi ViewModel được khởi tạo
    }

    fun getPosts() {
        adminRepository.getAllPosts(
            onSuccess = { posts ->
                _posts.value = posts
            },
            onFailure = { error ->
                Log.d("AdminViewModel", "Error fetching posts: $error")
            }
        )
    }

    fun deletePost(postId: String, context: Context) {
        viewModelScope.launch {
            val success = adminRepository.deletePost(postId)
            if (success) {
                Log.d("PostViewModel", "Post deleted successfully.")
                Toast.makeText(context, "Xoá thành công bài đăng.", Toast.LENGTH_SHORT).show()
                getPosts()
            } else {
                Log.w("PostViewModel", "Failed to delete post.")
                Toast.makeText(context, "Vui lòng thử lại sau.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val _userList = MutableLiveData<List<User>>()
    val userList: LiveData<List<User>> get() = _userList

    fun getUserkhongavtvsten() {
        viewModelScope.launch {
            val users = adminRepository.getUsersWithMissingNameAndAvatar()
            if (users.isNotEmpty()) {
                _userList.postValue(users)
                Log.d("AdminViewModel", "${users}")
            } else {
                Log.d("AdminViewModel", "No users found with missing name and avatar.")
            }
        }
    }

    fun deleteuser(userId: String, context: Context) {
        viewModelScope.launch {
            val success = adminRepository.deleteUserFromFirestore(userId)
            if (success) {
                Log.d("PostViewModel", "Post deleted successfully.")
                Toast.makeText(context, "Xoá thành công tài khoản này.", Toast.LENGTH_SHORT).show()
                getUserkhongavtvsten()
            } else {
                Log.w("PostViewModel", "Failed to delete user.")
                Toast.makeText(context, "Vui lòng thử lại sau.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}