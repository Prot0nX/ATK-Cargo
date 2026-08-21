package com.atk.atk_cargo.feature.admin.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// مدل UI مدیریت کاربران (آیکن/رنگ Compose)؛ جدا از DTOهای شبکه‌ای core:network نگه داشته شده است
data class UserTypeInfo(
    val label: String,
    val description: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)
