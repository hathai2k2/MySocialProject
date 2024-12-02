package com.example.mysocialproject.ui.feature.user.signup

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.mysocialproject.networking.AppDataHelper
import com.example.mysocialproject.networking.repository.UserRepository
import com.example.mysocialproject.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val appDataHelper: AppDataHelper,
    private val userRepository: UserRepository
):BaseViewModel<SignUpNavigation>() {

    fun signUp(email: String, password: String){
        viewModelScope.launch {
            try {
                userRepository.signUp(email, password)
                getNavigator()?.onSuccess()
            }catch (e:Exception){
                Log.e("SignUpViewModel", "Error signing up", e)
            }


        }
    }
}

interface SignUpNavigation{
    fun onSuccess()
}