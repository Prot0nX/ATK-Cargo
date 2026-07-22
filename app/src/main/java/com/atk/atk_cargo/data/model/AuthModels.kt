package com.atk.atk_cargo.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class LoginRequest(
    val username: String,
    val password: String,
    val userType: String,
    val deviceModel: String,
    val deviceId: String,
    val androidVersion: String,
    val appVersion: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val userType: String? = null,
    val sessionToken: String? = null,
    val permissions: Map<String, Boolean>? = null
)

data class LogoutRequest(
    val username: String,
    val deviceId: String = "",
    val sessionToken: String? = null
)

data class LogoutResponse(
    val success: Boolean,
    val message: String
)

data class ForceLogoutRequest(
    val action: String = "forceLogout",
    val username: String,
    @com.google.gson.annotations.SerializedName("device_id")
    val deviceId: String
)

data class ForceLogoutResponse(
    val success: Boolean,
    val message: String
)

data class SessionCheckRequest(
    val username: String,
    val deviceId: String = "",
    val sessionToken: String? = null
)

data class SessionResponse(
    val success: Boolean,
    val message: String,
    val userType: String?
)

data class PermissionSyncRequest(
    val username: String,
    val deviceId: String = "",
    val session_token: String? = null
)

data class PermissionSyncResponse(
    val success: Boolean,
    val message: String,
    val permissions: Map<String, Boolean>? = null
)

data class PasswordCheckResponse(
    val success: Boolean,
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
