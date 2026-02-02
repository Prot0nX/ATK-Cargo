package com.atk.atk_cargo.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY id DESC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY id DESC LIMIT :limit")
    fun getRecentMessages(limit: Int): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("UPDATE chat_messages SET isRead = 1 WHERE id = :messageId")
    suspend fun markAsRead(messageId: Int)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAll()

    // حذف پیام‌های قدیمی‌تر از 100 عدد برای مدیریت حافظه کش
    @Query("DELETE FROM chat_messages WHERE id NOT IN (SELECT id FROM chat_messages ORDER BY id DESC LIMIT 100)")
    suspend fun deleteOldMessages()
    
    @Query("UPDATE chat_messages SET message = :newMessage, updatedAt = :updatedAt WHERE id = :messageId")
    suspend fun updateMessage(messageId: Int, newMessage: String, updatedAt: String)

    @Query("UPDATE chat_messages SET isDeleted = 1 WHERE id = :messageId")
    suspend fun markAsDeleted(messageId: Int)
}
