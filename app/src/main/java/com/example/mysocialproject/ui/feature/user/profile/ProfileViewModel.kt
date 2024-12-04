package com.example.mysocialproject.ui.feature.user.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.mysocialproject.networking.AppDataHelper
import com.example.mysocialproject.networking.repository.UserRepositoryImpl
import com.example.mysocialproject.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val appDataHelper: AppDataHelper
) : BaseViewModel<ProfileNavigation>() {

    fun createProfile(userName: String, imageUri: Uri) {
        viewModelScope.launch {
            try {
                val result = appDataHelper.createProfile(imageUri, userName)
                if (result.isSuccess) {
                    getNavigator()?.onSuccess()
                } else {
                    val exception = result.exceptionOrNull()
                    Log.d("TAG", "Error updating avatar/name: $exception")
                }
            } catch (e: Exception) {
                Log.e("TAG", "Error creating profile: $e")
            }
        }
    }


}


interface ProfileNavigation {
    fun onSuccess()
}
