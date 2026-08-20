package com.atk.atk_cargo.core.startup

import androidx.compose.runtime.staticCompositionLocalOf
import com.atk.atk_cargo.domain.session.StartupController

/**
 * دسترسی به همان نمونه‌ی StartupViewModel که MainActivity ساخته، بدون نیاز به
 * `LocalContext.current as MainActivity` (Q-3) در composable های تودرتو مثل
 * MainScreen/HomeScreen/ProfileMenu که با @Preview و تست UI ناسازگار بود.
 *
 * نوع اینترفیس StartupController (نه کلاس concrete StartupViewModel) — home
 * (تنها مصرف‌کننده) نمی‌تواند به StartupViewModel واقعی که در app باقی
 * می‌ماند وابسته شود (Phase4 #29).
 */
val LocalStartupViewModel = staticCompositionLocalOf<StartupController> {
    error("LocalStartupViewModel فراهم نشده — باید در MainActivity.setContent تنظیم شود")
}

/**
 * درخواست مجوز POST_NOTIFICATIONS باید از طریق ActivityResultLauncher ثبت‌شده در
 * MainActivity انجام شود (باید در زمان ساخت Activity ثبت شود)؛ این CompositionLocal
 * دسترسی به آن را بدون کست به MainActivity در سطوح پایین‌تر ممکن می‌کند.
 */
val LocalNotificationPermissionRequester = staticCompositionLocalOf<() -> Unit> { {} }
