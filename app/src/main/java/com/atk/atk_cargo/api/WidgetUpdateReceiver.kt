package com.atk.atk_cargo.api

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import com.atk.atk_cargo.R

class WidgetUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(intent.component)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.real_time_loading_widget)

            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
        }
    }

    companion object {
        private const val EXTRA_TIMER = "extra_timer"

        fun scheduleUpdate(context: Context, appWidgetId: Int, remainingTime: Int) {
            val intent = Intent(context, WidgetUpdateReceiver::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(EXTRA_TIMER, remainingTime)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                pendingIntent
            )
        }
    }
}