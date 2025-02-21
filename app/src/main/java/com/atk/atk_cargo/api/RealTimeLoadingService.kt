package com.atk.atk_cargo.api

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RealTimeLoadingService : Service() {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("RealTimeLoadingService", "Service started")
        scope.launch {
            while (isActive) {
                updateWidgetData()
                delay(30 * 1000) // هر 30 ثانیه به‌روزرسانی می‌شود
            }
        }
        return START_STICKY
    }

    private suspend fun updateWidgetData() {
        withContext(Dispatchers.IO) {
            try {
                Log.d("RealTimeLoadingService", "Updating widget data")
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://cargo.atk-nk.site/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val service = retrofit.create(ApiService::class.java)
                val response = service.getRealTimeLoadingData()

                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("RealTimeLoadingService", "Data received: $data")
                    updateWidget()
                } else {
                    Log.e("RealTimeLoadingService", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("RealTimeLoadingService", "Exception: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun updateWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(this, RealTimeLoadingWidget::class.java))

        appWidgetIds.forEach { appWidgetId ->
            RealTimeLoadingWidget.updateAppWidget(this, appWidgetManager, appWidgetId)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}

