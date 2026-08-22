package com.atk.atk_cargo.feature.monitoring.di

import com.atk.atk_cargo.feature.monitoring.data.MonitoringRepository
import com.atk.atk_cargo.feature.monitoring.data.MonitoringRepositoryImpl
import com.atk.atk_cargo.feature.monitoring.presentation.MonitoringViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// وابستگی‌های فیچر مانیتورینگ (DEEP_CODE_AUDIT.md فاز۳ #۳۲ فاز ج)
val monitoringModule = module {
    single<MonitoringRepository> { MonitoringRepositoryImpl(get()) }
    viewModel { MonitoringViewModel(get()) }
}
