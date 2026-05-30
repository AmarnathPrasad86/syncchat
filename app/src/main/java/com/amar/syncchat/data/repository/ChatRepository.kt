package com.amar.syncchat.data.repository

// import com.amar.syncchat.data.local.ChatDao
// import com.amar.syncchat.data.local.entity.toEntity
// import com.amar.syncchat.data.local.entity.toMessage
// import com.amar.syncchat.data.local.entity.toUser
import com.amar.syncchat.data.model.*
import com.amar.syncchat.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val apiService: ApiService,
    // private val chatDao: ChatDao
) {
    // Auth APIs
    suspend fun register(request: RegisterRequest): Response<AuthResponse> = apiService.register(request)
    suspend fun login(request: LoginRequest): Response<AuthResponse> = apiService.login(request)
    
    // Fixed: Return ProfileResponse to match ApiService and correctly unwrap backend data
    suspend fun getProfile(token: String): Response<ProfileResponse> = apiService.getProfile("Bearer $token")

    // Users & Contacts - Modified for Remote only
    suspend fun getRemoteUsers(token: String): Response<List<User>> {
        return apiService.getContacts("Bearer $token")
    }

    /* Commented out Room implementation
    fun getLocalUsers(): Flow<List<User>> = chatDao.getAllUsers().map { entities -> 
        entities.map { it.toUser() } 
    }

    suspend fun refreshUsers(token: String) {
        val response = apiService.getContacts("Bearer $token")
        if (response.isSuccessful && response.body() != null) {
            val entities = response.body()!!.map { it.toEntity() }
            chatDao.insertUsers(entities)
        }
    }
    */

    // Messages - Modified for Remote only
    suspend fun getRemoteMessages(token: String, userId: String): Response<List<Message>> {
        return apiService.getMessages("Bearer $token", userId)
    }

    /* Commented out Room implementation
    fun getLocalMessages(userId: String): Flow<List<Message>> = 
        chatDao.getMessagesForUser(userId).map { entities -> 
            entities.map { it.toMessage() } 
        }

    suspend fun refreshMessages(token: String, userId: String) {
        val response = apiService.getMessages("Bearer $token", userId)
        if (response.isSuccessful && response.body() != null) {
            val entities = response.body()!!.map { it.toEntity() }
            chatDao.insertMessages(entities)
        }
    }

    suspend fun saveMessageLocally(message: Message) {
        chatDao.insertMessage(message.toEntity())
    }

    suspend fun updateMessageStatus(messageId: String, status: String) {
        chatDao.updateMessageStatus(messageId, status)
    }
    */
}
