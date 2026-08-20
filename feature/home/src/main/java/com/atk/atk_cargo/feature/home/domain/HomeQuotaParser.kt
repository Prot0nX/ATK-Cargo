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
    if (userPermissions["initial_info"] == true) {
        items.add(MenuItem("تعریف کشتی", R.drawable.ic_journal, "initial_info", "مدیریت", "ثبت اطلاعات اولیه"))
    }
    if (userPermissions["manage_users"] == true) {
        items.add(MenuItem("مدیریت کاربران", R.drawable.profile_admin, "manage_users", "مدیریت", "سطح دسترسی"))
    }
    
    return items
}
