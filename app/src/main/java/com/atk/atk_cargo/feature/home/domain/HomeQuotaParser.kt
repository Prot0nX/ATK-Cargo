package com.atk.atk_cargo.feature.home.domain

import com.atk.atk_cargo.R
import com.atk.atk_cargo.data.model.MenuItem

fun getMenuItemsForUserType(userPermissions: Map<String, Boolean>): List<MenuItem> {
    val items = mutableListOf<MenuItem>()
    
    if (userPermissions["select_info"] == true) {
        items.add(MenuItem("ثبت حواله", R.drawable.ic_boosters, "select_info", "عملیات پایه", "ثبت و مدیریت حواله‌های جدید"))
    }
    if (userPermissions["initial_info"] == true) {
        items.add(MenuItem("تعریف کشتی", R.drawable.ic_journal, "initial_info", "عملیات پایه", "ثبت اطلاعات اولیه کشتی"))
    }
    if (userPermissions["cargo_counter"] == true) {
        items.add(MenuItem("نظارت بارشمار", R.drawable.ic_cargo_counter, "cargo_counter", "نظارت", "کنترل و نظارت بر روند بارگیری"))
    }
    if (userPermissions["manage_users"] == true) {
        items.add(MenuItem("مدیریت کاربران", R.drawable.profile_admin, "manage_users", "مدیریت", "افزودن و مدیریت سطح دسترسی"))
    }
    if (userPermissions["manage_ships"] == true) {
        items.add(MenuItem("مدیریت کشتی ها", R.drawable.ic_reports, "manage_ships", "مدیریت", "لیست کشتی‌ها و وضعیت آن‌ها"))
    }
    
    return items
}
