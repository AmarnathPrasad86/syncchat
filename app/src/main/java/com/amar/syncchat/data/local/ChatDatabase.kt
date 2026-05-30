package com.amar.syncchat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.amar.syncchat.data.local.entity.MessageEntity
import com.amar.syncchat.data.local.entity.UserEntity

@Database(entities = [UserEntity::class, MessageEntity::class], version = 1, exportSchema = false)
abstract class ChatDatabase : RoomDatabase() {
    abstract val dao: ChatDao
}
