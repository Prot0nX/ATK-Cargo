package com.atk.atk_cargo.api

import com.google.gson.annotations.SerializedName

// درخواست ویرایش پیام
data class EditMessageRequest(
    @SerializedName("action")
    val action: String = "editMessage",
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("messageId")
    val messageId: Int,
    
    @SerializedName("message")
    val message: String
)

// درخواست حذف پیام
data class DeleteMessageRequest(
    @SerializedName("action")
    val action: String = "deleteMessage",
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("messageId")
    val messageId: Int
)
