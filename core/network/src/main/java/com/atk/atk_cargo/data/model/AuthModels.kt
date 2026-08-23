package com.atk.atk_cargo.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("userType")
    val userType: String,
    @SerializedName("deviceModel")
    val deviceModel: String,
    @SerializedName("deviceId")
    val deviceId: String,
    @SerializedName("androidVersion")
    val androidVersion: String,
    @SerializedName("appVersion")
    val appVersion: String
)

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("userType")
    val userType: String? = null,
 // سرور این فیلد را با کلید session_token (snake_case) می‌فرستد؛ بدون این SerializedName توکن نشست همیشه null می‌ماند
    @SerializedName("session_token")
    val sessionToken: String? = null,
 // فیلدهای جدید و افزایشی؛ نصب‌های قدیمی اپ این فیلدها را نادیده می‌گیرند چون Gson فیلد ناشناخته را ساکت رد می‌کند
    @SerializedName("access_token_expires_in")
    val accessTokenExpiresIn: Int? = null,
    @SerializedName("refresh_token")
    val refreshToken: String? = null,
    @SerializedName("refresh_token_expires_in")
    val refreshTokenExpiresIn: Int? = null,
    @SerializedName("permissions")
    val permissions: Map<String, Boolean>? = null
)

// پاسخ POST /api/v2/index.php?route=auth/refresh
data class RefreshTokenResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("session_token")
    val sessionToken: String? = null,
    @SerializedName("access_token_expires_in")
    val accessTokenExpiresIn: Int? = null,
    @SerializedName("refresh_token")
    val refreshToken: String? = null,
    @SerializedName("refresh_token_expires_in")
    val refreshTokenExpiresIn: Int? = null,
    @SerializedName("userType")
    val userType: String? = null,
    @SerializedName("message")
    val message: String? = null
)

// فقط برای خواندن فیلد code از بدنه‌ی خطای ۴۰۱، برای تشخیص access_token_expired از سایر خطاها
data class AuthErrorBody(
    @SerializedName("code")
    val code: String? = null
)

data class LogoutRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("deviceId")
    val deviceId: String = "",
    @SerializedName("sessionToken")
    val sessionToken: String? = null
)

data class LogoutResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)

data class ForceLogoutRequest(
    @SerializedName("action")
    val action: String = "forceLogout",
    @SerializedName("username")
    val username: String,
    @SerializedName("device_id")
    val deviceId: String
)

data class ForceLogoutResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)

data class SessionCheckRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("deviceId")
    val deviceId: String = "",
    @SerializedName("sessionToken")
    val sessionToken: String? = null
)

data class SessionResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("userType")
    val userType: String?
)

data class PermissionSyncRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("deviceId")
    val deviceId: String = "",
    @SerializedName("session_token")
    val session_token: String? = null
)

data class PermissionSyncResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("permissions")
    val permissions: Map<String, Boolean>? = null
)

data class PasswordCheckResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)

data class User(
    val id: Int,
    val username: String,
    val fullName: String? = null,
    val password: String? = null,
    val userType: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
 // ===== فیلدهای وضعیت آنلاین — از endpoint getAllUsersWithStatus =====
    @com.google.gson.annotations.SerializedName("is_online")
    val isOnline: Boolean = false,
    @com.google.gson.annotations.SerializedName("last_activity")
    val lastActivity: String? = null,
    @com.google.gson.annotations.SerializedName("idle_minutes")
    val idleMinutes: Int? = null,
    @com.google.gson.annotations.SerializedName("login_time")
    val loginTime: String? = null,
    @com.google.gson.annotations.SerializedName("device_model")
    val deviceModel: String? = null
)

data class ActiveSessionResponse(
    val success: Boolean,
    val message: String,
    @com.google.gson.annotations.SerializedName("device_id")
    val deviceId: String? = null,
    @com.google.gson.annotations.SerializedName("last_activity")
    val lastActivity: String? = null,
    @com.google.gson.annotations.SerializedName("login_time")
    val loginTime: String? = null
)

data class UpdateUserRequest(
    val action: String = "updateUser",
    val id: Int,
    val username: String? = null,
    val fullName: String? = null,
    val password: String? = null,
    val currentPassword: String? = null,
    val userType: String? = null
)

data class DeleteUserRequest(
    val action: String = "deleteUser",
    val userId: Int
)

data class CreateUserRequest(
    val action: String = "createUser",
    val username: String,
    val fullName: String,
    val password: String,
    val userType: String
)
