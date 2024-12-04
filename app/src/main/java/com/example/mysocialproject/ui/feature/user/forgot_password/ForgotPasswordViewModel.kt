package com.example.mysocialproject.ui.feature.user.forgot_password

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.mysocialproject.networking.AppDataHelper
import com.example.mysocialproject.networking.repository.UserRepositoryImpl
import com.example.mysocialproject.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val appDataHelper: AppDataHelper,
):BaseViewModel<ForgotPasswordNavigation>() {
    fun forgotPassword(email:String) {
        viewModelScope.launch {
            try {
                val result = appDataHelper.forgotPassword(email)
                if (result.isSuccess) {
                    getNavigator()?.onSuccess()
                }
            }catch (e:Exception){
                Log.e("ForgotPasswordViewModel", "Error forgot password", e)
            }
        }
    }
}


interface ForgotPasswordNavigation{
    fun onSuccess()
}