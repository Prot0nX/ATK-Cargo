package com.atk.atk_cargo.feature.update.di

import com.atk.atk_cargo.api.UpdateManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// وابستگی‌های فیچر به‌روزرسانی درون‌برنامه‌ای؛ قبلاً در appModule متمرکز بود
val updateModule = module {
    single { UpdateManager(androidContext()) }
}
