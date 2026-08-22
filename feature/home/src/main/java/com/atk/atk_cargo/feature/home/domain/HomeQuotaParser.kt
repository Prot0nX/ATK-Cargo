package com.atk.atk_cargo.feature.home.domain

import com.atk.atk_cargo.data.model.MenuItem
import com.atk.atk_cargo.feature.home.R

fun getMenuItemsForUserType(userPermissions: Map<String, Boolean>): List<MenuItem> {
    val items = mutableListOf<MenuItem>()
    
    if (userPermissions["select_info"] == true) {
        items.add(MenuItem("ثبت حواله", R.drawable.ic_boosters, "select_info", "عملیات پایه", "حواله‌های جدید"))
    }
    if (userPermissions["cargo_counter"] == true) {
        items.add(MenuItem("نظارت بارشمار", R.drawable.ic_cargo_counter, "cargo_counter", "عملیات پایه", "روند بارگیری"))
    }
    if (userPermissions["manage_ships"] == true) {
        items.add(MenuItem("مدیریت کشتی ها", R.drawable.ic_reports, "manage_ships", "مدیریت", "لیست کشتی‌ها و وضعیت آن‌ها"))
    }
    // بلافاصله بعد از manage_ships اضافه شد تا در همان ردیف کنارش جفت شود (هر دو compact‌اند، طبق تصمیم کاربر)
    // iconResourceId اینجا صرفاً فرمی است؛ آیکون واقعی از روی title در CategorizedMenuGrid (HomeScreen.kt) انتخاب می‌شود
    if (userPermissions["view_monitoring"] == true) {
        items.add(MenuItem("مانیتورینگ", R.drawable.ic_reports, "view_monitoring", "مدیریت", "رویدادهای سلامت و امنیت سیستم"))
    }
    if (userPermissions["initial_info"] == true) {
        items.add(MenuItem("تعریف کشتی", R.drawable.ic_journal, "initial_info", "مدیریت", "ثبت اطلاعات اولیه"))
    }
    if (userPermissions["manage_users"] == true) {
        items.add(MenuItem("مدیریت کاربران", R.drawable.profile_admin, "manage_users", "مدیریت", "سطح دسترسی"))
    }

    return items
}
