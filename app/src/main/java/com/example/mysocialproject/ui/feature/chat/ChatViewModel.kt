package com.example.mysocialproject.ui.feature.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mysocialproject.model.room.UserEntity
import com.example.mysocialproject.networking.FakeDataGenerator
import com.example.mysocialproject.networking.repository.UserRepositoryImpl
import com.example.mysocialproject.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val userRepositoryImpl: UserRepositoryImpl
):BaseViewModel<Any>() {

    private val _listUser = MutableLiveData<List<UserEntity>>(emptyList())
    val listUser:LiveData<List<UserEntity>> = _listUser

    init {
        getALlUser()
    }

    fun addUser(){
        viewModelScope.launch {
            val fakeUser = FakeDataGenerator.generateFakeUser(10)
            userRepositoryImpl.addUser(fakeUser)
        }
    }

    fun getALlUser(){
        viewModelScope.launch {
            _listUser.value = userRepositoryImpl.getAllUser()
        }
    }
}