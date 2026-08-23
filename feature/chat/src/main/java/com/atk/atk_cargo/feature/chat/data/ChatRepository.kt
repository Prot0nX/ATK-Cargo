package com.atk.atk_cargo.feature.chat.data

import android.util.Log
import com.atk.atk_cargo.api.ApiServiceV2
import com.atk.atk_cargo.api.ApiV2Routes
import com.atk.atk_cargo.api.ChatMessage
import com.atk.atk_cargo.api.DeleteMessageRequest
import com.atk.atk_cargo.api.EditMessageRequest
import com.atk.atk_cargo.api.SendMessageRequest
import com.atk.atk_cargo.data.db.ChatDao
import com.atk.atk_cargo.data.db.ChatMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ChatRepository(
    private val chatDao: ChatDao,
    private val apiServiceV2: ApiServiceV2,
    private val userPreferencesManager: ChatPreferencesStore
) {
    // دریافت پیام‌ها از دیتابیس به صورت جریان داده (Flow)
    val messages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()

    // برای کد HTTP غیر ۲xx، Retrofit پیام سرور را در body() نمی‌گذارد بلکه در errorBody() — بدون این، پیام‌های واقعی سرور (مثلاً «دسترسی غیرمجاز») از نسخه‌ی ۴.۱.۰ به بعد که سرور کد...
    private fun <T> extractErrorMessage(response: retrofit2.Response<T>, fallback: String): String {
        return try {
            response.errorBody()?.string()?.let { raw ->
                org.json.JSONObject(raw).optString("message", fallback)
            } ?: fallback
        } catch (_: Exception) {
            fallback
        }
    }

    // مشاهده تعداد پیام‌های خوانده نشده
    val unreadCount: Flow<Int> = chatDao.getUnreadCount()

    suspend fun refreshMessages() {
        withContext(Dispatchers.IO) {
            try {
                // دریافت نام کاربری
                val username = userPreferencesManager.username.first()
                if (username.isEmpty()) return@withContext

                // دریافت آخرین پیام‌های سرور تا پیام‌های ویرایش/حذف‌شده هم به‌روز شوند
                val response = apiServiceV2.getChatMessages(
                    limit = 100,
                    username = username
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val messages = response.body()?.messages ?: emptyList()
                    Log.d("ATK_CHAT_DEBUG", "Refresh: Received ${messages.size} messages from server")

                    if (messages.isNotEmpty()) {
                        val serverIds = messages.map { it.id }
                        val minIdInBatch = serverIds.minOrNull() ?: 0
                        val maxIdInBatch = serverIds.maxOrNull() ?: 0

                        // همگام‌سازی: حذف پیام‌هایی که در این بازه هستند اما در پاسخ سرور نبودند
                        Log.d("ATK_CHAT_DEBUG", "Refresh: Syncing range [$minIdInBatch, $maxIdInBatch]")
                        chatDao.deleteOrphanedMessages(minIdInBatch, maxIdInBatch, serverIds)

                        val entities = messages.map { it.toEntity(username) }
                        Log.d("ATK_CHAT_DEBUG", "Refresh: Inserting ${entities.size} entities into local DB")
                        chatDao.insertMessages(entities)

                        // پاکسازی پیام‌های خیلی قدیمی برای جلوگیری از انباشت دیتا
                        chatDao.deleteOldMessages()
                    } else {
                        // اگر سرور هیچ پیامی برنگرداند، یعنی چت کلاً خالی شده است
                        Log.d("ATK_CHAT_DEBUG", "Refresh: Server returned empty list. Clearing local cache.")
                        chatDao.clearAll()
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "Error refreshing messages", e)
                throw e
            }
        }
    }

    suspend fun loadOlderMessages(olderThanId: Int) {
        withContext(Dispatchers.IO) {
            try {
                val username = userPreferencesManager.username.first()
                if (username.isEmpty()) return@withContext

                val response = apiServiceV2.getChatMessages(
                    olderThanId = olderThanId,
                    limit = 100,
                    username = username
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val messages = response.body()?.messages ?: emptyList()
                    Log.d("ATK_CHAT_DEBUG", "Load Older: Received ${messages.size} messages for ID < $olderThanId")
                    if (messages.isNotEmpty()) {
                        val serverIds = messages.map { it.id }
                        val minIdInBatch = serverIds.minOrNull() ?: 0
                        val maxIdInBatch = serverIds.maxOrNull() ?: 0

                        // همگام‌سازی برای صفحات قدیمی
                        chatDao.deleteOrphanedMessages(minIdInBatch, maxIdInBatch, serverIds)

                        val entities = messages.map { it.toEntity(username) }
                        chatDao.insertMessages(entities)
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "Error loading older messages", e)
                throw e
            }
        }
    }

    suspend fun sendMessage(message: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val username = userPreferencesManager.username.first()
            Log.d("ATK_CHAT_DEBUG", "Send Message: Requesting - user: $username, msg: $message")
            val request = SendMessageRequest(username = username, message = message)
            val response = apiServiceV2.sendChatMessage(request)

            Log.d("ATK_CHAT_DEBUG", "Send Message: HTTP Code: ${response.code()}, Body success: ${response.body()?.success}")

            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.messageData?.let { newMessage ->
                    Log.d("ATK_CHAT_DEBUG", "Send Message: Success. Inserting new message ID: ${newMessage.id}")
                    chatDao.insertMessage(newMessage.toEntity(username))
                }
                Result.success(Unit)
            } else {
                val errorMsg = response.body()?.message ?: extractErrorMessage(response, "خطا در ارسال پیام")
                Log.e("ATK_CHAT_DEBUG", "Send Message: Failed - $errorMsg (Raw message: ${response.body()?.message})")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("ATK_CHAT_DEBUG", "Send Message: Exception", e)
            Result.failure(e)
        }
    }

    suspend fun editMessage(messageId: Int, newMessage: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val username = userPreferencesManager.username.first()
            val request = EditMessageRequest(username = username, messageId = messageId, message = newMessage)
            val response = apiServiceV2.editChatMessage(
                request = request,
                route = ApiV2Routes.chatMessageEdit(messageId)
            )

            if (response.isSuccessful && response.body()?.success == true) {
                // آپدیت دستی دیتابیس محلی چون پاسخ سرور updated_at را برنمی‌گرداند
                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.ENGLISH).format(java.util.Date())
                chatDao.updateMessage(messageId, newMessage, timestamp)

                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: extractErrorMessage(response, "خطا در ویرایش پیام")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMessage(messageId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val username = userPreferencesManager.username.first()
            val request = DeleteMessageRequest(username = username, messageId = messageId)
            val response = apiServiceV2.deleteChatMessage(
                request = request,
                route = ApiV2Routes.chatMessageDelete(messageId)
            )

            if (response.isSuccessful && response.body()?.success == true) {
                chatDao.markAsDeleted(messageId)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: extractErrorMessage(response, "خطا در حذف پیام")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ChatMessage.toEntity(currentUsername: String): ChatMessageEntity {
        return ChatMessageEntity(
            id = this.id,
            username = this.username,
            fullName = this.fullName,
            message = this.message,
            timestamp = this.timestamp,
            isRead = this.isRead == 1,
            isDeleted = this.isDeleted == 1,
            updatedAt = this.updatedAt,
            isSelf = this.username == currentUsername,
            readByNames = this.readByNames,
            isReadByMe = this.isReadByMe == 1
        )
    }

    suspend fun getAdminUsers(): List<com.atk.atk_cargo.data.model.User> = withContext(Dispatchers.IO) {
        try {
            apiServiceV2.getAdminUsers()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching admin users", e)
            emptyList()
        }
    }

    suspend fun getShipsList(): com.atk.atk_cargo.data.model.ShipsData = withContext(Dispatchers.IO) {
        try {
            val response = apiServiceV2.getShipsList()
            if (response.isSuccessful) {
                response.body()?.data ?: com.atk.atk_cargo.data.model.ShipsData(emptyList(), emptyList())
            } else {
                com.atk.atk_cargo.data.model.ShipsData(emptyList(), emptyList())
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching ships list", e)
            com.atk.atk_cargo.data.model.ShipsData(emptyList(), emptyList())
        }
    }

    suspend fun getShipQuotas(shipName: String): List<com.atk.atk_cargo.data.model.Quota> = withContext(Dispatchers.IO) {
        try {
            val response = apiServiceV2.getShipQuotas(route = ApiV2Routes.shipQuotas(shipName))
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching ship quotas", e)
            emptyList()
        }
    }

    suspend fun markAllMessagesAsRead() {
        withContext(Dispatchers.IO) {
            chatDao.markAllAsReadByMe()
        }
    }
}