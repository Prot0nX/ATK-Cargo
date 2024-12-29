package com.atk.atk_cargo.api

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.atk.atk_cargo.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RealTimeLoadingWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return RealTimeLoadingViewFactory(this.applicationContext, intent)
    }
}

class RealTimeLoadingViewFactory(
    private val context: Context,
    private val intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private var loadingData: List<RealTimeLoadingData> = emptyList()
    private val sharedPreferences = context.getSharedPreferences("RealTimeLoadingWidget", Context.MODE_PRIVATE)
    private val gson = Gson()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        loadingData = getLatestData()
    }

    private fun getLatestData(): List<RealTimeLoadingData> {
        val json = sharedPreferences.getString("latest_data", null)
        return if (json != null) {
            val type = object : TypeToken<List<RealTimeLoadingData>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    override fun onDestroy() {}

    override fun getCount(): Int = loadingData.size

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_grid_item)
        val data = loadingData[position]

        views.setTextViewText(R.id.shipName, data.shipName)
        views.setTextViewText(R.id.warehouseName, data.loadingWarehouse)
        views.setTextViewText(R.id.loadingQuotaNumber, "کوتاژ: ${data.loadingQuotaNumber}")
        views.setTextViewText(R.id.totalNetWeight, "وزن خالص: ${data.totalNetWeight} تن")
        views.setTextViewText(R.id.entryExitVouchers, "ورود: ${data.entryVouchers} / خروج: ${data.exitVouchers}")

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}