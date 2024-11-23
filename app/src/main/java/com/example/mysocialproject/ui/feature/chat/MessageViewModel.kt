package com.example.mysocialproject.ui.feature.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mysocialproject.model.room.MessageEntity
import com.example.mysocialproject.networking.FakeDataGenerator
import com.example.mysocialproject.networking.repository.MessageRepository
import com.example.mysocialproject.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val repository: MessageRepository
) : BaseViewModel<Any>() {
    init {
        insertFakeMessages()
    }
    private val _messages = MutableLiveData<List<MessageEntity>>()
    val messages: LiveData<List<MessageEntity>> = _messages

    fun loadMessages(senderId: String, receiverId: String) {
        viewModelScope.launch {
            val data = repository.getMessagesBetween(senderId, receiverId)
            _messages.postValue(data)
        }
    }

    fun insertFakeMessages() {
        viewModelScope.launch {
            val fakeMessages = FakeDataGenerator.generateFakeMessages(50)
            repository.insertMessages(fakeMessages)
        }
    }

    fun sendMessage(senderId: String, receiverId: String, content: String) {
        viewModelScope.launch {
            val newMessage = MessageEntity(
                id = UUID.randomUUID().toString(),
                senderId = senderId,
                receiverId = receiverId,
                content = content,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessages(listOf(newMessage))

            // Cập nhật danh sách tin nhắn
            val updatedMessages = repository.getMessagesBetween(senderId, receiverId)
            _messages.postValue(updatedMessages)
        }
    }
}
