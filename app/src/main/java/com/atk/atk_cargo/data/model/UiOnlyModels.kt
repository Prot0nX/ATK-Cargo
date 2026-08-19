package com.atk.atk_cargo.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * فقط برای UI (آیکن/رنگ Compose) — بر خلاف بقیه‌ی این پکیج (DTOهای شبکه‌ای)
 * که در core:network زندگی می‌کنند، این کلاس باید در ماژول app بماند چون
 * core:network وابستگی به Compose UI ندارد (DEEP_CODE_AUDIT.md #Phase4.2،
 * استخراج core:network — این فایل عمداً جدا نگه داشته شد).
 */
data class UserTypeInfo(
    val label: String,
    val description: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)
