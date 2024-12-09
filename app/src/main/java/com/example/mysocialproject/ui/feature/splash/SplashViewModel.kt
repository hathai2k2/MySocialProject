package com.example.mysocialproject.ui.feature.splash

import androidx.lifecycle.viewModelScope
import com.example.mysocialproject.model.UserData
import com.example.mysocialproject.networking.AppDataHelper
import com.example.mysocialproject.networking.repository.UserRepositoryImpl
import com.example.mysocialproject.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val appDataHelper: AppDataHelper
):BaseViewModel<SplashNavigation>() {
    fun checkLogin(){
        viewModelScope.launch {
           val isLogin = appDataHelper.isUserLoggedIn()
            val uid = appDataHelper.getUserId()
            val userData = appDataHelper.getInfoUser()
            val isEmptyFields = appDataHelper.getUserId()
                ?.let { appDataHelper.checkIfUserFieldsEmpty(it) }
            if (isLogin){
                if (isEmptyFields == true){
                    getNavigator()?.openSignupProfile()
                }else{
                    if (uid != null) {
                        userData.getOrNull()?.let { getNavigator()?.openHome(uid, it) }
                    }
                }
            }else{
                getNavigator()?.openLogin()
            }
        }
    }


}

interface SplashNavigation{
    fun openHome(uid:String,userData: UserData)
    fun openLogin()
    fun openSignupProfile()
}