package com.atk.atk_cargo.core.startup

import androidx.compose.runtime.staticCompositionLocalOf
import com.atk.atk_cargo.domain.session.StartupController

// دسترسی به همان نمونه‌ی StartupViewModel در composableهای تودرتو، بدون نیاز به کست به MainActivity؛ نوع اینترفیس StartupController تا home به کلاس concrete وابسته نشود
val LocalStartupViewModel = staticCompositionLocalOf<StartupController> {
    error("LocalStartupViewModel فراهم نشده — باید در MainActivity.setContent تنظیم شود")
}

// دسترسی به ActivityResultLauncher درخواست مجوز POST_NOTIFICATIONS بدون کست به MainActivity در سطوح پایین‌تر
val LocalNotificationPermissionRequester = staticCompositionLocalOf<() -> Unit> { {} }
