package com.atk.atk_cargo.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: Int,
    val username: String,
    val fullName: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean,
    val isDeleted: Boolean = false,
    val updatedAt: String? = null,
    val isSelf: Boolean,
    val readByNames: String? = null,
    val isReadByMe: Boolean = false
)
