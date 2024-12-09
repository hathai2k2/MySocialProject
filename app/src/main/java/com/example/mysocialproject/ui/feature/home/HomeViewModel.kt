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
//
//    private val _getUserResult = MutableLiveData<UserData?>()
//    val getUserResult: LiveData<UserData?> get() = _getUserResult

    fun logout() {
        viewModelScope.launch {
            try {
                appDataHelper.logout()
                appDataHelper.clearApp()
                getNavigator()?.onLogOut()
            } catch (e: Exception) {
                Log.d("TAG", "logout: " + e.message)
            }
        }
    }


}


interface HomeNavigation {
    fun onLogOut()
    fun onOpenPost()
}