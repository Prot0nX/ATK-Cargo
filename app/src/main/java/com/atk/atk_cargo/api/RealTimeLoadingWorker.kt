package com.atk.atk_cargo.api

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RealTimeLoadingWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("RealTimeLoadingWorker", "Worker started")

            val apiService = RetrofitClient.apiService
            val response = apiService.getRealTimeLoadingData()

            if (response.isSuccessful) {
                val data = response.body()
                Log.d("RealTimeLoadingWorker", "Data received: $data")

                // ذخیره داده‌ها در SharedPreferences
                val sharedPreferences = context.getSharedPreferences("RealTimeLoadingWidget", Context.MODE_PRIVATE)
                val gson = Gson()
                val json = gson.toJson(data)
                sharedPreferences.edit().putString("latest_data", json).apply()

                // به‌روزرسانی ویجت
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, RealTimeLoadingWidget::class.java)
                )

                appWidgetIds.forEach { appWidgetId ->
                    RealTimeLoadingWidget.updateAppWidget(context, appWidgetManager, appWidgetId)
                }

                Result.success()
            } else {
                Log.e("RealTimeLoadingWorker", "Error: ${response.errorBody()?.string()}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("RealTimeLoadingWorker", "Exception: ${e.message}")
            Result.failure()
        }
    }
}