package com.atk.atk_cargo.feature.reports.di

import com.atk.atk_cargo.data.repository.ReportsRepository
import com.atk.atk_cargo.domain.repository.QuotaRepository
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

// وابستگی‌های فیچر گزارش‌ها؛ قبلاً در appModule متمرکز بود (DEEP_CODE_AUDIT.md فاز۳ #۳۳)
val reportsModule = module {
    single { ReportsRepository(get()) } bind QuotaRepository::class
    viewModel { ReportsViewModel(get(), androidApplication()) }
}
