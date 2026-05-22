package com.amar.syncchat.data.remote

import com.amar.syncchat.data.local.TokenManager
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor(
    private val tokenManager: TokenManager
) {
    private var socket: Socket? = null
    
    // For receiving incoming private messages
    private val _incomingMessages = MutableSharedFlow<JSONObject>(extraBufferCapacity = 10)
    val incomingMessages: SharedFlow<JSONObject> = _incomingMessages

    // For confirmation from server when a message is processed (includes AI results)
    private val _messageSent = MutableSharedFlow<JSONObject>(extraBufferCapacity = 10)
    val messageSent: SharedFlow<JSONObject> = _messageSent

    // For notification when recipient gets the message
    private val _messageDelivered = MutableSharedFlow<JSONObject>(extraBufferCapacity = 10)
    val messageDelivered: SharedFlow<JSONObject> = _messageDelivered

    // For notification when recipient opens/sees the message
    private val _messageSeen = MutableSharedFlow<JSONObject>(extraBufferCapacity = 10)
    val messageSeen: SharedFlow<JSONObject> = _messageSeen

    // For user status updates (online/offline)
    private val _userStatus = MutableSharedFlow<JSONObject>(extraBufferCapacity = 10)
    val userStatus: SharedFlow<JSONObject> = _userStatus

    // For typing indicators from others
    private val _typingStatus = MutableSharedFlow<JSONObject>(extraBufferCapacity = 10)
    val typingStatus: SharedFlow<JSONObject> = _typingStatus
    
    // For toxicity warnings from server
    private val _aiWarning = MutableSharedFlow<JSONObject>(extraBufferCapacity = 10)
    val aiWarning: SharedFlow<JSONObject> = _aiWarning

    // For any error messages from the backend
    private val _errorMessage = MutableSharedFlow<JSONObject>(extraBufferCapacity = 10)
    val errorMessage: SharedFlow<JSONObject> = _errorMessage

    suspend fun connect() {
        if (socket?.connected() == true) return

        val token = tokenManager.token.firstOrNull() ?: return
        
        // Match backend 'authenticateSocket' middleware
        val options = IO.Options.builder()
            .setAuth(mapOf("token" to token))
            .build()

        socket = IO.socket("http://10.0.2.2:4000", options)

        socket?.on(Socket.EVENT_CONNECT) {
            println("Socket Connected")
        }

        // Listen for private messages arriving from others
        socket?.on("private_message") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { _incomingMessages.tryEmit(it) }
        }

        // Listen for server confirmation of message processing
        socket?.on("message_sent") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { _messageSent.tryEmit(it) }
        }

        // Listen for delivery confirmation
        socket?.on("message_delivered") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { _messageDelivered.tryEmit(it) }
        }

        // Listen for read receipts (seen status)
        socket?.on("message_seen") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { _messageSeen.tryEmit(it) }
        }

        // Listen for online/offline status changes
        socket?.on("user_status") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { _userStatus.tryEmit(it) }
        }

        // Listen for typing events from the person you are chatting with
        socket?.on("typing") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { _typingStatus.tryEmit(it) }
        }

        // Listen for toxicity warnings
        socket?.on("ai_warning") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { _aiWarning.tryEmit(it) }
        }

        // Listen for general error messages
        socket?.on("error_message") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { _errorMessage.tryEmit(it) }
        }

        socket?.connect()
    }

    // --- EMITTERS (Sending to Backend) ---

    fun sendMessage(to: String, text: String) {
        val data = JSONObject().apply {
            put("to", to)
            put("text", text)
        }
        socket?.emit("private_message", data)
    }

    fun sendTyping(to: String, isTyping: Boolean) {
        val data = JSONObject().apply {
            put("to", to)
            put("isTyping", isTyping)
        }
        socket?.emit("typing", data)
    }

    fun sendSeen(messageId: String) {
        val data = JSONObject().apply {
            put("messageId", messageId)
        }
        socket?.emit("message_seen", data)
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }
}
