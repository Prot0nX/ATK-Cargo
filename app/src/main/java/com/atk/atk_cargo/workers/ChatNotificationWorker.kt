package com.atk.atk_cargo.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.atk.atk_cargo.api.AppNotificationManager
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.UserPreferencesManager
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ChatNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {
    private val userPreferencesManager: UserPreferencesManager by inject()

    override suspend fun doWork(): ListenableWorker.Result {
        return try {
 // Check if user is logged in
            val username = userPreferencesManager.username.first()
            if (username.isEmpty()) {
                return ListenableWorker.Result.success()
            }

            val userType = userPreferencesManager.userType.first()
            val lastNotifiedId = userPreferencesManager.lastNotifiedMessageId.first()
            
 // Fetch latest messages
            val response = RetrofitClient.apiServiceV2.getChatMessages(username = username, limit = 20)
            
            if (response.isSuccessful) {
                val messagesResponse = response.body()
                val messages = messagesResponse?.messages ?: emptyList()
                
 // Filter new messages (ID > lastNotifiedId)
                val newMessages = messages.filter { it.id > lastNotifiedId }
                    .sortedBy { it.id }

                if (newMessages.isNotEmpty()) {
                    val messagesToShow = mutableListOf<String>()
                    var maxId = lastNotifiedId
                    val uniqueSenders = mutableSetOf<String>()

                    for (message in newMessages) {
                        if (message.id > maxId) {
                            maxId = message.id
                        }

 // Don't notify for own messages
                        if (message.username == username) continue

 // Notification Logic
                        if (userType == "admin") {
 // Admin gets notified for all new messages
                            val senderName = message.fullName ?: message.username
                            messagesToShow.add("$senderName: ${message.message}")
                            uniqueSenders.add(senderName)
                        } else if (message.message.contains("@$username")) {
 // Regular user gets notified if mentioned
                            val senderName = message.fullName ?: message.username
                            messagesToShow.add("$senderName: ${message.message}")
                            uniqueSenders.add(senderName)
                        }
                    }

                    if (messagesToShow.isNotEmpty()) {
 // در مدل جدید، لیست جفت‌های (فرستنده، متن) را ارسال می‌کنیم
                        val messagesPairs = newMessages
                            .filter { it.username != username }
                            .map { (it.fullName ?: it.username) to it.message }
                        
                        showGroupedNotification(messagesPairs, uniqueSenders)
                    }

 // Update last notified ID
                    userPreferencesManager.saveLastNotifiedMessageId(maxId)
                }
            }
            
            ListenableWorker.Result.success()
        } catch (e: Exception) {
            Log.e("ChatWorker", "Error in chat notification worker", e)
            ListenableWorker.Result.retry()
        }
    }

    private fun showGroupedNotification(messages: List<Pair<String, String>>, senders: Set<String>) {
 // بررسی صریح مجوز POST_NOTIFICATIONS (مطابق الگوی StartupViewModel)، وگرنه worker برای کاربر بدون مجوز بی‌نهایت retry می‌کرد
        val hasNotificationPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        if (!hasNotificationPermission) {
            return
        }

        val appNotificationManager = AppNotificationManager(applicationContext)

 // نمایش پیام‌ها با استایل پیام‌رسان
        messages.forEach { (sender, text) ->
            appNotificationManager.showChatNotification(sender, text)
        }
    }
}

