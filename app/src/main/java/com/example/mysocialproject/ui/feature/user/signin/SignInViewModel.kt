package com.example.mysocialproject.ui.feature.user.signin

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.mysocialproject.model.UserData
import com.example.mysocialproject.networking.AppDataHelper
import com.example.mysocialproject.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val appDataHelper: AppDataHelper,
): BaseViewModel<SignInNavigation>(){
    fun signIn(email: String, password: String){
        viewModelScope.launch {
            try {
               var result = appDataHelper.signIn(email, password)
                if (result.isSuccess){
                    Log.d("SignInViewModel", "Sign in successful")
                   val userData =  appDataHelper.getInfoUser()

                    userData.getOrNull()?.let { getNavigator()?.onSuccess(it) }
                }else{
                    Log.e("SignInViewModel", "Sign in failed: ${result.exceptionOrNull()?.message}")
                }
            }catch (e:Exception){
                Log.e("SignInViewModel", "Error signing in", e)
            }
        }

    }

}

interface SignInNavigation{
    fun onSuccess(userData: UserData)
}