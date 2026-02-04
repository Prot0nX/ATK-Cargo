package com.atk.atk_cargo.api

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.edit
import com.atk.atk_cargo.MainActivity
import com.atk.atk_cargo.R

class AppNotificationManager(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    companion object {
        // کانال‌های اعلان
        const val CHANNEL_CHAT = "chat_notifications_v2"
        const val CHANNEL_LOADING = "loading_notifications_v2"
        const val CHANNEL_SYSTEM = "system_alerts_v2"
        const val CHANNEL_SERVICE = "service_status_v2"

        const val SUMMARY_LOADING_ID = 200
        const val SYSTEM_ALERT_ID = 300
    }

    /**
     * تنظیم اولیه‌ی کانال‌های اعلان در سیستم اندروید
     */
    fun setupChannels() {
        val systemManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channels = listOf(
            NotificationChannel(CHANNEL_CHAT, "گفتگوها", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "پیام‌های جدید از کاربران و مدیران"
                enableLights(true)
                lightColor = Color.GREEN
            },
            NotificationChannel(CHANNEL_LOADING, "بارگیری لحظه‌ای", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "آپدیت‌های مربوط به وضعیت بارگیری کشتی‌ها"
            },
            NotificationChannel(CHANNEL_SYSTEM, "هشدارهای سیستمی", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "هشدارهای مربوط به تناژ و اعلان‌های مهم سیستمی"
                enableVibration(true)
            },
            NotificationChannel(CHANNEL_SERVICE, "وضعیت سرویس", NotificationManager.IMPORTANCE_LOW).apply {
                description = "اطلاع از وضعیت پایداری سرویس‌های پس‌زمینه"
            }
        )
        systemManager.createNotificationChannels(channels)
    }

    /**
     * نمایش اعلان چت با استفاده از MessagingStyle (استاندارد پیام‌رسان‌ها)
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showChatNotification(
        senderName: String,
        message: String,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val user = Person.Builder()
            .setName(senderName)
            .build()

        val messagingStyle = NotificationCompat.MessagingStyle(user)
            .addMessage(message, timestamp, user)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "admin_chat")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_CHAT)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setStyle(messagingStyle)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup("CHAT_GROUP_V2")
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        notificationManager.notify(senderName.hashCode(), notification)
    }

    /**
     * مدیریت لیست کشتی‌های مسدود شده
     */
    fun muteShip(shipName: String) {
        val prefs = context.getSharedPreferences("ship_notifications_prefs", Context.MODE_PRIVATE)
        val muted = prefs.getStringSet("muted_ships", emptySet())?.toMutableSet() ?: mutableSetOf()
        muted.add(shipName)
        prefs.edit { putStringSet("muted_ships", muted) }
        notificationManager.cancel(shipName.hashCode())
    }

    fun unmuteShip(shipName: String) {
        val prefs = context.getSharedPreferences("ship_notifications_prefs", Context.MODE_PRIVATE)
        val muted = prefs.getStringSet("muted_ships", emptySet())?.toMutableSet() ?: mutableSetOf()
        muted.remove(shipName)
        prefs.edit { putStringSet("muted_ships", muted) }
    }

    fun clearMutedShips() {
        context.getSharedPreferences("ship_notifications_prefs", Context.MODE_PRIVATE).edit { clear() }
    }

    /**
     * نمایش هشدارهای سیستمی (مانند هشدارهای تناژ)
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showSystemAlert(title: String, content: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.atk.atk_cargo.OPEN_WARNINGS"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_SYSTEM)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        notificationManager.notify(SYSTEM_ALERT_ID, notification)
    }

    /**
     * مدیریت هوشمند و نمایش اعلان‌های بارگیری لحظه‌ای
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun notifyLoadingData(loadingData: List<RealTimeLoadingData>, mutedShips: Set<String>) {
        val shipGroups = loadingData.groupBy { it.shipName }
        
        var totalInput = 0
        var totalExit = 0
        var totalWeight = 0

        shipGroups.forEach { (shipName, dataList) ->
            totalInput += dataList.sumOf { it.entryVouchers }
            totalExit += dataList.sumOf { it.exitVouchers }
            totalWeight += dataList.sumOf { it.totalNetWeight }

            // نمایش اعلان انفرادی اگر کشتی مسدود (Mute) نشده باشد
            if (!mutedShips.contains(shipName)) {
                val warehouses = dataList.map { it.loadingWarehouse }.distinct()
                val title = "کشتی $shipName (${warehouses.joinToString()})"
                val content = "✅ ورود: ${dataList.sumOf { it.entryVouchers }} | ⬅️ خروج: ${dataList.sumOf { it.exitVouchers }} | ⚖️ ${formatWeight(dataList.sumOf { it.totalNetWeight })}"
                
                val expandedText = buildString {
                    append("آمار تفکیکی انبارها:\n")
                    dataList.forEach { data ->
                        append("• ${data.loadingWarehouse}: ${data.entryVouchers + data.exitVouchers} حواله (${formatWeight(data.totalNetWeight)})\n")
                    }
                }
                
                showShipNotification(shipName, title, content, expandedText)
            }
        }

        // نمایش آمار کلی
        val summaryTitle = "آمار کلی بارگیری عملیات"
        val summaryContent = "کل حواله ها: ${totalInput + totalExit} | ✅ ورود: $totalInput | ⬅️ خروج: $totalExit"
        val summaryExpanded = buildString {
            append("📊 وضعیت لحظه ای عملیات:\n")
            append("🔸 کل حواله ها: ${totalInput + totalExit}\n")
            append("🔹 کل ورودی: $totalInput\n")
            append("🔸 کل خروجی: $totalExit\n")
            append("⚖️ مجموع تناژ: ${formatWeight(totalWeight)}")
        }
        showTotalStats(summaryTitle, summaryContent, summaryExpanded)
    }

    private fun formatWeight(weight: Int): String {
        return "%,d کیلوگرم".format(weight)
    }

    /**
     * نمایش اعلان برای یک کشتی خاص
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showShipNotification(shipName: String, title: String, content: String, expandedText: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("SHIP_NAME", shipName)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, shipName.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_LOADING)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setContentIntent(pendingIntent)
            .setGroup("LOADING_GROUP_V2")
            .setAutoCancel(true)
            .build()

        notificationManager.notify(shipName.hashCode(), notification)
    }

    /**
     * نمایش آمار کلی بارگیری
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showTotalStats(title: String, content: String, expandedText: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_LOADING)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup("LOADING_GROUP_V2")
            .setGroupSummary(false) // این به عنوان یکی از اعضای گروه نمایش داده می‌شود
            .build()

        notificationManager.notify(SUMMARY_LOADING_ID, notification)
    }

    /**
     * پاک کردن تمامی اعلان‌ها
     */
    fun clearAll() {
        notificationManager.cancelAll()
    }
}
