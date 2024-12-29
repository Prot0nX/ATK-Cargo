package com.atk.atk_cargo.api

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.atk.atk_cargo.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class RealTimeLoadingWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        scheduleWorker(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == CLICK_ACTION) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                // Trigger manual update
                val workRequest = OneTimeWorkRequestBuilder<RealTimeLoadingWorker>().build()
                WorkManager.getInstance(context).enqueue(workRequest)

                // Update the widget immediately to show loading animation
                val appWidgetManager = AppWidgetManager.getInstance(context)
                updateAppWidget(context, appWidgetManager, appWidgetId, isLoading = true)
            }
        }
    }

    companion object {
        private const val CLICK_ACTION = "com.atk.atk_cargo.WIDGET_CLICK"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            isLoading: Boolean = false
        ) {
            Log.d("RealTimeLoadingWidget", "Updating widget $appWidgetId")
            val views = RemoteViews(context.packageName, R.layout.real_time_loading_widget)

            // Set up the intent for the grid view
            val serviceIntent = Intent(context, RealTimeLoadingWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_grid_view, serviceIntent)

            // Set up click intent
            val clickIntent = Intent(context, RealTimeLoadingWidget::class.java).apply {
                action = CLICK_ACTION
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val clickPendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId, clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.refreshIcon, clickPendingIntent)

            // Set up the last update time
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            views.setTextViewText(R.id.lastUpdateTime, "آخرین به‌روزرسانی: $currentTime")

            // Show/hide loading indicator
            views.setViewVisibility(R.id.loading_indicator, if (isLoading) View.VISIBLE else View.GONE)
            views.setViewVisibility(R.id.widget_grid_view, if (isLoading) View.GONE else View.VISIBLE)

            // Set up empty view
            views.setEmptyView(R.id.widget_grid_view, R.id.message)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            if (!isLoading) {
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_grid_view)
            }
        }

        private fun scheduleWorker(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<RealTimeLoadingWorker>(30, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "RealTimeLoadingWorker",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }
}