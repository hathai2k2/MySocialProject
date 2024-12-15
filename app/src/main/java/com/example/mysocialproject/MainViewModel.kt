package com.example.mysocialproject

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.example.mysocialproject.networking.AppDataHelper
import com.example.mysocialproject.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appDataHelper: AppDataHelper,
) : BaseViewModel<MainNavigation>() {

    private val _imgUriInitialized = MutableLiveData<Boolean>(false)
    val imgUriInitialized: LiveData<Boolean> = _imgUriInitialized

    private val _imgUri = MutableLiveData<Uri?>()
    val imgUri: LiveData<Uri?> = _imgUri
    fun logUserData() {
        viewModelScope.launch {
            appDataHelper.getUserId()?.let { userId ->
//                val user = appDataHelper.LogData(userId)
//                Log.d("TAG", "show: $user")
            }
        }
    }

    fun isUserLoggedOut(): Boolean {
        return !appDataHelper.isUserLoggedIn()
    }

    // Handle image result and resize it
    fun handleImageResult(context: Context, imgUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val resizedImageUri = resizeImage(context, imgUri)

            // Post the resized image URI
            _imgUri.postValue(resizedImageUri)
            _imgUriInitialized.postValue(true)
        }
    }

    // Resize image to 300x300
    private suspend fun resizeImage(context: Context, imgUri: Uri): Uri? {
        val resizedBitmap = withContext(Dispatchers.IO) {
            Glide.with(context)
                .asBitmap()
                .load(imgUri)
                .centerCrop()
                .circleCrop()
                .placeholder(R.drawable.bg_circle_image_gray)
                .submit()
                .get()
        }

        return saveBitmapToStorage(resizedBitmap, context)
    }

    // Save resized bitmap to storage and return its URI
    private fun saveBitmapToStorage(bitmap: Bitmap, context: Context): Uri? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return try {
            val imageFile = File.createTempFile(fileName, ".jpg", storageDir)
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: Exception) {
            Log.e("SaveBitmap", "Error saving bitmap: ${e.message}")
            null
        }
    }

    fun handleDynamicLink(intent: Intent) {
        appDataHelper.handleDynamicLink(intent) { result ->
            Log.d("LinkVmodel", "handleDynamicLink callback result: $result")
            result?.let {
                if (isLoggedIn()) {
                    getNavigator()?.setCurrentId(it)
                } else {
                    setCurrentId(it)
                }
            }
        }
    }

    fun setCurrentId(currentId: String) {
        viewModelScope.launch {
            appDataHelper.setUserId(currentId)
        }
    }

    fun isLoggedIn(): Boolean {
        return appDataHelper.isUserLoggedIn()
    }

    fun clearPref() {
        viewModelScope.launch {
            appDataHelper.clearApp()
        }
    }


}

interface MainNavigation {
    fun setCurrentId(uid: String)
}