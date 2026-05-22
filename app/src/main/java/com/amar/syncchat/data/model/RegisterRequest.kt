package com.amar.syncchat.data.model

data class RegisterRequest(
    val name: String,
    val email: String,
    val mobile: String,
    val password: String
)
