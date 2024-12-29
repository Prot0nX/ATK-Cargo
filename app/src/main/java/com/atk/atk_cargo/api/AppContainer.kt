package com.atk.atk_cargo.api

import android.content.Context

class AppContainer(private val context: Context) {
    private val apiService: ApiService by lazy {
        RetrofitClient.apiService
    }

    val reportsRepository: ReportsRepository by lazy {
        ReportsRepository(apiService)
    }
}