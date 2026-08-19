package com.atk.atk_cargo.feature.admin.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * فقط برای UI مدیریت کاربران (آیکن/رنگ Compose) — قبلاً در
 * data/model/UiOnlyModels.kt (app) بود، عمداً جدا از بقیه‌ی آن پکیج
 * (DTOهای شبکه‌ای در core:network) نگه داشته شده بود چون core:network
 * وابستگی به Compose UI ندارد (DEEP_CODE_AUDIT.md #Phase4.2). با استخراج
 * feature:admin (Phase 5.12) که تنها مصرف‌کننده‌ی این کلاس بود، به همین
 * ماژول منتقل شد.
 */
data class UserTypeInfo(
    val label: String,
    val description: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)
