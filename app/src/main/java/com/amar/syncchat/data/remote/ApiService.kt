package com.amar.syncchat.data.remote

import com.amar.syncchat.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<User>

    @GET("api/chat/contacts")
    suspend fun getContacts(@Header("Authorization") token: String): Response<List<User>>

    @GET("api/chat/messages/{userId}")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<List<Message>>
}
