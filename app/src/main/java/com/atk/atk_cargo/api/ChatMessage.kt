package com.atk.atk_cargo.api

import com.google.gson.annotations.SerializedName

/**
 * مدل داده برای پیام‌های چت
 * این کلاس ساختار داده‌ای برای نمایش و مدیریت پیام‌های چت را تعریف می‌کند
 */
data class ChatMessage(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("fullName")
    val fullName: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("timestamp")
    val timestamp: String,
    
    @SerializedName("is_read")
    val isRead: Int = 0,

    @SerializedName("is_deleted")
    val isDeleted: Int = 0,

    @SerializedName("read_by_names")
    val readByNames: String? = null,

    @SerializedName("is_read_by_me")
    val isReadByMe: Int = 0,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)

/**
 * پاسخ API برای دریافت پیام‌ها
 */
data class ChatMessagesResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("messages")
    val messages: List<ChatMessage>,
    
    @SerializedName("count")
    val count: Int = 0
)

/**
 * پاسخ API برای ارسال پیام
 */
data class SendMessageResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("messageId")
    val messageId: Int? = null,
    
    @SerializedName("messageData")
    val messageData: ChatMessage? = null
)

/**
 * درخواست ارسال پیام
 */
data class SendMessageRequest(
    @SerializedName("action")
    val action: String = "sendMessage",
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("message")
    val message: String
)

/**
 * پاسخ API برای تعداد پیام‌های خوانده نشده
 */
data class UnreadCountResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("unreadCount")
    val unreadCount: Int = 0
)
