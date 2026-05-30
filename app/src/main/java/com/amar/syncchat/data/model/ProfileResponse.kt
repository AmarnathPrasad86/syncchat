package com.amar.syncchat.data.model

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("user", alternate = ["data", "profile"])
    val user: User? = null,
    val success: Boolean = false,
    val message: String? = null
)
