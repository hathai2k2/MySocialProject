package com.example.mysocialproject.ui.feature.viewmodel

import NotificationRepository
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mysocialproject.model.Message
import com.example.mysocialproject.model.User
import com.example.mysocialproject.repository.MessageRepository
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.O)
class MessageViewModel() : ViewModel(), Observable {

    val messageRepository = MessageRepository()

    private lateinit var generativeModel: GenerativeModel

    @Bindable
    var messagesend = MutableLiveData<String?>()

    private val _sendSuccess = MutableLiveData<Boolean>()
    val sendSuccess: LiveData<Boolean> get() = _sendSuccess
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    fun sendMessage(
        postId: String,
        receiverId: String,
        imgUrl: String,
        voiceUrl: String,
        content: String,
        createAt: String,
        avtUserPost: String
    ) {
        _loading.value = true
        var message = messagesend.value ?: ""
        viewModelScope.launch(Dispatchers.IO) {
            val result = messageRepository.sendMessage(
                message,
                postId,
                receiverId,
                imgUrl,
                voiceUrl,
                content,
                createAt,
                avtUserPost
            )
            result.onSuccess { success ->
                if (success) {
                    withContext(Dispatchers.Main) {
                        _loading.value = true
                        _sendSuccess.value = true
                    }

                }
            }.onFailure { exception ->
                withContext(Dispatchers.Main) {
                    _loading.value = true
                    _sendSuccess.value = true
                }
            }
        }
    }

    fun uploadAndSendMessage(
        contentUri: Uri?,
        receiverId: String,
        content: String,
        isImage: Boolean
    ) {
        var message = messagesend.value ?: ""
        viewModelScope.launch {
            val result = messageRepository.uploadImageAndSendMessage(
                contentUri,
                message,
                receiverId,
                content,
                isImage
            )
            _sendSuccess.value = result.isNotEmpty()
        }
    }

    fun updateMessagesToSeen(senderId: String) {
        viewModelScope.launch {
            messageRepository.updateMessagesToSeen(senderId)
        }
    }

    fun updateMessagesToReadtu(senderId: String) {
        viewModelScope.launch {
            messageRepository.updateMessagesToRead(senderId)
        }
    }


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> get() = _messages

    fun getMessages(userId: String, friendId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            messageRepository.getMessages(userId, friendId).collect {
                _messages.value = it
                _isLoading.value = false
            }
        }
    }

    private val _unreadMessageCount = MutableLiveData<Int>(0)
    val unreadMessageCount: LiveData<Int> get() = _unreadMessageCount

    private val _hasNewMessages = MutableLiveData<Boolean>(false)
    val hasNewMessages: LiveData<Boolean> get() = _hasNewMessages

    private val notificationRepository = NotificationRepository()

    init {
        notificationRepository.listenForLatestMessage { latestMessage, sender ->
            if (latestMessage != null && sender != null) {
                _hasNewMessages.value = true
                incrementUnreadMessageCount()
            }
        }
    }


    fun incrementUnreadMessageCount() {
        _unreadMessageCount.value = (_unreadMessageCount.value ?: 0) + 1
    }


    private val _friendsWithMessages = MutableLiveData<Pair<List<User>, Map<String, Message>>>()
    val friendsWithMessages: LiveData<Pair<List<User>, Map<String, Message>>>
        get() = _friendsWithMessages

    fun fetchFriendsWithLastMessages() {
        messageRepository.getFriendsWithLastMessages(
            onSuccess = { friendsAndMessages ->
                _friendsWithMessages.postValue(friendsAndMessages)
            },
            onError = { exception ->
                Log.d(
                    "MessageViewModel",
                    "Error fetching friends and last messages: ${exception.message}"
                )
            }
        )
    }

    fun sendMessageToGemini() {
        var message = messagesend.value ?: ""
        viewModelScope.launch {
            _isLoading.value = true
            messageRepository.sendMessageToGemini(message,
                onComplete = { result ->
                    _isLoading.value = false
                    _loading.value = false

                    result.onSuccess { message ->
                        _sendSuccess.value = true
                    }.onFailure { exception ->
                        _sendSuccess.value = false
                        Log.d(
                            "MessageViewModel",
                            "Error sending message to Gemini: ${exception.message}"
                        )
                    }
                }
            )
        }
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }
}