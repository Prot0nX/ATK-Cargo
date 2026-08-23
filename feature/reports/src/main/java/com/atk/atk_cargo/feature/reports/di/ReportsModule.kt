package com.atk.atk_cargo.feature.reports.di

import com.atk.atk_cargo.data.repository.ReportsRepository
import com.atk.atk_cargo.domain.repository.QuotaRepository
import com.atk.atk_cargo.feature.reports.viewmodel.ReportsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

// وابستگی‌های فیچر گزارش‌ها؛ قبلاً در appModule متمرکز بود
val reportsModule = module {
    single { ReportsRepository(get()) } bind QuotaRepository::class
    viewModel { ReportsViewModel(get(), androidApplication()) }
}
