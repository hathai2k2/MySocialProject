//package com.example.mysocialproject.ui.feature.user
//
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.graphics.Bitmap
//import android.net.Uri
//import android.os.Environment
//import android.util.Log
//import android.widget.Toast
//import androidx.core.content.FileProvider
//import androidx.databinding.Bindable
//import androidx.databinding.Observable
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.asFlow
//import androidx.lifecycle.viewModelScope
//import com.bumptech.glide.Glide
//import com.example.mysocialproject.model.UserData
//import com.example.mysocialproject.networking.AppDataHelper
//import com.example.mysocialproject.service.notification.NotificationService
//import com.example.mysocialproject.ui.base.BaseViewModel
//import com.google.android.material.imageview.ShapeableImageView
//import com.google.firebase.FirebaseNetworkException
//import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
//import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
//import com.google.firebase.auth.FirebaseAuthUserCollisionException
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.filter
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.io.File
//import java.io.FileOutputStream
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//import java.util.regex.Pattern
//import javax.inject.Inject
//
//@HiltViewModel
//class AuthViewModel @Inject constructor(
//    private val appDataHelper: AppDataHelper,
//    private val context: Context
//) : BaseViewModel<Any>()
//{
//
//
//    var email = MutableLiveData<String?>()
//
//
//    var nameUser = MutableLiveData<String?>()
//
//    private var imgvAvtUser: ShapeableImageView? = null
//
//    private val _emailError = MutableLiveData<String?>()
//    val emailError: LiveData<String?> get() = _emailError
//
//
//    private val _pwErrorLg = MutableLiveData<String?>()
//    val pwErrorLg: LiveData<String?> get() = _pwErrorLg
//
//
//
//    var password = MutableLiveData<String>()
//
//    private val _passwordError = MutableLiveData<String?>()
//    val passwordError: LiveData<String?> get() = _passwordError
//
//    private val _nameError = MutableLiveData<String?>()
//    val nameError: LiveData<String?> get() = _nameError
//
//    //
//    private val _signupResult = MutableLiveData<Boolean>()
//    val signupResult: LiveData<Boolean> get() = _signupResult
//
//    private val _updateResult = MutableLiveData<Boolean>()
//    val updateResult: LiveData<Boolean> get() = _updateResult
//
//    private val _loginResult = MutableLiveData<Boolean>()
//    val loginResult: LiveData<Boolean> get() = _loginResult
//    private val _getUserResult = MutableLiveData<UserData?>()
//    val getUserResult: LiveData<UserData?> get() = _getUserResult
//    private val _logoutResult = MutableLiveData<Boolean>()
//    val logoutResult: LiveData<Boolean> get() = _logoutResult
//
//
//    //helpertxt
//    private val _emailHelperText = MutableLiveData<String?>()
//    val emailHelperText: LiveData<String?> get() = _emailHelperText
//
//    private val _passwordHelperText = MutableLiveData<String?>()
//    val passwordHelperText: LiveData<String?> get() = _passwordHelperText
//
//    //helpertxt
//    private val _emailHelperTextLg = MutableLiveData<String?>()
//    val emailHelperTextLg: LiveData<String?> get() = _emailHelperTextLg
//
//    private val _passwordHelperTextLg = MutableLiveData<String?>()
//    val passwordHelperTextLg: LiveData<String?> get() = _passwordHelperTextLg
//
//
//    private val _namehelper = MutableLiveData<String?>()
//    val namehelper: LiveData<String?> get() = _namehelper
//
//    private val _avatarImageUrl = MutableLiveData<String?>()
//    val avatarImageUrl: MutableLiveData<String?> get() = _avatarImageUrl
//
//    //loading
//    private val _loading = MutableLiveData<Boolean>()
//    val loading: LiveData<Boolean> get() = _loading
//
//    lateinit var imgUri: Uri
//
////    private lateinit var imgURI:Uri
//
//    //validate email
//    private fun isBasicValidEmail(email: String): Boolean {
//        val basicEmailRegex = "^[A-Za-z0-9+_.-]{2,}@[A-Za-z0-9.-]{2,}\\.[A-Za-z]{2,}\$"
//        return Pattern.compile(basicEmailRegex).matcher(email).matches()
//    }
//
//
//    //validate pw
//    private fun isValidPassword(passwordValue: String): Boolean {
//        val pwRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{6,}$"
//        val pattern = Pattern.compile(pwRegex)
//        return pattern.matcher(passwordValue).matches()
//    }
//
//    private fun handleSignUpError(exception: Throwable?): Boolean {
//        var isAlreadyEmail = false
//        if (exception is FirebaseAuthUserCollisionException) {
//            isAlreadyEmail = true
//        }
//        return isAlreadyEmail
//    }
//
//
//    private fun validateEmailAndPassword(
//        emailValue: String,
//        passwordValue: String,
//        exception: Throwable?
//    ): Boolean {
//        var isValid = true
//        if (emailValue.isEmpty()) {
//            isValid = false
//            _emailError.value = "^"
//        } else if (!isBasicValidEmail(emailValue)) {
//            isValid = false
//            _emailError.value = "Email không hợp lệ"
//        } else if (handleSignUpError(exception)) {
//            isValid = false
//            _emailError.value = "Tài khoản email đã tồn tại. Vui lòng thử email khác!"
//        } else {
//            _emailError.value = ""
//        }
//
//        if (passwordValue.isEmpty()) {
//            isValid = false
//            _passwordError.value = "^"
//        } else if (passwordValue.length < 6) {
//            isValid = false
//            _passwordError.value = "Mật khẩu phải lớn hơn 6 ký tự"
//        } else if (!isValidPassword(passwordValue)) {
//            isValid = false
//            _passwordError.value =
//                "Mật khẩu phải chứa ít nhất 1 kí tự in hoa, 1 chữ thường và 1 chữ số"
//        } else {
//            _passwordError.value = null
//        }
//        return isValid
//    }
//
//
//    //update helpertext
//    fun updateEmailHelperText(email: String) {
//        when {
//            email.isEmpty() -> {
//                _emailHelperText.value = "^"
//            }
//
//            email.length in 1..3 -> {
//                _emailHelperText.value = "Chọn email chính chủ của bạn"
//            }
//
//            !isBasicValidEmail(email) -> {
//                _emailHelperText.value = "Email không hợp lệ"
//            }
//
//            else -> {
//                _emailHelperText.value = null
//                _emailError.value = null
//            }
//        }
//    }
//
//    fun updatePasswordHelperText(password: String) {
//        when {
//            password.isEmpty() -> {
//                _passwordHelperText.value = "^"
//            }
//
//            password.length in 1..4 -> {
//                _passwordHelperText.value = "Mật khẩu phải có ít nhất 6 kí tự"
//            }
//
//            !isValidPassword(password) && password.length > 4 -> {
//                _passwordHelperText.value =
//                    "và có ít nhất 1 kí tự in hoa, 1 chữ số, 1 chữ thường"
//            }
//
//            isValidPassword(password) -> {
//                _passwordHelperText.value = null
//            }
//
//            else -> {
//                _passwordHelperText.value = null
//                _passwordError.value = null
//            }
//        }
//    }
//
//    fun signUp() {
//        val emailValue = email.value ?: ""
//        val passwordValue = password.value ?: ""
//
//        Log.d("TAGY", "xoay xoay")
//        _loading.value = true
//        viewModelScope.launch(Dispatchers.IO) {
//            // Sử dụng coroutine để kiểm tra đồng thời
//            try {
//                val signUpResult = appDataHelper.signUp(emailValue, passwordValue)
//                val exception = signUpResult.exceptionOrNull()
//                if (signUpResult.isSuccess) {
//                    withContext(Dispatchers.Main) {
//                        _loading.value = true
//                        Log.d("TAGY", "Sign up successful")
////                        val intent = Intent(context, LoadingActivity::class.java)
////                        context.startActivity(intent)
////                        if (context is Activity) {
////                            context.finish()
////                        }
//                        _signupResult.value = true
//                        _loading.value = false
//                    }
//                } else {
//                    withContext(Dispatchers.Main) {
//                        !validateEmailAndPassword(emailValue, passwordValue, exception)
//                        _loading.value = false
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    _loading.value = false
//                    !validateEmailAndPassword(emailValue, passwordValue, e)
//                }
//            } catch (e: FirebaseNetworkException) {
//                Toast.makeText(
//                    context,
//                    "Vui lòng kiểm tra kết nối mạng của bạn",
//                    Toast.LENGTH_SHORT
//                )
//            }
//        }
//    }
//
//
//    fun chooseAvatar() {
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "image/*"
//        (context as Activity).startActivityForResult(intent, REQUEST_IMAGE_GET)
//    }
//
//    private val _imgUriInitialized = MutableLiveData<Boolean>(false)
//    val imgUriInitialized: LiveData<Boolean> = _imgUriInitialized
//    fun handleImageResult(
//        requestCode: Int,
//        resultCode: Int,
//        data: Intent?,
//        ShapeableImageView: ShapeableImageView
//    ) {
//        if (resultCode == Activity.RESULT_OK) {
//            when (requestCode) {
//                REQUEST_IMAGE_GET -> {
//                    val originalImageUri = data?.data
//                    if (originalImageUri != null) {
//                        viewModelScope.launch(Dispatchers.IO) {
//                            val resizedImageUri = getResizedImageUri(context, originalImageUri)
//
//                            // Gán imgUri bằng Uri của ảnh đã resize
//                            imgUri = resizedImageUri!!
//                            _imgUriInitialized.postValue(true)
//                            // Hiển thị ảnh đã resize lên ShapeableImageView
//                            withContext(Dispatchers.Main) {
//                                if (resizedImageUri != null) {
//                                    Glide.with(context)
//                                        .load(resizedImageUri)
//                                        .into(ShapeableImageView)
//                                }
//                                updateAvt()
//                            }
//                        }
//
//                    }
//                }
//            }
//        }
//    }
//
//    fun getResizedImageUri(context: Context, imgUri: Uri): Uri? {
//        val resizedBitmap = Glide.with(context)
//            .asBitmap()
//            .load(imgUri)
//            .centerCrop()
//            .circleCrop()
//            .override(300, 300)
//            .submit()
//            .get()
//
//        return saveBitmapToStorage(resizedBitmap, context)
//    }
//
//    private fun saveBitmapToStorage(bitmap: Bitmap, context: Context): Uri? {
//        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//        val fileName = "JPEG_${timeStamp}_"
//        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//
//        return try {
//            val imageFile = File.createTempFile(fileName, ".jpg", storageDir)
//            FileOutputStream(imageFile).use { out ->
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
//            }
//            FileProvider.getUriForFile(
//                context,
//                "${context.packageName}.fileprovider",
//                imageFile
//            )
//        } catch (e: Exception) {
//            Log.e("SaveBitmap", "error save bitmap: ${e.message}")
//            null
//        }
//    }
//
//
//    fun updatenamehelper(name: String) {
//        when {
//            name.isEmpty() -> {
//                _namehelper.value = "^"
//            }
//
//            name.length <= 3 -> {
//                _namehelper.value = "Vui lòng điền tên dài hơn"
//            }
//
//            else -> {
//                _namehelper.value = ""
//                _nameError.value = ""
//            }
//        }
//    }
//
//    //
//    fun updateAvtAndNameUser() {
//        var userNameValue = nameUser.value ?: ""
//        _loading.value = true
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val updateResult = appDataHelper.createProfile(imgUri, userNameValue)
//                if (updateResult.isSuccess) {
//                    withContext(Dispatchers.Main) {
//                        _loading.value = true
//                        Log.d("TAGY", "thanh cong")
////                        val i = Intent(context, MainActivity::class.java)
////                        i.flags =
////                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
////                        context.startActivity(i)
////                        if (context is Activity) {
////                            context.finish()
////                        }
//                        _updateResult.value = true
//                        _loading.value = false
//                    }
//                } else {
//                    withContext(Dispatchers.Main) {
//                        val exception = updateResult.exceptionOrNull()
//                        _loading.value = false
//                        _updateResult.value = false
//                        _nameError.value = "Vui lòng chọn ảnh đại diện"
//                        Log.d("TAGY", "error update avtname: $exception $imgUri")
//                    }
//                }
//
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    _loading.value = false
//                    _updateResult.value = false
//                    _nameError.value = "Vui lòng chọn ảnh đại diện"
//                }
//            }
//        }
//    }
//
//
//    private fun handleLoginError(exception: Throwable?) {
//        exception?.let {
//            Log.e("LoginError", "Exception: ${it.message}, Type: ${it::class.java.simpleName}")
//            val errorMessage = when (it) {
//                is FirebaseAuthInvalidCredentialsException -> {
//                    when (it.errorCode) {
//                        "ERROR_INVALID_EMAIL" -> "Địa chỉ email không hợp lệ"
//                        "ERROR_USER_NOT_FOUND" -> "Tài khoản không tồn tại"
//                        else -> "Tài khoản hoặc mật khẩu không đúng"
//                    }
//                }
//
//                is FirebaseNetworkException -> "Lỗi mạng khi đăng nhập"
//                else -> it.message ?: "Tài khoản không tồn tại"
//            }
//            Log.e("LoginError", errorMessage)
//            _pwErrorLg.value = errorMessage
//        }
//    }
//
//    fun validateLogin(emailValue: String) {
//
//        if (emailValue.isEmpty()) {
//            _emailHelperTextLg.value = "^"
//
//        } else {
//            _emailHelperTextLg.value = ""
//        }
//
//    }
//
//    fun validateLoginpw(passwordValue: String) {
//
//        if (passwordValue.isEmpty()) {
//            _passwordHelperTextLg.value = "^"
//        } else {
//            _passwordHelperTextLg.value = ""
//        }
//    }
//
//    fun login() {
//        val emailValue = email.value ?: ""
//        val passwordValue = password.value ?: ""
//
//        _loading.value = true
//        Log.d("TAGY", "xoayxoay")
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val loginResult = appDataHelper.signIn(emailValue, passwordValue)
//                if (loginResult.isSuccess) {
//                    withContext(Dispatchers.Main) {
//                        _loading.value = true
//                        if (emailValue == "hathai0217@gmail.com") {
////                            val i = Intent(context, AdminActivity::class.java)
////                            i.flags =
////                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
////                            context.startActivity(i)
//                        } else {
////
////                            val i = Intent(context, MainActivity::class.java)
////                            i.flags =
////                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
////                            context.startActivity(i)
//                            if (context is Activity) {
//                                context.finish()
//                            }
//                        }
//
//                        Log.d("TAGY", "Login successful")
//                        _loginResult.value = true
//                    }
//                } else {
//                    val exception = loginResult.exceptionOrNull()
//                    withContext(Dispatchers.Main) {
//                        handleLoginError(exception)
//                        _loading.postValue(false)
//                        Log.d("TAGY", "Login error: ${exception?.message}")
//                        _loginResult.postValue(false)
//                    }
//
//                }
//
//
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    handleLoginError(e)
//                    _loading.value = false
//                    Log.e("TAGY", "Login exception: ${e.message}")
//                    _loginResult.value == false
//                }
//            }
//        }
//    }
//
//    init {
//        getInfo()
//    }
//
//    fun getInfo() {
//        viewModelScope.launch(Dispatchers.IO) {
//            val getUserResult = appDataHelper.getInfoUser()
//            if (getUserResult.isSuccess) {
//                _getUserResult.postValue(getUserResult.getOrNull())
//                Log.d("TAGY", "$getUserResult")
//            } else {
//                // Xử lý lỗi
//                Log.e(
//                    "ProfileViewModel",
//                    "Error get current user: ${getUserResult.exceptionOrNull()}"
//                )
//            }
//        }
//    }
//
//    fun getCurrentId(): String {
//        return appDataHelper.getCurrentId()
//    }
//
//    fun islogined(): Boolean {
//        var islogin = appDataHelper.isUserLoggedIn()
//        return islogin
//    }
//
//    fun isadmin(): Boolean {
//        var isAdmin = appDataHelper.isAdmin()
//        return isAdmin
//    }
//
//    @Bindable
//    var emailforgot = MutableLiveData<String?>()
//    private val _btntext = MutableLiveData<String?>()
//    val btntext: LiveData<String?> get() = _btntext
//
//
//    fun forgotPassword() {
//        viewModelScope.launch {
//            var emailfg = emailforgot.value ?: ""
//            val resetEmail = appDataHelper.forgotPassword(emailfg)
//            if (resetEmail.isSuccess) {
//                _btntext.value = "Đã gửi, Vui lòng kiểm tra email"
//            } else {
//                _btntext.value = "Vui lòng thử lại"
//            }
//        }
//    }
//
//    private fun validatepw(newpw: String): Boolean {
//        var isValid = true
//        if (newpw.isEmpty()) {
//            isValid = false
//            _passwordError.value = "^"
//        } else if (newpw.length < 6) {
//            isValid = false
//            _passwordError.value = "Mật khẩu phải lớn hơn 6 ký tự"
//        } else if (!isValidPassword(newpw)) {
//            isValid = false
//            _passwordError.value =
//                "Mật khẩu phải chứa ít nhất 1 kí tự in hoa, 1 chữ thường và 1 chữ số"
//        } else {
//            _passwordError.value = null
//        }
//        return isValid
//    }
//
//    //update name
//    private val _UpdateError = MutableLiveData<String>()
//    val UpdateError: LiveData<String> get() = _UpdateError
//    fun updatepw() {
//        val newpwvalue = password.value ?: ""
//        _loading.value = true
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val result = appDataHelper.updatePassword(newpwvalue)
//                if (result.isSuccess) {
//                    withContext(Dispatchers.Main) {
//                        _loading.value = true
//                        Log.d("TAGY", "update success")
//                        _UpdateError.value = "mật khẩu"
//                        _loading.value = false
//                    }
//                } else {
//                    val exception = result.exceptionOrNull()
//                    withContext(Dispatchers.Main) {
//                        !validatepw(newpwvalue)
//                        Log.d("TAGY", "update false $exception")
//
//                        if (exception is FirebaseAuthRecentLoginRequiredException) {
//                            _UpdateError.value = "Vui lòng đăng nhập lại trước khi đổi mật khẩu"
//                        } else if (exception is FirebaseNetworkException) {
//                            _UpdateError.value = "Vui lòng kiểm tra lại kết nối"
//                        }
//                        _loading.value = false
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    !validatepw(newpwvalue)
//                    if (e is FirebaseAuthRecentLoginRequiredException) {
//                        _UpdateError.value = "Vui lòng đăng nhập lại trước khi đổi mật khẩu"
//                    } else if (e is FirebaseNetworkException) {
//                        _UpdateError.value = "Vui lòng kiểm tra lại kết nối"
//                    }
//                    _updateResult.value = false
//                    Log.d("TAGY", "$e")
//                    _loading.value = false
//                }
//            }
//        }
//    }
//
//    fun updatename() {
//        val newnamevalue = nameUser.value ?: ""
//        _loading.value = true
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val result = appDataHelper.updateName(newnamevalue)
//                if (result.isSuccess) {
//                    withContext(Dispatchers.Main) {
//                        _loading.value = true
//                        Log.d("TAGY", "update success")
//                        _getUserResult.value?.nameUser = newnamevalue
//                        _UpdateError.value = "tên"
//                        _loading.value = false
//                    }
//                    getInfo()
//                } else {
//                    withContext(Dispatchers.Main) {
//                        _UpdateError.value = "Kiểm tra lại kết nối hoặc thử lại sau"
//                        _loading.value = false
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    _UpdateError.value = "Kiểm tra lại kết nối hoặc thử lại sau"
//                    _loading.value = false
//                }
//            }
//        }
//    }
//
//
//    fun updateAvt() {
//        _loading.value = true
//        viewModelScope.launch(Dispatchers.IO) {
//            _imgUriInitialized.asFlow().filter { it }.first()
//            val result = appDataHelper.updateAvatar(imgUri)
//            if (result.isSuccess) {
//                withContext(Dispatchers.Main) {
//                    _getUserResult.value?.let {
//
//                        _loading.value = true
//                        it.avatarUser = it.avatarUser
//
//                    }
//                    _updateResult.value = true
//                    _loading.value = false
//                    Log.d("Mmmmmmmmmmmmmmmmmmmm", "update success")
//                }
//
//            } else {
//                withContext(Dispatchers.Main) {
//                    _loading.value = false
//                    _updateResult.value = false
//                    Log.d("Mmmmmmmmmmmmmmmmmmmm", "update false")
//                }
//            }
//        }
//    }
//
//
//    fun deleteAccount() {
//        viewModelScope.launch {
//            try {
//                withContext(Dispatchers.Main) {
////                    context.stopService(Intent(context, NotificationService::class.java))
//                }
//                val result = appDataHelper.deleteAccount()
//
//
//                withContext(Dispatchers.Main) {
//                    when {
//                        result.isSuccess -> {
//                            Toast.makeText(context, "Xóa tài khoản thành công", Toast.LENGTH_LONG)
//                                .show()
////                            widgetViewModel().cancelListen()
////                            messWidgetViewModel().cancelListen()
////                            context.startActivity(Intent(context, StartAppActivity::class.java))
//                            (context as? Activity)?.finish()
//
//                        }
//
//                        result.exceptionOrNull() is FirebaseAuthRecentLoginRequiredException -> {
//                            Toast.makeText(
//                                context,
//                                "Vui lòng đăng nhập lại trước khi xóa tài khoản",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//
//                        else -> {
//                            Toast.makeText(
//                                context,
//                                "Vui lòng kiểm tra kết nối mạng",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    when (e) {
//                        is FirebaseAuthRecentLoginRequiredException -> Toast.makeText(
//                            context,
//                            "Vui lòng đăng nhập lại trước khi xóa tài khoản",
//                            Toast.LENGTH_LONG
//                        ).show()
//
//                        else -> Toast.makeText(
//                            context,
//                            "Vui lòng kiểm tra kết nối mạng",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//                Log.d("DeleteAccount", "$e")
//            }
//        }
//    }
//
//    fun deletenewAccount() {
//        viewModelScope.launch {
//            try {
//                val result = appDataHelper.deletenewAccount()
//                if (result) {
//                    withContext(Dispatchers.Main) {
////                        context.startActivity(Intent(context, StartAppActivity::class.java))
//                        (context as? Activity)?.finish()
//                        Toast.makeText(context, "Đã Hủy", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(context, "Vui lòng thử lại sau", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(context, "Vui lòng thử lại sau", Toast.LENGTH_SHORT).show()
//                }
//                Log.d("DeleteAccount", "$e")
//            }
//        }
//    }
//
//    fun logout() {
//        viewModelScope.launch {
//
//            withContext(Dispatchers.Main) {
//                context.stopService(Intent(context, NotificationService::class.java))
//            }
//
//            appDataHelper.logout()
//
//            withContext(Dispatchers.Main) {
//                _logoutResult.value = true
////                widgetViewModel().cancelListen()
////                messWidgetViewModel().cancelListen()
////                context.startActivity(Intent(context, StartAppActivity::class.java))
//                (context as? Activity)?.finish()
//
//            }
//        }
//    }
//
//    companion object {
//    const val REQUEST_IMAGE_GET = 1
//}
//
//    override fun onCleared() {
//        super.onCleared()
//        Log.d("TAGY", "ViewModel is clear")
//    }
//
//
//}