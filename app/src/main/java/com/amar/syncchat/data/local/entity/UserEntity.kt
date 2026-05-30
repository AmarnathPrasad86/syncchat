package com.amar.syncchat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.amar.syncchat.data.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String?,
    val email: String?,
    val mobile: String?,
    val online: Boolean,
    val profilePic: String?,
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null,
    val unreadCount: Int = 0
)

fun UserEntity.toUser() = User(
    id = id,
    name = name,
    email = email,
    mobile = mobile,
    online = online,
    profilePic = profilePic
)

fun User.toEntity() = UserEntity(
    id = id ?: "",
    name = name,
    email = email,
    mobile = mobile,
    online = online,
    profilePic = profilePic
)
