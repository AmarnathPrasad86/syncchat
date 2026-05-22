package com.amar.syncchat.data.repository

import com.amar.syncchat.data.model.*
import com.amar.syncchat.data.remote.ApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun register(request: RegisterRequest): Response<AuthResponse> = apiService.register(request)
    
    suspend fun login(request: LoginRequest): Response<AuthResponse> = apiService.login(request)
    
    suspend fun getProfile(token: String): Response<User> = apiService.getProfile("Bearer $token")
    
    suspend fun getContacts(token: String): Response<List<User>> = apiService.getContacts("Bearer $token")
    
    suspend fun getMessages(token: String, userId: String): Response<List<Message>> = 
        apiService.getMessages("Bearer $token", userId)
}
