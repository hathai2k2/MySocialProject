package com.example.mysocialproject.ui.feature.friend

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mysocialproject.model.FriendshipData
import com.example.mysocialproject.model.UserData
import com.example.mysocialproject.networking.AppDataHelper
import com.example.mysocialproject.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val appDataHelper: AppDataHelper
) : BaseViewModel<Any>() {

    private val _dynamicLink = MutableLiveData<String?>()
    val dynamicLink: LiveData<String?> = _dynamicLink

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _friendshipResult = MutableLiveData<String?>()
    val friendshipResult: LiveData<String?> = _friendshipResult

    private val _infoUserSendlink = MutableLiveData<Pair<String?, String?>>()
    val infoUserSendlink: LiveData<Pair<String?, String?>> = _infoUserSendlink

    private val _friendRequest = MutableLiveData<Boolean?>()
    val friendRequest: LiveData<Boolean?> = _friendRequest

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading


    private val _listFriendData = MutableLiveData<MutableList<FriendshipData>?>()
    val listFriendData: LiveData<MutableList<FriendshipData>?> = _listFriendData


    private val _listFriend = MutableLiveData<MutableList<UserData>?>()
    val listFriend: LiveData<MutableList<UserData>?> = _listFriend

    fun createDynamicLink() {
        viewModelScope.launch {
            try {
                val shortDynamicLink = appDataHelper.createDynamicLink()
                _dynamicLink.postValue(shortDynamicLink.toString())
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            }
        }
    }
    private val _currentId = MutableLiveData<String>()
    val currentId: LiveData<String> = _currentId

    suspend fun getCurrentId() {
        _currentId.value = appDataHelper.getUserId()
    }

    fun resetDynamicLink() {
        _dynamicLink.value = ""
    }

    fun getinforUserSendlink(userId: String) {

        viewModelScope.launch {
            try {
                appDataHelper.getAvatarUserSendLink(userId).let { result ->
                    _infoUserSendlink.value = result
                }
                Log.d("infoavtandname", "${_infoUserSendlink.value}")
            } catch (e: Exception) {
                _errorMessage.postValue("Vui lòng thử lại lần sau")
            }
        }
    }

    fun handleFriendRequest(senderUid: String) {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = appDataHelper.handleFriendRequest(senderUid)
                if (result.isSuccess) {
                    withContext(Dispatchers.Main) {
                        _loading.value = true
                        _friendRequest.value = true
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    withContext(Dispatchers.Main) {
                        _friendRequest.value = false
                        _loading.value = false
                        _errorMessage.value = exception?.message
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _loading.value = false
                    _friendRequest.value = false
                    _errorMessage.value = e.message
                    Log.d("requestfriend", "$e")
                }
            }
        }
    }

    fun getFriendship() {
        appDataHelper.getFriendships { result ->
            result.onSuccess {
                _listFriendData.postValue(it)
                Log.d("friendrequestVmdol", "${_listFriendData.postValue(it)}")
            }.onFailure {

            }
        }
    }

    fun onAcceptClick(friendship: FriendshipData) {

        viewModelScope.launch {
            try {
                val result =
                    friendship.id?.let { appDataHelper.updateStateFriendship("Accepted", it) }
                if (result != null) {
                    if (result.isSuccess) {
                        getFriendAccepted()
                        withContext(Dispatchers.Main) {

                            _listFriendData.value?.remove(friendship)
                            _listFriendData.value = _listFriendData.value
                        }
                    } else {
                        val exception = result.exceptionOrNull()
                        Log.d("friendrequestVmdol", "$exception")
                    }
                }
            } catch (e: Exception) {
                Log.d("friendrequestVmdol", "$e")
            }
        }
    }

    fun onDeclineClick(friendship: FriendshipData) {
        viewModelScope.launch {
            try {
                val result =
                    friendship.id?.let { appDataHelper.updateStateFriendship("Declined", it) }
                if (result != null) {
                    if (result.isSuccess) {
                        withContext(Dispatchers.Main) {
                            _listFriendData.value?.remove(friendship)
                            _listFriendData.value = _listFriendData.value
                            _listFriend.value = _listFriend.value
                        }
                    } else {
                        val exception = result.exceptionOrNull()
                        Log.d("friendrequestVmdol", "$exception")
                    }
                }
            } catch (e: Exception) {
                Log.d("friendrequestVmdol", "$e")
            }
        }
    }

    fun getFriendAccepted() {
        appDataHelper.getFriendAccepted(
            onSuccess = { friends ->
                // Xử lý danh sách bạn bè thành công
                _listFriend.value = friends
            },
            onFailure = { exception ->
                // Xử lý lỗi
                Log.e("ViewModel", "Lỗi khi lấy danh sách bạn bè", exception)
            }
        )

    }


    fun onRemove(friendId: String, position: Int) {
        viewModelScope.launch {
            try {
                val result = appDataHelper.removeFriend(friendId, position)
                Log.d("friendrequestVmdol", "${result}")
                if (result.isSuccess) {
                    withContext(Dispatchers.Main) {
                        _listFriend.value?.removeAt(position)
                        _listFriend.value = _listFriend.value
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    Log.d("friendrequestVmdol", "$exception")
                }
            } catch (e: Exception) {
                Log.d("friendrequestVmdol", "$e")
            }
        }
    }

    private val _friendId = MutableLiveData<Triple<String?, String?, String?>?>()
    val friendId: LiveData<Triple<String?, String?, String?>?> = _friendId
    fun ongetid(position: Int) {
        viewModelScope.launch {
            try {
                val friendId = appDataHelper.getFriendId(position)
                if (friendId != null) {
                    Log.d("friendrequestVmdol", "Friend ID: $friendId")
                    withContext(Dispatchers.Main) {
                        _friendId.value = friendId
                        Log.d(
                            "friendrequestVmdol",
                            "Không tìm thấy friendId tại vị trí ${_friendId.value}"
                        )
                    }
                } else {
                    Log.d("friendrequestVmdol", "Không tìm thấy friendId tại vị trí $position")
                }
            } catch (e: Exception) {
                Log.d("friendrequestVmdol", "Lỗi: ${e.message}")
            }
        }
    }

    init {
        getsize()
    }

    fun getsize() {
        _listFriend.value = _listFriend.value
    }


}