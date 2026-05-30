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

    private val _uiState = MutableStateFlow<List<Message>>(emptyList())
    val uiState: StateFlow<List<Message>> = _uiState

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    private var currentChatUserId: String? = null

    init {
        observeSocketEvents()
    }

    private fun observeSocketEvents() {
        viewModelScope.launch {
            // Naye messages ko memory mein add karein (Room bypass)
            socketManager.incomingMessages.collect { json ->
                val message = parseMessage(json)
                // repository.saveMessageLocally(message) // Commented Room usage
                
                // Real-time update memory state if message belongs to current chat
                if (message.senderId == currentChatUserId) {
                    _uiState.update { currentList -> currentList + message }
                    message.id?.let { socketManager.sendSeen(it) }
                }
            }
        }

        viewModelScope.launch {
            // Sent message ka confirmation
            socketManager.messageSent.collect { json ->
                val message = parseMessage(json)
                // repository.saveMessageLocally(message) // Commented Room usage
                
                // Update memory state
                _uiState.update { currentList -> currentList + message }
            }
        }

        viewModelScope.launch {
            // Typing Indicator
            socketManager.typingStatus.collect { json ->
                val from = json.optString("from")
                if (from == currentChatUserId) {
                    _isTyping.value = json.optBoolean("isTyping")
                }
            }
        }

        viewModelScope.launch {
            // Seen status update
            socketManager.messageSeen.collect { json ->
                val messageId = json.optString("messageId")
                // repository.updateMessageStatus(messageId, "seen") // Commented Room usage
                
                // Memory update for status
                _uiState.update { currentList ->
                    currentList.map { 
                        if (it.id == messageId) it.copy(status = "seen") else it 
                    }
                }
            }
        }
    }

    fun initChat(userId: String) {
        currentChatUserId = userId
        refreshMessages(userId)
    }

    private fun refreshMessages(userId: String) {
        viewModelScope.launch {
            val token = tokenManager.token.firstOrNull() ?: return@launch
            socketManager.connect()
            
            // Fetch from Remote instead of Room
            try {
                val response = repository.getRemoteMessages(token, userId)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = response.body()!!
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun sendMessage(toUserId: String, text: String) {
        if (text.isNotBlank()) {
            socketManager.sendMessage(toUserId, text)
        }
    }

    fun sendTyping(toUserId: String, isTyping: Boolean) {
        socketManager.sendTyping(toUserId, isTyping)
    }

    private fun parseMessage(json: JSONObject): Message {
        return Message(
            id = json.optString("messageId", null),
            senderId = json.optString("sender", null),
            receiverId = json.optString("receiver", null),
            message = json.optString("text", ""),
            timestamp = json.optLong("createdAt", System.currentTimeMillis()),
            isToxic = json.optBoolean("isToxic", false),
            status = json.optString("status", "sent")
        )
    }
}
