package com.example.mysocialproject.ui.feature.viewmodel

import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.databinding.Observable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mysocialproject.ui.feature.model.Friendship
import com.example.mysocialproject.ui.feature.model.User
import com.example.mysocialproject.ui.feature.repository.FriendRepository
import com.example.mysocialproject.ui.feature.repository.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.O)
class FriendViewmodel : ViewModel(), Observable {
    private val friendRepository = FriendRepository()


    private val messRepository = MessageRepository()
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


    private val _listFriendrequest = MutableLiveData<MutableList<Friendship>?>()
    val listFriendrequest: LiveData<MutableList<Friendship>?> = _listFriendrequest


    private val _listFriend = MutableLiveData<MutableList<User>?>()
    val listFriend: LiveData<MutableList<User>?> = _listFriend

    fun createDynamicLink() {
        viewModelScope.launch {
            try {
                val shortDynamicLink = friendRepository.createdynamicLink()
                _dynamicLink.postValue(shortDynamicLink.toString())
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            }
        }
    }

    fun handleDynamicLink(intent: Intent) {
        friendRepository.handleDynamicLink(intent) { result ->
            Log.d("LinkVmodel", "handleDynamicLink callback result: $result")
            _friendshipResult.value = result
            Log.d("LinkVmodel", "_friendshipResult.value after post: ${_friendshipResult.value}")
        }
    }


    fun resetDynamicLink() {
        _dynamicLink.value = ""
    }

    fun getinforUserSendlink(userId: String) {
        viewModelScope.launch {
            try {
                friendRepository.getAvatarUsersendlink(userId).let { result ->
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
                val result = friendRepository.handleFriendRequest(senderUid)
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
        friendRepository.getFriendships { result ->
            result.onSuccess {
                _listFriendrequest.postValue(it)
                Log.d("friendrequestVmdol", "${_listFriendrequest.postValue(it)}")
            }.onFailure {

            }
        }
    }

    fun onAcceptClick(friendship: Friendship) {

        viewModelScope.launch {
            try {
                val result =
                    friendship.id?.let { friendRepository.updatestateFriendship("Accepted", it) }
                if (result != null) {
                    if (result.isSuccess) {
                        getFriendAccepted()
                        withContext(Dispatchers.Main) {

                            _listFriendrequest.value?.remove(friendship)
                            _listFriendrequest.value = _listFriendrequest.value
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

    fun onDeclineClick(friendship: Friendship) {
        viewModelScope.launch {
            try {
                val result =
                    friendship.id?.let { friendRepository.updatestateFriendship("Declined", it) }
                if (result != null) {
                    if (result.isSuccess) {
                        withContext(Dispatchers.Main) {
                            _listFriendrequest.value?.remove(friendship)
                            _listFriendrequest.value = _listFriendrequest.value
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
        friendRepository.getFriendAccepted(
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
                val result = friendRepository.removeFriend(friendId, position)
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
                val friendId = friendRepository.getFriendId(position)
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

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }

}