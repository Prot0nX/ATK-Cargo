# راهنمای سیستم مدیریت جلسات کاربری

## مقدمه
سیستم مدیریت جلسات کاربری ATK-Cargo به‌صورت کامل بازنویسی و بهینه‌سازی شده است تا امنیت و کارایی بالایی را فراهم کند.

## ویژگی‌های کلیدی

### 🔐 امنیت پیشرفته
- مدیریت جلسات بر اساس دستگاه و کاربر
- جلوگیری از ورود همزمان از دستگاه‌های مختلف
- پاکسازی خودکار جلسات منقضی شده
- ثبت کامل لاگ‌های فعالیت

### ⚡ عملکرد بهینه
- استفاده از ایندکس‌های ترکیبی برای سرعت بالا
- کلاس SessionManager برای مدیریت متمرکز
- پاکسازی خودکار جلسات قدیمی
- کش کردن اطلاعات جلسات

### 📊 قابلیت‌های مدیریتی
- نمایش کاربران آنلاین
- خروج اجباری کاربران
- تنظیم مدت زمان انقضای جلسه
- آمارگیری کامل از فعالیت‌ها

## ساختار فایل‌ها

### فایل‌های اصلی

#### 1. `SessionManager.php`
کلاس اصلی مدیریت جلسات که شامل متدهای زیر است:

```php
// ایجاد جلسه جدید
$sessionManager->createSession($username, $deviceId, $deviceModel, $androidVersion, $ipAddress);

// بررسی فعال بودن جلسه
$sessionManager->isSessionActive($username, $deviceId);

// غیرفعال کردن جلسه
$sessionManager->deactivateSession($username, $deviceId);

// دریافت اطلاعات جلسه فعال
$sessionManager->getActiveSession($username);

// به‌روزرسانی فعالیت جلسه
$sessionManager->updateSessionActivity($username, $deviceId);

// دریافت کاربران آنلاین
$sessionManager->getOnlineUsers();

// پاکسازی جلسات منقضی
$sessionManager->cleanupExpiredSessions();
```

#### 2. `session_api.php`
API کامل برای مدیریت جلسات از طریق درخواست‌های HTTP:

- `GET /session_api.php?action=get_online_users` - دریافت کاربران آنلاین
- `POST /session_api.php` با `action=check_session_status` - بررسی وضعیت جلسه
- `POST /session_api.php` با `action=force_logout` - خروج اجباری
- `POST /session_api.php` با `action=cleanup_sessions` - پاکسازی جلسات

#### 3. فایل‌های به‌روزرسانی شده
- `check_Auth.php` - احراز هویت با SessionManager
- `check_logout.php` - خروج با SessionManager
- `check_session.php` - بررسی جلسه با SessionManager

## نحوه استفاده

### 1. ورود کاربر
```php
require_once 'SessionManager.php';

$sessionManager = new SessionManager();

try {
    $result = $sessionManager->createSession(
        $username,
        $deviceId,
        $deviceModel,
        $androidVersion,
        $_SERVER['REMOTE_ADDR']
    );
    
    if ($result['success']) {
        // ورود موفق
        echo json_encode(['success' => true, 'message' => 'ورود موفقیت‌آمیز']);
    }
} catch (Exception $e) {
    // مدیریت خطا
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
}
```

### 2. بررسی وضعیت جلسه
```php
$sessionManager = new SessionManager();

if ($sessionManager->isSessionActive($username, $deviceId)) {
    // کاربر آنلاین است
    $sessionManager->updateSessionActivity($username, $deviceId);
} else {
    // جلسه منقضی شده
    echo json_encode(['success' => false, 'message' => 'لطفاً مجدداً وارد شوید']);
}
```

### 3. خروج کاربر
```php
$sessionManager = new SessionManager();

$result = $sessionManager->deactivateSession($username, $deviceId);
echo json_encode($result);
```

### 4. دریافت کاربران آنلاین
```php
$sessionManager = new SessionManager();

$onlineUsers = $sessionManager->getOnlineUsers();
echo json_encode([
    'success' => true,
    'data' => [
        'count' => count($onlineUsers),
        'users' => $onlineUsers
    ]
]);
```

## تنظیمات پیشرفته

### تنظیم مدت زمان انقضای جلسه
```php
$sessionManager = new SessionManager();

// تنظیم انقضا به 2 ساعت (7200 ثانیه)
$sessionManager->setSessionTimeout(7200);

// دریافت مدت زمان فعلی
$currentTimeout = $sessionManager->getSessionTimeout();
```

### پاکسازی دستی جلسات
```php
$sessionManager = new SessionManager();

// پاکسازی جلسات منقضی شده
$sessionManager->cleanupExpiredSessions();
```

## ساختار دیتابیس

### جدول `user_sessions`
```sql
CREATE TABLE `user_sessions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT 'نام کاربری',
  `device_id` varchar(100) NOT NULL COMMENT 'شناسه دستگاه',
  `device_model` varchar(100) DEFAULT NULL COMMENT 'مدل دستگاه',
  `android_version` varchar(20) DEFAULT NULL COMMENT 'نسخه اندروید',
  `login_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'زمان ورود',
  `logout_time` datetime DEFAULT NULL COMMENT 'زمان خروج',
  `is_active` tinyint(1) NOT NULL DEFAULT 1 COMMENT 'وضعیت فعال',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'آدرس IP',
  `last_activity` datetime DEFAULT NULL COMMENT 'آخرین فعالیت',
  `session_token` varchar(128) DEFAULT NULL COMMENT 'توکن جلسه',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_active_session` (`username`, `device_id`, `is_active`)
);
```

### ایندکس‌های بهینه‌سازی
- `idx_username_active` - برای جستجوی سریع جلسات فعال کاربر
- `idx_device_active` - برای بررسی وضعیت دستگاه
- `idx_active_activity` - برای پاکسازی جلسات منقضی
- `idx_username_device_active` - برای بررسی جلسه مشخص

## لاگ‌ها و نظارت

### فایل‌های لاگ
- `logs/session_activity.log` - فعالیت‌های جلسات
- `logs/admin_actions.log` - اقدامات مدیریتی
- `logs/login.log` - ورودهای سیستم
- `logs/logout.log` - خروج‌های سیستم

### نمونه لاگ
```
[2024-01-15 14:30:25] LOGIN | کاربر: admin | دستگاه: SM-G973F | IP: 192.168.1.100
[2024-01-15 14:35:10] LOGOUT | کاربر: admin | دستگاه: SM-G973F | IP: 192.168.1.100
[2024-01-15 14:40:15] FORCE LOGOUT - Target: user1, Admin: admin, IP: 192.168.1.100
```

## کوئری‌های مفید

### نمایش کاربران آنلاین
```sql
SELECT username, device_model, login_time, ip_address 
FROM user_sessions 
WHERE is_active = 1 
ORDER BY login_time DESC;
```

### آمار ورود روزانه
```sql
SELECT DATE(login_time) as date, COUNT(*) as login_count 
FROM user_sessions 
WHERE login_time >= CURDATE() - INTERVAL 7 DAY 
GROUP BY DATE(login_time) 
ORDER BY date DESC;
```

### جلسات منقضی شده
```sql
SELECT * FROM user_sessions 
WHERE is_active = 1 
AND TIMESTAMPDIFF(HOUR, COALESCE(last_activity, login_time), NOW()) > 1;
```

## بهترین روش‌ها

### 1. امنیت
- همیشه از HTTPS استفاده کنید
- IP کاربران را ثبت کنید
- لاگ‌های امنیتی را نگهداری کنید
- مدت زمان انقضای مناسب تنظیم کنید

### 2. عملکرد
- به‌طور منظم جلسات منقضی را پاک کنید
- از ایندکس‌های مناسب استفاده کنید
- فعالیت جلسات را به‌روزرسانی کنید

### 3. نظارت
- لاگ‌ها را به‌طور منظم بررسی کنید
- آمار کاربران آنلاین را نظارت کنید
- جلسات مشکوک را شناسایی کنید

## عیب‌یابی

### مشکلات رایج

#### 1. کاربر نمی‌تواند وارد شود
- بررسی کنید که جلسه قبلی غیرفعال شده باشد
- لاگ‌های خطا را بررسی کنید
- اتصال دیتابیس را تست کنید

#### 2. جلسه به‌طور غیرمنتظره منقضی می‌شود
- مدت زمان انقضا را بررسی کنید
- فعالیت کاربر را به‌روزرسانی کنید
- تنظیمات سرور را بررسی کنید

#### 3. عملکرد کند
- ایندکس‌های دیتابیس را بررسی کنید
- جلسات قدیمی را پاک کنید
- کوئری‌ها را بهینه‌سازی کنید

## نتیجه‌گیری

سیستم جدید مدیریت جلسات ATK-Cargo امنیت، کارایی و قابلیت مدیریت بالایی را فراهم می‌کند. با پیروی از این راهنما، می‌توانید از تمام قابلیت‌های سیستم به‌طور بهینه استفاده کنید.

برای سوالات بیشتر یا گزارش مشکلات، لطفاً با تیم توسعه تماس بگیرید.