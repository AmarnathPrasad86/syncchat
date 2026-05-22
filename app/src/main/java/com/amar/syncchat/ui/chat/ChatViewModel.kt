package com.amar.syncchat.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amar.syncchat.data.local.TokenManager
import com.amar.syncchat.data.model.Message
import com.amar.syncchat.data.remote.SocketManager
import com.amar.syncchat.data.repository.ChatRepository
import com.amar.syncchat.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val tokenManager: TokenManager,
    private val socketManager: SocketManager
) : ViewModel() {

    private val _messagesState = MutableStateFlow<Resource<List<Message>>>(Resource.Loading())
    val messagesState: StateFlow<Resource<List<Message>>> = _messagesState

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    private val messagesList = mutableListOf<Message>()

    init {
        observeSocketEvents()
    }

    private fun observeSocketEvents() {
        viewModelScope.launch {
            // New Incoming Messages
            launch {
                socketManager.incomingMessages.collect { json ->
                    val newMessage = parseMessage(json)
                    messagesList.add(0, newMessage)
                    _messagesState.value = Resource.Success(messagesList.toList())
                    
                    // Jab naya message chat screen par aaye, use "Seen" mark karein
                    socketManager.sendSeen(newMessage.id)
                }
            }

            // Typing Status
            launch {
                socketManager.typingStatus.collect { json ->
                    _isTyping.value = json.optBoolean("isTyping")
                }
            }

            // Delivery & Seen Confirmation
            launch {
                socketManager.messageSeen.collect { json ->
                    val messageId = json.optString("messageId")
                    updateMessageStatus(messageId, "seen")
                }
            }
        }
    }

    private fun parseMessage(json: JSONObject): Message {
        return Message(
            id = json.optString("messageId"),
            senderId = json.optString("sender"),
            receiverId = json.optString("receiver"),
            message = json.optString("text"),
            timestamp = System.currentTimeMillis(),
            isToxic = json.optBoolean("isToxic"),
            status = json.optString("status", "sent")
        )
    }

    private fun updateMessageStatus(messageId: String, newStatus: String) {
        val index = messagesList.indexOfFirst { it.id == messageId }
        if (index != -1) {
            messagesList[index] = messagesList[index].copy(status = newStatus)
            _messagesState.value = Resource.Success(messagesList.toList())
        }
    }

    fun getMessages(userId: String) {
        viewModelScope.launch {
            _messagesState.value = Resource.Loading()
            try {
                val token = tokenManager.token.first()
                if (token != null) {
                    socketManager.connect()
                    val response = repository.getMessages(token, userId)
                    if (response.isSuccessful) {
                        messagesList.clear()
                        messagesList.addAll(response.body() ?: emptyList())
                        _messagesState.value = Resource.Success(messagesList.toList())
                        
                        // Sabhi messages ko seen mark karein chat open hone par
                        messagesList.firstOrNull()?.let { 
                            socketManager.sendSeen(it.id) 
                        }
                    }
                }
            } catch (e: Exception) {
                _messagesState.value = Resource.Error(e.localizedMessage ?: "Error")
            }
        }
    }

    fun sendMessage(toUserId: String, text: String) {
        socketManager.sendMessage(toUserId, text)
    }

    fun sendTyping(toUserId: String, isTyping: Boolean) {
        socketManager.sendTyping(toUserId, isTyping)
    }
}
