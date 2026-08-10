package com.atk.atk_cargo.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    // سرور (AuthController::login) این فیلد را با کلید session_token (snake_case)
    // می‌فرستد، نه sessionToken؛ بدون این SerializedName مقدار همیشه null بود و
    // توکن نشست هرگز واقعاً ذخیره نمی‌شد (تا امروز هم بی‌اثر بود چون جایی توکن
    // را واقعاً اعتبارسنجی نمی‌کرد).
    @SerializedName("session_token")
    val sessionToken: String? = null,
    @SerializedName("permissions")
    val permissions: Map<String, Boolean>? = null
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

data class UserTypeInfo(
    val label: String,
    val description: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)
