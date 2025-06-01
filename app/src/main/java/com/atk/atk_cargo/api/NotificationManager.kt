package com.atk.atk_cargo.api

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.atk.atk_cargo.MainActivity
import com.atk.atk_cargo.R
import com.atk.atk_cargo.formatNumber
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * کلاس مدیریت نوتیفیکیشن‌های بارگیری لحظه‌ای
 * این کلاس مسئول ایجاد و نمایش نوتیفیکیشن‌های مربوط به اطلاعات بارگیری لحظه‌ای است
 */
class LoadingNotificationManager(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)
    private val userPreferencesManager = UserPreferencesManager(context)
    
    companion object {
        const val CHANNEL_ID = "loading_notifications"
        private const val GROUP_KEY = "com.atk.atk_cargo.LOADING_NOTIFICATIONS"
        private const val MAIN_STATUS_ID = 1
        private const val SUMMARY_ID = 0
        private const val SHIPS_PREFS_NAME = "ship_notifications_prefs"
        private const val KEY_MUTED_SHIPS = "muted_ships"
    }

    /**
     * ایجاد کانال نوتیفیکیشن (فقط برای اندروید 8.0 به بالا)
     */
    fun createNotificationChannel() {
        val name = "بارگیری لحظه‌ای"
        val descriptionText = "نوتیفیکیشن‌های مربوط به اطلاعات بارگیری لحظه‌ای"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            enableLights(true)
            lightColor = Color.BLUE
            enableVibration(true)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * بررسی سطح دسترسی کاربر
     * فقط کاربران با نقش مدیر نوتیفیکیشن دریافت می‌کنند
     */
    private fun isAdmin(): Boolean {
        return runBlocking {
            userPreferencesManager.userType.first() == "admin"
        }
    }

    /**
     * بررسی مجوز نوتیفیکیشن
     */
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * نمایش نوتیفیکیشن‌های بارگیری به تفکیک کشتی
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showLoadingNotifications(loadingDataList: List<RealTimeLoadingData>, isRefresh: Boolean = false) {
        // بررسی سطح دسترسی کاربر و مجوز نوتیفیکیشن
        if (!isAdmin() || !hasNotificationPermission()) {
            return
        }
        
        // گروه‌بندی داده‌ها بر اساس کشتی
        val shipGroups = loadingDataList.groupBy { it.shipName }
        
        // لیست کشتی‌هایی که نوتیفیکیشن‌ آنها غیرفعال شده
        val mutedShips = getMutedShips()
        
        // تعداد کشتی‌های فعال
        var activeShipCount = 0
        
        // آمار کلی بارگیری
        var totalVouchers = 0
        var totalInputVouchers = 0
        var totalExitVouchers = 0
        var totalNetWeight = 0
        
        // ابتدا جمع‌آوری آمار کلی
        shipGroups.forEach { (_, shipData) ->
            shipData.forEach { data ->
                // محاسبه مجموع حواله‌ها (ورودی + خروجی)
                totalVouchers += (data.entryVouchers + data.exitVouchers)
                totalInputVouchers += data.entryVouchers
                totalExitVouchers += data.exitVouchers
                totalNetWeight += data.totalNetWeight
            }
        }
        
        // ابتدا نمایش نوتیفیکیشن برای هر کشتی
        shipGroups.forEach { (shipName, shipData) ->
            // بررسی آیا این کشتی غیرفعال شده است
            if (!mutedShips.contains(shipName)) {
                showShipNotification(shipName, shipData)
                activeShipCount++
            } else if (isRefresh) {
                // در صورت بروزرسانی دستی، کشتی‌های غیرفعال را هم با قابلیت فعال‌سازی مجدد نمایش می‌دهیم
                showMutedShipNotification(shipName, shipData)
            }
        }
        
        // اگر کشتی‌های غیرفعال شده داریم، نوتیفیکیشن مدیریت آنها را نمایش دهیم
        if (mutedShips.isNotEmpty() && !isRefresh) {
            showMutedShipsManagementNotification(mutedShips.size)
        }
        
        // سپس نمایش نوتیفیکیشن آمار کلی (با اولویت بالاتر)
        showMainStatusNotification(totalVouchers, totalInputVouchers, totalExitVouchers, totalNetWeight, isRefresh)
        
        // اگر نوتیفیکیشن آمار کلی یا کشتی‌های فعال نمایش داده شده‌اند،
        // نوتیفیکیشن وضعیت سرویس را حذف کنیم تا اسپم نباشد
        if (totalVouchers > 0 || activeShipCount > 0) {
            notificationManager.cancel(LoadingNotificationService.FOREGROUND_ID)
        }
        // اگر هیچ کشتی فعالی نداریم، فقط نوتیفیکیشن وضعیت سرویس را نمایش دهیم
        else if (activeShipCount == 0) {
            showServiceStatusNotification()
        }
        
        // نمایش نوتیفیکیشن خلاصه برای گروه
        if (activeShipCount > 1) {
            showSummaryNotification(activeShipCount)
        }
    }

    /**
     * نمایش نوتیفیکیشن برای یک کشتی
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showShipNotification(shipName: String, shipData: List<RealTimeLoadingData>) {
        // برای شناسه منحصر به فرد نوتیفیکیشن از هش نام کشتی استفاده می‌کنیم
        val notificationId = shipName.hashCode()
        
        // محاسبه مجموع حواله‌های خروجی و ورودی و تناژ خالص
        val totalExitVouchers = shipData.sumOf { it.exitVouchers }
        val totalInputVouchers = shipData.sumOf { it.entryVouchers }
        val totalNetWeight = formatNumber(shipData.sumOf { it.totalNetWeight })
        
        // گرفتن نام انبار(ها) برای نمایش در حالت بسته
        val warehouses = shipData.map { it.loadingWarehouse }.distinct()
        
        // متن عنوان برای حالت بسته با اضافه کردن نام انبار
        val contentTitle = if (warehouses.size == 1) {
            "کشتی $shipName (${warehouses.first()})"
        } else {
            "کشتی $shipName (${warehouses.size} انبار)"
        }
        
        // ایجاد اینتنت برای باز شدن برنامه هنگام کلیک روی نوتیفیکیشن
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("SHIP_NAME", shipName)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            notificationId, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // ایجاد اینتنت برای غیرفعال کردن نوتیفیکیشن کشتی
        val muteIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MUTE_SHIP
            putExtra(NotificationActionReceiver.EXTRA_SHIP_NAME, shipName)
        }
        
        val mutePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1000,
            muteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // متن جزئیات برای حالت گسترده
        val expandedText = buildExpandedText(shipData)
        
        // متن خلاصه با نمایش حواله‌های ورودی و خروجی
        val contentText = "✅ ورود: $totalInputVouchers | ⬅️ خروج: $totalExitVouchers | ⚖️ تناژ: $totalNetWeight"
        
        // ساخت نوتیفیکیشن
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup(GROUP_KEY)
            // تنظیم کلید مرتب‌سازی بر اساس نام کشتی
            .setSortKey("a_ship_$shipName")  // استفاده از a برای قرار گرفتن در ابتدای فهرست، قبل از اعلان آمار کلی
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .addAction(
                R.drawable.ic_notification_mute,
                "غیرفعال کردن اعلان",
                mutePendingIntent
            )
            .build()
            
        // نمایش نوتیفیکیشن
        notificationManager.notify(notificationId, notification)
    }
    
    /**
     * ساخت متن جزئیات برای نمایش در حالت گسترده
     */
    private fun buildExpandedText(shipData: List<RealTimeLoadingData>): String {
        val stringBuilder = StringBuilder()
        
        // گروه‌بندی بر اساس انبار
        val warehouseGroups = shipData.groupBy { it.loadingWarehouse }
        
        // اگر فقط یک انبار وجود دارد، به همان روش قبلی نمایش داده شود
        if (warehouseGroups.size == 1) {
            warehouseGroups.forEach { (warehouseName, warehouseData) ->
                // آمار کلی انبار
                val totalInputVouchers = warehouseData.sumOf { it.entryVouchers }
                val totalExitVouchers = warehouseData.sumOf { it.exitVouchers }
                val totalNetWeight = formatNumber(warehouseData.sumOf { it.totalNetWeight })
                val totalVouchers = totalInputVouchers + totalExitVouchers

                // نمایش اطلاعات کلی انبار: نام انبار در یک خط و اطلاعات در خط بعدی
                stringBuilder.append("📦 $warehouseName\n")
                stringBuilder.append("حواله: $totalVouchers | ورود: $totalInputVouchers | خروج: $totalExitVouchers | تناژ: $totalNetWeight\n\n")
            }
        } else {
            // اگر بیش از یک انبار وجود دارد، با خط جداکننده نمایش داده شود
            
            val warehouseList = warehouseGroups.entries.toList()
            for (i in warehouseList.indices) {
                val (warehouseName, warehouseData) = warehouseList[i]
                // آمار کلی انبار
                val totalInputVouchers = warehouseData.sumOf { it.entryVouchers }
                val totalExitVouchers = warehouseData.sumOf { it.exitVouchers }
                val totalNetWeight = formatNumber(warehouseData.sumOf { it.totalNetWeight })
                val totalVouchers = totalInputVouchers + totalExitVouchers

                // نمایش اطلاعات کلی انبار: نام انبار در یک خط و اطلاعات در خط بعدی
                stringBuilder.append("📦 $warehouseName\n")
                stringBuilder.append("حواله: $totalVouchers | ورود: $totalInputVouchers | خروج: $totalExitVouchers | تناژ: $totalNetWeight")
                
                // اضافه کردن خط جداکننده بین انبارها (به جز آخرین انبار)
                if (i < warehouseList.size - 1) {
                    stringBuilder.append("\n" + "-".repeat(30) + "\n")
                } else {
                    stringBuilder.append("\n\n")
                }
            }
        }
        
        return stringBuilder.toString()
    }
    
    /**
     * نمایش نوتیفیکیشن خلاصه برای گروه
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showSummaryNotification(shipCount: Int) {
        val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("بارگیری لحظه‌ای")
            .setContentText("اطلاعات بارگیری $shipCount کشتی")
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .build()
            
        notificationManager.notify(SUMMARY_ID, summaryNotification)
    }
    
    /**
     * نمایش نوتیفیکیشن آمار کلی بارگیری
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showMainStatusNotification(
        totalVouchers: Int,
        totalInputVouchers: Int,
        totalExitVouchers: Int,
        totalNetWeight: Int,
        isRefresh: Boolean = false
    ) {
        val contentText = "کل: $totalVouchers | ✅ ورود: $totalInputVouchers | ⬅️ خروج: $totalExitVouchers"

        // ساخت متن گسترده با تقسیم‌بندی مناسب
        val expandedTextBuilder = StringBuilder()
        expandedTextBuilder.append("🔸 کل: $totalVouchers | ورودی‌: $totalInputVouchers | خروجی‌: $totalExitVouchers | تناژ: ${formatNumber(totalNetWeight)}\n\n")
        
        // اضافه کردن زمان آخرین بروزرسانی
        val updateTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        if (isRefresh) {
            expandedTextBuilder.append("🔄 بروزرسانی شده در $updateTime\n")
        } else {
            expandedTextBuilder.append("⏱ آخرین بروزرسانی: $updateTime\n")
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("SHOW_REPORTS", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            MAIN_STATUS_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // اینتنت برای بروزرسانی نوتیفیکیشن‌ها
        val refreshIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_REFRESH_NOTIFICATIONS
        }
        
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            1000,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // اینتنت برای غیرفعال کردن نوتیفیکیشن‌ها در شیفت فعلی
        val disableIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_DISABLE_SHIFT_NOTIFICATIONS
        }
        
        val disablePendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            disableIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle("آمار کلی بارگیری")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedTextBuilder.toString()))
            // استفاده از اولویت بسیار بالا برای اعلان آمار کلی
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup(GROUP_KEY)
            // تنظیمات اولویت و ترتیب نمایش
            .setSortKey("z_total_stats")  // استفاده از z برای قرار گرفتن در انتهای فهرست
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            // تنظیم وزن اعلان برای اطمینان از نمایش در بالا
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_notification_mute,
                "غیرفعال کردن",
                disablePendingIntent
            )
            .addAction(
                R.drawable.ic_notification_icon,
                "بروزرسانی",
                refreshPendingIntent
            )
            .build()
            
        notificationManager.notify(MAIN_STATUS_ID, notification)
    }

    /**
     * نمایش نوتیفیکیشن وضعیت سرویس
     * این نوتیفیکیشن زمانی نمایش داده می‌شود که هیچ کشتی‌ای برای نمایش نوتیفیکیشن وجود نداشته باشد
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showServiceStatusNotification() {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle("بارگیری لحظه‌ای")
            .setContentText("در حال بررسی بارگیری...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
            
        notificationManager.notify(LoadingNotificationService.FOREGROUND_ID, notification)
    }
    
    /**
     * دریافت لیست کشتی‌هایی که نوتیفیکیشن‌ آنها غیرفعال شده
     */
    private fun getMutedShips(): Set<String> {
        return context.getSharedPreferences(SHIPS_PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_MUTED_SHIPS, emptySet()) ?: emptySet()
    }
    
    /**
     * افزودن یک کشتی به لیست کشتی‌های غیرفعال شده
     */
    fun muteShip(shipName: String) {
        val prefs = context.getSharedPreferences(SHIPS_PREFS_NAME, Context.MODE_PRIVATE)
        val mutedShips = HashSet(getMutedShips())
        mutedShips.add(shipName)
        
        prefs.edit {
            putStringSet(KEY_MUTED_SHIPS, mutedShips)
        }
        
        // حذف نوتیفیکیشن مربوط به این کشتی
        notificationManager.cancel(shipName.hashCode())
    }
    
    /**
     * حذف یک کشتی از لیست کشتی‌های غیرفعال شده
     */
    fun unmuteShip(shipName: String) {
        val prefs = context.getSharedPreferences(SHIPS_PREFS_NAME, Context.MODE_PRIVATE)
        val mutedShips = HashSet(getMutedShips())
        mutedShips.remove(shipName)
        
        prefs.edit {
            putStringSet(KEY_MUTED_SHIPS, mutedShips)
        }
    }
    
    /**
     * پاک کردن لیست کشتی‌های غیرفعال شده
     */
    fun clearMutedShips() {
        context.getSharedPreferences(SHIPS_PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                clear()
            }
    }
    
    /**
     * پاک کردن تمام نوتیفیکیشن‌ها
     */
    fun clearNotifications() {
        notificationManager.cancelAll()
    }

    /**
     * نمایش نوتیفیکیشن مدیریت کشتی‌های غیرفعال شده
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showMutedShipsManagementNotification(mutedShipsCount: Int) {
        // اینتنت برای پاک کردن لیست کشتی‌های غیرفعال شده
        val clearIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_CLEAR_MUTED_SHIPS
        }
        
        val clearPendingIntent = PendingIntent.getBroadcast(
            context,
            2000,
            clearIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_mute)
            .setContentTitle("کشتی‌های غیرفعال شده")
            .setContentText("$mutedShipsCount کشتی غیرفعال شده دارید")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setGroup(GROUP_KEY)
            // تنظیم کلید مرتب‌سازی برای اعلان مدیریت کشتی‌های غیرفعال شده
            .setSortKey("c_muted_management")  // استفاده از c برای قرار گرفتن بعد از کشتی‌های غیرفعال اما قبل از اعلان آمار کلی
            .addAction(
                R.drawable.ic_notification_icon,
                "فعال‌سازی همه",
                clearPendingIntent
            )
            .build()
            
        notificationManager.notify(2000, notification)
    }

    /**
     * نمایش نوتیفیکیشن برای کشتی غیرفعال شده با قابلیت فعال‌سازی مجدد
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showMutedShipNotification(shipName: String, shipData: List<RealTimeLoadingData>) {
        // برای شناسه منحصر به فرد نوتیفیکیشن از هش نام کشتی استفاده می‌کنیم
        val notificationId = shipName.hashCode()
        
        // محاسبه مجموع حواله‌های خروجی و ورودی و تناژ خالص
        val totalExitVouchers = shipData.sumOf { it.exitVouchers }
        val totalInputVouchers = shipData.sumOf { it.entryVouchers }
        val totalNetWeight = formatNumber(shipData.sumOf { it.totalNetWeight })
        
        // گرفتن نام انبار(ها) برای نمایش در حالت بسته
        val warehouses = shipData.map { it.loadingWarehouse }.distinct()
        
        // متن عنوان برای حالت بسته با اضافه کردن نام انبار
        val contentTitle = if (warehouses.size == 1) {
            "کشتی $shipName (${warehouses.first()}) - غیرفعال"
        } else {
            "کشتی $shipName (${warehouses.size} انبار) - غیرفعال"
        }
        
        // ایجاد اینتنت برای فعال‌سازی مجدد نوتیفیکیشن این کشتی
        val unmuteIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_UNMUTE_SHIP
            putExtra(NotificationActionReceiver.EXTRA_SHIP_NAME, shipName)
        }
        
        val unmutePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 5000,
            unmuteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // متن جزئیات برای حالت گسترده
        val expandedText = buildExpandedText(shipData)
        
        // متن خلاصه با نمایش حواله‌های ورودی و خروجی
        val contentText = "✅ ورود: $totalInputVouchers | ⬅️ خروج: $totalExitVouchers | ⚖️ تناژ: $totalNetWeight"
        
        // ساخت نوتیفیکیشن
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_mute)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setGroup(GROUP_KEY)
            // تنظیم کلید مرتب‌سازی برای کشتی‌های غیرفعال شده
            .setSortKey("b_muted_ship_$shipName")  // استفاده از b برای قرار گرفتن بعد از کشتی‌های فعال اما قبل از اعلان آمار کلی
            .addAction(
                R.drawable.ic_notification_icon,
                "فعال‌سازی مجدد",
                unmutePendingIntent
            )
            .build()
            
        // نمایش نوتیفیکیشن
        notificationManager.notify(notificationId + 3000, notification)
    }
} 