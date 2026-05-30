package com.amar.syncchat.data.local

import androidx.room.*
import com.amar.syncchat.data.local.entity.MessageEntity
import com.amar.syncchat.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("SELECT * FROM users ORDER BY online DESC, name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getMessagesForUser(userId: String): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("DELETE FROM messages")
    suspend fun clearMessages()

    @Query("DELETE FROM users")
    suspend fun clearUsers()
}
