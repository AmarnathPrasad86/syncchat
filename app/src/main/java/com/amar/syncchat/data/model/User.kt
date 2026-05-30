package com.amar.syncchat.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id", alternate = ["id"])
    val id: String? = null,
    
    @SerializedName("name", alternate = ["fullName", "username"])
    val name: String? = null,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("mobile", alternate = ["phone", "phoneNumber"])
    val mobile: String? = null,
    
    @SerializedName("online")
    val online: Boolean = false,
    
    @SerializedName("profilePic", alternate = ["avatar", "image"])
    val profilePic: String? = "",
    
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null,
    val unreadCount: Int = 0
)
