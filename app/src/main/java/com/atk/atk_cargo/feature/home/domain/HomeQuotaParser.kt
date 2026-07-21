package com.atk.atk_cargo.feature.home.domain

import com.atk.atk_cargo.R
import com.atk.atk_cargo.api.MenuItem
import com.atk.atk_cargo.api.QuotaTonnageWarning

fun parseQuotaTonnageData(rawData: String): List<QuotaTonnageWarning> {
    val warnings = mutableListOf<QuotaTonnageWarning>()
    val blocks = rawData.split("━━━━━━━━━━━━━━━━")

    for (block in blocks) {
        if (block.contains("کشتی") && block.contains("کوتاژ")) {
            try {
                val shipName = block.substringAfter("کشتی *").substringBefore("*").trim()
                val cargoOwner = block.substringAfter("👤 ").substringBefore("\n").trim()
                val quotaNumber = block.substringAfter("کوتاژ: ").substringBefore("\n").trim()
                val currentRemaining = block.substringAfter("مانده فعلی: ").substringBefore(" کیلوگرم").trim()
                val voucherCount = block.substringAfter("حواله‌های ورود شده: ").substringBefore(" عدد").trim()
                val remainingAfterExit = block.substringAfter("مانده بعداز خروج: ").substringBefore(" کیلوگرم").trim()

                val isNegative = remainingAfterExit.contains("−") || remainingAfterExit.contains("-")

                warnings.add(
                    QuotaTonnageWarning(
                        shipName = shipName,
                        cargoOwner = cargoOwner,
                        quotaNumber = quotaNumber,
                        currentRemaining = currentRemaining,
                        voucherCount = voucherCount,
                        remainingAfterExit = remainingAfterExit,
                        isNegative = isNegative
                    )
                )
            } catch (_: Exception) {
            }
        }
    }

    return warnings
}

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
