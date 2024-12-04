package com.example.mysocialproject.ui.feature.home

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mysocialproject.model.UserData
import com.example.mysocialproject.networking.AppDataHelper
import com.example.mysocialproject.ui.base.BaseViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appDataHelper: AppDataHelper
) : BaseViewModel<HomeNavigation>() {

    private val _getUserResult = MutableLiveData<UserData?>()
    val getUserResult: LiveData<UserData?> get() = _getUserResult

    fun logout() {
        viewModelScope.launch {
            try {
                appDataHelper.logout()
                getNavigator()?.onLogOut()
            } catch (e: Exception) {
                Log.d("TAG", "logout: " + e.message)
            }
        }
    }

    fun getInfo() {
        // Khởi chạy tác vụ bất đồng bộ trên IO Dispatcher
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val getUserResult = appDataHelper.getInfoUser()

                if (getUserResult.isSuccess) {
                    // Nếu lấy dữ liệu thành công, cập nhật giá trị lên UI
                    _getUserResult.postValue(getUserResult.getOrNull())
                    Log.d("TAGY", "User data retrieved successfully: ${getUserResult.getOrNull()}")
                } else {
                    // Nếu có lỗi trong quá trình lấy dữ liệu
                    val errorMessage = getUserResult.exceptionOrNull()?.message ?: "Unknown error"
                    Log.e("ProfileViewModel", "Error getting current user: $errorMessage")
                    // Cập nhật UI về trạng thái lỗi nếu cần
                    _getUserResult.postValue(null) // Hoặc có thể là một trạng thái lỗi khác
                }
            } catch (e: Exception) {
                // Bắt tất cả lỗi khác và log lại
                Log.e("ProfileViewModel", "Unexpected error: ${e.message}")
                _getUserResult.postValue(null) // Hoặc trạng thái lỗi cụ thể
            }
        }
    }

    fun updateAvatar(uri: Uri?) {
        viewModelScope.launch {
            try {
                uri?.let { appDataHelper.updateAvatar(it) }
                _getUserResult.value = _getUserResult.value?.copy(
                    avatarUser = uri.toString()
                )
            } catch (e: Exception) {
                Log.d("TAG", "updateAvatar: " + e.message)
            }
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch {
            try {
                appDataHelper.updateName(name)
                _getUserResult.value = _getUserResult.value?.copy(
                    nameUser = name
                )
            } catch (e: Exception) {
                Log.d("TAG", "updateName: " + e.message)
            }
        }
    }

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _UpdateError = MutableLiveData<String>()
    val UpdateError: LiveData<String> get() = _UpdateError
    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> get() = _passwordError

    fun updatePassword(newPassword: String) {

        setLoadingState(true)

        viewModelScope.launch(Dispatchers.IO) {
            val result = try {
                appDataHelper.updatePassword(newPassword)
            } catch (e: Exception) {
                handleError(e, newPassword)
                null
            }

            if (result?.isSuccess == true) {
                handleSuccess()
                _getUserResult.value = _getUserResult.value?.copy(
                    passwordUser = newPassword
                )
            } else {
                // Nếu có lỗi, thông báo lỗi được xử lý trong `handleError`
            }

            setLoadingState(false)
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        _loading.value = isLoading
    }

    private fun handleSuccess() {
        // Xử lý thành công
        Log.d("TAGY", "update success")
        _UpdateError.value = "mật khẩu"
    }

    private fun handleError(exception: Exception, newpwvalue: String) {
        Log.d("TAGY", "update failed $exception")
        validatepw(newpwvalue)

        when (exception) {
            is FirebaseAuthRecentLoginRequiredException -> {
                _UpdateError.value = "Vui lòng đăng nhập lại trước khi đổi mật khẩu"
            }

            is FirebaseNetworkException -> {
                _UpdateError.value = "Vui lòng kiểm tra lại kết nối"
            }

            else -> {
                _UpdateError.value = "Đã xảy ra lỗi, vui lòng thử lại"
            }
        }
    }

    private fun validatepw(newpw: String): Boolean {
        var isValid = true
        if (newpw.isEmpty()) {
            isValid = false
            _passwordError.value = "^"
        } else if (newpw.length < 6) {
            isValid = false
            _passwordError.value = "Mật khẩu phải lớn hơn 6 ký tự"
        } else if (!isValidPassword(newpw)) {
            isValid = false
            _passwordError.value =
                "Mật khẩu phải chứa ít nhất 1 kí tự in hoa, 1 chữ thường và 1 chữ số"
        } else {
            _passwordError.value = null
        }
        return isValid
    }

    private fun isValidPassword(passwordValue: String): Boolean {
        val pwRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{6,}$"
        val pattern = Pattern.compile(pwRegex)
        return pattern.matcher(passwordValue).matches()
    }
}


interface HomeNavigation {
    fun onLogOut()
}