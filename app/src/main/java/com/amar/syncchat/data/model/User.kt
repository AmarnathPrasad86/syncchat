package com.amar.syncchat.data.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val mobile: String,
    val online: Boolean = false,
    val profilePic: String = ""
)
