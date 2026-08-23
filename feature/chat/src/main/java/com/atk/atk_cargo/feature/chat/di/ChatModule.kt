package com.atk.atk_cargo.feature.chat.di

import com.atk.atk_cargo.data.db.AppDatabase
import com.atk.atk_cargo.feature.chat.data.ChatRepository
import org.koin.dsl.module

// وابستگی‌های فیچر چت؛ قبلاً در appModule متمرکز بود
val chatModule = module {
    single { ChatRepository(get<AppDatabase>().chatDao(), get(), get()) }
}
