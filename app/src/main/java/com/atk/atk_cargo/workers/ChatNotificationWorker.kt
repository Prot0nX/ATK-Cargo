package com.atk.atk_cargo.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.atk.atk_cargo.MainActivity
import com.atk.atk_cargo.R
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.UserPreferencesManager
import kotlinx.coroutines.flow.first

class ChatNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): ListenableWorker.Result {
        return try {
            val userPreferencesManager = UserPreferencesManager(applicationContext)
            
            // Check if user is logged in
            val username = userPreferencesManager.username.first()
            if (username.isEmpty()) {
                return ListenableWorker.Result.success()
            }

            val userType = userPreferencesManager.userType.first()
            val lastNotifiedId = userPreferencesManager.lastNotifiedMessageId.first()
            
            // Fetch latest messages
            val apiService = RetrofitClient.apiService
            val response = apiService.getChatMessages(username = username, limit = 20)
            
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
                        showGroupedNotification(messagesToShow, uniqueSenders)
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

    private fun showGroupedNotification(messages: List<String>, senders: Set<String>) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "chat_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "پیام‌های چت",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "نوتیفیکیشن برای پیام‌های جدید چت"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "admin_chat")
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (senders.size == 1) {
            "پیام جدید از ${senders.first()}"
        } else {
            "${messages.size} پیام جدید از ${senders.size} گفتگو"
        }

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)
        
        messages.take(5).forEach { inboxStyle.addLine(it) }
        if (messages.size > 5) {
            inboxStyle.setSummaryText("+${messages.size - 5} پیام دیگر")
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentText(messages.lastOrNull() ?: "پیام جدید")
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup("CHAT_GROUP") // For potential future expansion
            .setGroupSummary(true)
            .build()

        // Use a fixed ID for summary/grouped notification to update it instead of spamming
        notificationManager.notify(1001, notification)
    }
}

