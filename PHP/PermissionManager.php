<?php
// پنل مدیریت متمرکز نقش‌ها و دسترسی‌های اختصاصی کاربران (V2)

// نمایش خطا در production غیرفعال است تا جزئیات داخلی افشا نشود؛ خطاها همچنان لاگ می‌شوند
error_reporting(E_ALL);
ini_set('display_errors', '0');
ini_set('log_errors', '1');

date_default_timezone_set('Asia/Tehran');

// کوکی نشست باید قبل از session_start تنظیم شود؛ قبلاً httponly/secure/samesite اعمال نمی‌شد
session_set_cookie_params([
    'lifetime' => 0,
    'path' => '/',
    'secure' => !empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off',
    'httponly' => true,
    'samesite' => 'Lax',
]);
session_start();

if (empty($_SESSION['csrf_token'])) {
    $_SESSION['csrf_token'] = bin2hex(random_bytes(32));
}

require_once __DIR__ . '/config/config.php';
require_once __DIR__ . '/vendor/autoload.php';

// LoginAttemptLimiter به APP_ROOT نیاز دارد؛ چون bootstrap.php اینجا require نمی‌شود، مستقل تعریف می‌گردد
if (!defined('APP_ROOT')) {
    define('APP_ROOT', __DIR__);
}

use App\Core\MicroCache;
use App\Services\LoginAttemptLimiter;
use App\Services\PermissionService;
use App\Repositories\PermissionRepository;

// منبع مجوزها از config/permissions.json به جداول دیتابیس منتقل شده؛ اگر migration هنوز اجرا نشده این پنل خطای صریح می‌دهد ولی بررسی مجوز همچنان به فایل قدیمی fallback می‌کند
$permissionRepository = new PermissionRepository();
$dbMigrated = true;
try {
    $permissionRepository->getAllRolePermissions();
} catch (\Throwable $e) {
    $dbMigrated = false;
}

$ADMIN_PASSWORD_HASH = $_ENV['ADMIN_PASSWORD_HASH'] ?? getenv('ADMIN_PASSWORD_HASH') ?: '';

$is_authenticated = isset($_SESSION['perm_manager_auth']) && $_SESSION['perm_manager_auth'] === true;

// اعتبارسنجی CSRF برای تمامی درخواست‌های POST
$csrf_valid = true;
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $submittedToken = $_POST['csrf_token'] ?? '';
    if (!hash_equals($_SESSION['csrf_token'], $submittedToken)) {
        $csrf_valid = false;
    }
}

// هندل لاگین با همان LoginAttemptLimiter مورد استفاده در API برای جلوگیری از brute-force روی رمز ادمین
$loginAttemptLimiter = new LoginAttemptLimiter();
$clientIp = (string)($_SERVER['REMOTE_ADDR'] ?? 'unknown');

if (isset($_POST['login'])) {
    if (!$csrf_valid) {
        $login_error = "توکن امنیتی (CSRF) نامعتبر است. لطفاً صفحه را بازنشانی کنید.";
    } elseif ($loginAttemptLimiter->isLocked('permmgr', $clientIp)) {
        $login_error = "تعداد تلاش‌های ناموفق بیش از حد مجاز است. لطفاً ۱۵ دقیقه دیگر تلاش کنید.";
    } elseif (empty($ADMIN_PASSWORD_HASH)) {
        $login_error = "خطای پیکربندی: متغیر ADMIN_PASSWORD_HASH در فایل .env تنظیم نشده است.";
    } elseif (password_verify($_POST['password'], $ADMIN_PASSWORD_HASH)) {
        $loginAttemptLimiter->resetAttempts('permmgr', $clientIp);
        session_regenerate_id(true);
        $_SESSION['perm_manager_auth'] = true;
        $_SESSION['last_activity'] = time();
        header("Location: PermissionManager.php");
        exit;
    } else {
        $loginAttemptLimiter->registerFailedAttempt('permmgr', $clientIp);
        $login_error = "رمز عبور اشتباه است!";
    }
}

// هندل کردن خروج
if (isset($_GET['logout'])) {
    $_SESSION = [];
    if (ini_get('session.use_cookies')) {
        $p = session_get_cookie_params();
        setcookie(session_name(), '', time() - 42000, $p['path'], $p['domain'], $p['secure'], $p['httponly']);
    }
    session_destroy();
    header("Location: PermissionManager.php");
    exit;
}

// انقضای نشست
if ($is_authenticated && (time() - $_SESSION['last_activity'] > 1800)) {
    session_destroy();
    header("Location: PermissionManager.php");
    exit;
}
if ($is_authenticated) $_SESSION['last_activity'] = time();

// $all_data همان ساختار قبلی (roles/users) را حفظ می‌کند تا تمپلیت پایین بدون تغییر کار کند
$all_data = ['roles' => ['admin' => [], 'operator' => [], 'verifier' => []], 'users' => []];
if ($dbMigrated) {
    $all_data = [
        'roles' => $permissionRepository->getAllRolePermissions(),
        'users' => $permissionRepository->getAllUserPermissions(),
    ];
}

// هندل کردن ذخیره‌سازی
if ($is_authenticated && isset($_POST['save_permissions'])) {
    if (!$dbMigrated) {
        $error_msg = "دیتابیس مجوزها هنوز migrate نشده است. ابتدا migrations/2026_08_19_permissions_to_database.sql را روی این سرور اجرا کنید.";
    } elseif (!$csrf_valid) {
        $error_msg = "توکن امنیتی (CSRF) نامعتبر است. لطفاً صفحه را بازنشانی کنید.";
    } else {
        $type = $_POST['target_type']; // 'role' or 'user'
        $target_name = $_POST['target_name']; // role name or username

        $features = [
            'initial_info', 'select_info', 'cargo_counter', 'manage_ships',
            'manage_users', 'admin_chat', 'edit_cargo', 'delete_cargo',
            'view_reports', 'tonnage_warning', 'manage_quotas',
            'view_monitoring'
        ];

        $new_perms = [];
        foreach ($features as $feature) {
            $new_perms[$feature] = isset($_POST["perm_{$feature}"]);
        }

        try {
            if ($type === 'role') {
                $permissionRepository->saveRolePermissions($target_name, $new_perms);
                $all_data['roles'][$target_name] = $new_perms;
            } else {
 // اگر تمام گزینه‌ها غیرفعال بود و کاربر خواست "تنظیم اختصاصی" را حذف کند
                if (isset($_POST['delete_user_custom']) && $_POST['delete_user_custom'] == '1') {
                    $permissionRepository->deleteUserPermissions($target_name);
                    unset($all_data['users'][$target_name]);
                } else {
                    $permissionRepository->saveUserPermissions($target_name, $new_perms);
                    $all_data['users'][$target_name] = $new_perms;
                }
            }

 // بدون این خط تغییرات تا انقضای TTL کش (۳۰ ثانیه) در بررسی مجوز اعمال نمی‌شد
            MicroCache::forget(PermissionService::CACHE_KEY);
            $success_msg = "تنظیمات " . ($type === 'role' ? "نقش" : "کاربر") . " با موفقیت به‌روزرسانی شد.";
        } catch (\Throwable $e) {
            error_log('PermissionManager save failed: ' . $e->getMessage());
            $error_msg = "خطای دیتابیس در ذخیره‌سازی تنظیمات!";
        }
    }
}

// دریافت لیست کاربران از دیتابیس برای انتخاب
$users_list = [];
if ($is_authenticated) {
    try {
 // قبلاً از یک اتصال مستقل استفاده می‌شد که تنظیمات SET SESSION را نداشت و باعث دو سوکت جدا به دیتابیس می‌شد
        $conn = \App\Core\Database::getInstance()->getPdoConnection();
        $stmt = $conn->query("SELECT username, fullName, userType FROM Users ORDER BY username ASC");
        while ($row = $stmt->fetch(\PDO::FETCH_ASSOC)) {
            $users_list[] = $row;
        }
    } catch (Exception $e) {
 // خطا در دیتابیس
    }
}

$feature_labels = [
    'initial_info' => ['label' => 'تعریف کشتی (Initial Info)', 'icon' => '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 21c.6.5 1.2 1 2.5 1 2.5 0 3.2-1.2 5.5-1.2 2.3 0 3 1.2 5.5 1.2 1.3 0 1.9-.5 2.5-1M19.38 20A11.6 11.6 0 0 0 21 14l-9-4-9 4c0 2.9.94 5.34 2.81 7.76"/></svg>'],
    'select_info' => ['label' => 'ثبت حواله (Select Info)', 'icon' => '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><line x1="10" y1="9" x2="8" y2="9"/></svg>'],
    'cargo_counter' => ['label' => 'پایش بارگیری (Counter)', 'icon' => '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="16" height="20" x="4" y="2" rx="2"/><line x1="8" x2="16" y1="6" y2="6"/><line x1="16" x2="16.01" y1="10" y2="10"/><line x1="12" x2="12.01" y1="10" y2="10"/><line x1="8" x2="8.01" y1="10" y2="10"/><line x1="16" x2="16.01" y1="14" y2="14"/><line x1="12" x2="12.01" y1="14" y2="14"/><line x1="8" x2="8.01" y1="14" y2="14"/><line x1="16" x2="16.01" y1="18" y2="18"/><line x1="12" x2="12.01" y1="18" y2="18"/><line x1="8" x2="8.01" y1="18" y2="18"/></svg>'],
    'manage_ships' => ['label' => 'گزارشات و کشتی‌ها', 'icon' => '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="m19 9-5 5-4-4-3 3"/></svg>'],
    'manage_users' => ['label' => 'مدیریت کاربران', 'icon' => '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>'],
    'admin_chat' => ['label' => 'ارتباطات (چت ادمین)', 'icon' => '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M7.9 20A9 9 0 1 0 4 16.1L2 22Z"/></svg>'],
    'edit_cargo' => ['label' => 'ویرایش حواله', 'icon' => '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/><path d="m15 5 4 4"/></svg>'],
    'delete_cargo' => ['label' => 'حذف حواله', 'icon' => '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/><line x1="10" x2="10" y1="11" y2="17"/><line x1="14" x2="14" y1="11" y2="17"/></svg>'],
    'view_reports' => ['label' => 'مشاهده آمار تحلیلی', 'icon' => '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.21 15.89A10 10 0 1 1 8 2.83"/><path d="M22 12A10 10 0 0 0 12 2v10z"/></svg>'],
    'tonnage_warning' => ['label' => 'هشدار تناژ سیستمی', 'icon' => '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><path d="M12 9v4"/><path d="M12 17h.01"/></svg>'],
 // دسترسی مشترک رویدادهای مانیتورینگ سلامت و امنیت سیستم
    'view_monitoring' => ['label' => 'مانیتورینگ (رویدادهای سلامت و امنیت)', 'icon' => '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>']
];

?>
<!DOCTYPE html>
<html lang="fa" dir="rtl" data-theme="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>ATK-Cargo | مدیریت پیشرفته دسترسی</title>
    <meta name="description" content="پنل مدیریت متمرکز نقش‌ها و دسترسی‌های کاربران سیستم ATK-Cargo">
    <link href="https://cdn.jsdelivr.net/gh/rastikerdar/vazirmatn@v33.003/Vazirmatn-font-face.css" rel="stylesheet" type="text/css" />
    <link rel="stylesheet" href="assets/permission-manager.css">
    <script>
 /* FOUC Prevention — apply saved theme before paint */
        (function(){
            var t = localStorage.getItem('atk_theme_pref');
            if(!t) t = window.matchMedia('(prefers-color-scheme:dark)').matches ? 'dark' : 'light';
            document.documentElement.setAttribute('data-theme', t);
        })();
    </script>
</head>
<body>

<div id="toast-container"></div>

<div class="container">
    <div class="glass-panel">
        <?php if (!$is_authenticated): ?>
            <div class="login-wrapper">
                <div class="login-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="11" x="3" y="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                </div>
                <h1 class="login-title">ورود امن به پنل</h1>
                <p class="login-subtitle">جهت دسترسی به تنظیمات امنیتی، رمز عبور را وارد کنید</p>
                
                <form method="POST">
                    <input type="hidden" name="csrf_token" value="<?php echo htmlspecialchars($_SESSION['csrf_token']); ?>">
                    <div class="login-input-wrapper">
                        <input type="password" name="password" class="login-input" placeholder="••••••••" required autofocus>
                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="11" x="3" y="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                    </div>
                    <?php if (isset($login_error)): ?>
                        <div class="login-error">
                            <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
                            <?php echo $login_error; ?>
                        </div>
                    <?php endif; ?>
                    <button type="submit" name="login" class="btn-primary" style="width:100%;justify-content:center;">تایید و ورود</button>
                </form>
            </div>
        <?php else: ?>
            <div class="header">
                <div class="header-titles">
                    <h1>مدیریت پیشرفته دسترسی (ACL)</h1>
                    <p>پیکربندی هوشمند لایه‌های امنیتی و سطوح کاربری</p>
                </div>
                <div class="header-actions">
                    <button class="icon-btn" onclick="toggleTheme()" id="themeIcon" title="تغییر پوسته">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/></svg>
                    </button>
                    <a href="?logout=1" class="btn-outline-danger">
                        <span>خروج</span>
                        <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path></svg>
                    </a>
                </div>
            </div>

            <?php if (!$dbMigrated): ?>
            <div class="alert alert-info" style="margin: 0 0 16px;">
                <svg width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                <div>
                    دیتابیس مجوزها هنوز راه‌اندازی نشده — <code>migrations/2026_08_19_permissions_to_database.sql</code>
                    را روی این سرور اجرا کنید. مقادیر نمایش‌داده‌شده در این صفحه فعلاً پیش‌فرض خالی هستند
                    (منطق بررسی مجوز در باقی برنامه هنوز از <code>config/permissions.json</code> استفاده می‌کند و کار می‌کند؛
                    فقط این پنل مدیریت غیرفعال است).
                </div>
            </div>
            <?php endif; ?>

 <!-- Stats Bar -->
            <div class="stats-bar">
                <div class="stat-chip">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                    <span>نقش‌های تعریف‌شده</span>
                    <span class="stat-value"><?php echo count($all_data['roles']); ?></span>
                </div>
                <div class="stat-chip">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                    <span>تنظیمات اختصاصی</span>
                    <span class="stat-value"><?php echo count($all_data['users']); ?></span>
                </div>
                <div class="stat-chip">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="8" height="4" x="8" y="2" rx="1"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><path d="M12 11h4"/><path d="M12 16h4"/><path d="M8 11h.01"/><path d="M8 16h.01"/></svg>
                    <span>دسترسی‌های قابل مدیریت</span>
                    <span class="stat-value"><?php echo count($feature_labels); ?></span>
                </div>
                <div class="stat-chip">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                    <span>آخرین ویرایش</span>
                    <span class="stat-value" style="font-family:monospace;direction:ltr;"><?php
                        $lastUpdatedAt = $dbMigrated ? $permissionRepository->getLastUpdatedAt() : null;
                        echo $lastUpdatedAt ? date('H:i — Y/m/d', strtotime($lastUpdatedAt)) : '—';
                    ?></span>
                </div>
            </div>

            <div class="content-body">
                <div class="tabs">
                    <div class="tab active" onclick="setMode('role', this)">
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                        پارامترهای نقش (Roles)
                    </div>
                    <div class="tab" onclick="setMode('user', this)">
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                        تنظیمات اختصاصی (Users)
                    </div>
                </div>

                <div class="main-content">
 <!-- Form Box -->
                <form method="POST" class="form-card" id="permissionsForm">
                    <input type="hidden" name="csrf_token" value="<?php echo htmlspecialchars($_SESSION['csrf_token']); ?>">
                    <div class="form-card-header">
                        <input type="hidden" name="target_type" id="target_type" value="role">
                    </div>
                    
                    <div class="control-group" id="roleSelectorGroup">
                        <label>انتخاب نقش کاربری:</label>
                        <div class="select-wrapper">
                            <select name="target_role" id="target_role" onchange="loadPermissions()">
                                <option value="admin">مدیر سیستم (Admin)</option>
                                <option value="operator">کاربر عملیات / باسکول (Operator)</option>
                                <option value="verifier">ناظر / بارشمار (Verifier)</option>
                            </select>
                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m6 9 6 6 6-6"/></svg>
                        </div>
                    </div>

                    <div class="control-group" id="userSelectorGroup" style="display: none;">
                        <label>انتخاب حساب کاربری:</label>
                        <div class="searchable-dropdown" id="userDropdown">
                            <div class="sd-input-wrapper" onclick="toggleDropdown()" id="sdInputWrapper">
                                <svg class="sd-search-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
                                <input type="text" id="userSearchInput" class="sd-search-input" placeholder="جستجوی نام یا نام کاربری..." autocomplete="off"
                                    oninput="filterUsers(this.value)"
                                    onclick="event.stopPropagation(); openDropdown()"
                                    onkeydown="handleSearchKey(event)">
                                <span id="sdSelectedLabel" class="sd-placeholder">انتخاب کاربر</span>
                                <button type="button" class="sd-clear-btn" id="sdClearBtn" onclick="clearUserSelection(event)" style="display:none;">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
                                </button>
                                <svg class="sd-chevron" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m6 9 6 6 6-6"/></svg>
                            </div>
                            <div class="sd-dropdown-list" id="sdDropdownList">
                                <div class="sd-empty" id="sdEmpty" style="display:none;">کاربری یافت نشد</div>
                                <?php foreach ($users_list as $u): ?>
                                <div class="sd-option"
                                    data-value="<?php echo htmlspecialchars($u['username']); ?>"
                                    data-role="<?php echo htmlspecialchars($u['userType']); ?>"
                                    data-label="<?php echo htmlspecialchars($u['fullName'] . ' (' . $u['username'] . ')'); ?>"
                                    data-search="<?php echo strtolower(htmlspecialchars($u['fullName'] . ' ' . $u['username'])); ?>"
                                    onclick="selectUser(this)">
                                    <div class="sd-option-main"><?php echo htmlspecialchars($u['fullName']); ?></div>
                                    <div class="sd-option-sub"><?php echo htmlspecialchars($u['username']); ?> &nbsp;·&nbsp; <?php echo htmlspecialchars($u['userType']); ?></div>
                                </div>
                                <?php endforeach; ?>
                            </div>
                        </div>
                        <input type="hidden" name="target_user" id="target_user" value="">
                        <input type="hidden" name="target_name" id="target_name" value="">
                    </div>

                    <div id="inheritedHint" class="alert alert-info" style="display: none;">
                        <svg width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                        <div>
                            کاربر انتخاب شده فاقد تنظیمات اختصاصی است و وضعیت فعلی از نقش <b style="font-family: monospace; font-size: 16px;"><span id="roleNameText"></span></b> به ارث برده شده است. با تغییر موارد زیر، یک پیکربندی مستقل برای این شخص ایجاد می‌گردد.
                        </div>
                    </div>

                    <div class="permissions-grid">
                        <?php foreach ($feature_labels as $key => $data): ?>
                        <label class="perm-card">
                            <div class="perm-info">
                                <div class="perm-icon"><?php echo $data['icon']; ?></div>
                                <span class="perm-label"><?php echo $data['label']; ?></span>
                            </div>
                            <div class="toggle-switch">
                                <input type="checkbox" name="perm_<?php echo $key; ?>" id="perm_<?php echo $key; ?>">
                                <span class="slider"></span>
                            </div>
                        </label>
                        <?php endforeach; ?>
                    </div>

                    <div class="footer-actions">
                        <div id="userActions" style="display: none;">
                            <label class="checkbox-danger">
                                <input type="checkbox" name="delete_user_custom" value="1">
                                <span>حذف پیکربندی اختصاصی و بازگشت به تنظیمات پیش‌فرض نقش</span>
                            </label>
                        </div>
                        <button type="submit" name="save_permissions" class="btn-primary">
                            <svg width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4"></path></svg>
                            اعمال و ذخیره تغییرات
                        </button>
                    </div>
                </form>
            </div>

 <!-- Panel Footer -->
            <div class="panel-footer">
                <span>ATK-Cargo Permission Manager &copy; <?php echo date('Y'); ?></span>
                <div class="panel-footer-badge">
                    <svg width="10" height="10" viewBox="0 0 10 10" fill="currentColor"><circle cx="5" cy="5" r="5"/></svg>
                    V2 — Professional
                </div>
            </div>
        <?php endif; ?>
    </div>
</div>

<script>
 // Toast Notification System
    function showToast(message, type = 'success') {
        const container = document.getElementById('toast-container');
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        
        const icon = type === 'success' 
            ? `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>`
            : `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>`;
            
        toast.innerHTML = `
            <div class="toast-icon">${icon}</div>
            <div>${message}</div>
        `;
        
        container.appendChild(toast);
        
 // Trigger reflow to ensure transition works
        void toast.offsetWidth;
        toast.classList.add('show');
        
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 400);
        }, 3000);
    }

 // Check for PHP generated messages
    <?php if (isset($success_msg)): ?>
        window.addEventListener('DOMContentLoaded', () => showToast("<?php echo addslashes($success_msg); ?>", 'success'));
    <?php endif; ?>
    <?php if (isset($error_msg)): ?>
        window.addEventListener('DOMContentLoaded', () => showToast("<?php echo addslashes($error_msg); ?>", 'error'));
    <?php endif; ?>

 // Theme Management
    const themeIcon = document.getElementById('themeIcon');
    const sunSVG = '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="4"/><path d="M12 2v2"/><path d="M12 20v2"/><path d="m4.93 4.93 1.41 1.41"/><path d="m17.66 17.66 1.41 1.41"/><path d="M2 12h2"/><path d="M20 12h2"/><path d="m6.34 17.66-1.41 1.41"/><path d="m19.07 4.93-1.41 1.41"/></svg>';
    const moonSVG = '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/></svg>';
    
    function applyTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        document.body.setAttribute('data-theme', theme);
        if(themeIcon) themeIcon.innerHTML = theme === 'dark' ? sunSVG : moonSVG;
    }

    function toggleTheme() {
        const cur = document.documentElement.getAttribute('data-theme');
        const next = cur === 'light' ? 'dark' : 'light';
        localStorage.setItem('atk_theme_pref', next);
        applyTheme(next);
    }

    document.addEventListener('DOMContentLoaded', () => {
        const saved = localStorage.getItem('atk_theme_pref') || (window.matchMedia('(prefers-color-scheme:dark)').matches ? 'dark' : 'light');
        applyTheme(saved);
    });

    <?php if ($is_authenticated): ?>
    const allData = <?php echo json_encode($all_data); ?>;
    let currentMode = 'role';

    function setMode(mode, element) {
        currentMode = mode;
        document.getElementById('target_type').value = mode;
        
        document.querySelectorAll('.tab').forEach(b => b.classList.remove('active'));
        if(element) element.classList.add('active');

        document.getElementById('roleSelectorGroup').style.display = mode === 'role' ? 'flex' : 'none';
        document.getElementById('userSelectorGroup').style.display = mode === 'user' ? 'flex' : 'none';
        document.getElementById('userActions').style.display = mode === 'user' ? 'block' : 'none';
        
        loadPermissions();
    }

 // ===== SEARCHABLE DROPDOWN — STATE =====
    let _sdOpen = false;
    let _sdSelectedValue = '';
    let _sdSelectedRole  = '';
    let _sdFocusedIndex  = -1;

    function getVisibleOptions() {
        return Array.from(document.querySelectorAll('#sdDropdownList .sd-option:not([style*="display: none"])')
        ).filter(el => el.style.display !== 'none');
    }

    function openDropdown() {
        _sdOpen = true;
        const wrapper = document.getElementById('userDropdown');
        const list    = document.getElementById('sdDropdownList');
        const input   = document.getElementById('userSearchInput');
        wrapper.classList.add('open');
        list.style.display = 'block';
        input.focus();
        _sdFocusedIndex = -1;
    }

    function closeDropdown() {
        _sdOpen = false;
        const wrapper = document.getElementById('userDropdown');
        const list    = document.getElementById('sdDropdownList');
        wrapper.classList.remove('open');
        list.style.display = 'none';
        document.getElementById('userSearchInput').value = '';
        filterUsers('');
    }

    function toggleDropdown() {
        if (_sdOpen) closeDropdown(); else openDropdown();
    }

    function filterUsers(query) {
        const q = query.trim().toLowerCase();
        const options  = document.querySelectorAll('#sdDropdownList .sd-option');
        let visible = 0;
        options.forEach(opt => {
            const searchable = opt.getAttribute('data-search') || '';
            const match = !q || searchable.includes(q);
            opt.style.display = match ? 'block' : 'none';
            if (match) visible++;
        });
        document.getElementById('sdEmpty').style.display = (visible === 0) ? 'block' : 'none';
        _sdFocusedIndex = -1;
        highlightOption(-1);
    }

    function highlightOption(index) {
        const opts = getVisibleOptions();
        opts.forEach((o, i) => o.classList.toggle('focused', i === index));
        if (index >= 0 && opts[index]) {
            opts[index].scrollIntoView({ block: 'nearest' });
        }
    }

    function handleSearchKey(e) {
        const opts = getVisibleOptions();
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            _sdFocusedIndex = Math.min(_sdFocusedIndex + 1, opts.length - 1);
            highlightOption(_sdFocusedIndex);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            _sdFocusedIndex = Math.max(_sdFocusedIndex - 1, 0);
            highlightOption(_sdFocusedIndex);
        } else if (e.key === 'Enter') {
            e.preventDefault();
            if (_sdFocusedIndex >= 0 && opts[_sdFocusedIndex]) {
                selectUser(opts[_sdFocusedIndex]);
            }
        } else if (e.key === 'Escape') {
            closeDropdown();
        }
    }

    function selectUser(optionEl) {
        _sdSelectedValue = optionEl.getAttribute('data-value');
        _sdSelectedRole  = optionEl.getAttribute('data-role');
        const label = optionEl.getAttribute('data-label');

        document.getElementById('target_user').value  = _sdSelectedValue;
        document.getElementById('target_name').value  = _sdSelectedValue;

        const lbl = document.getElementById('sdSelectedLabel');
        lbl.textContent = label;
        lbl.classList.remove('sd-placeholder');

        document.getElementById('sdClearBtn').style.display = 'flex';
        document.getElementById('userSearchInput').value = '';
        closeDropdown();
        loadPermissions();
    }

    function clearUserSelection(e) {
        e.stopPropagation();
        _sdSelectedValue = '';
        _sdSelectedRole  = '';
        document.getElementById('target_user').value = '';
        document.getElementById('target_name').value = '';
        const lbl = document.getElementById('sdSelectedLabel');
        lbl.textContent = 'انتخاب کاربر';
        lbl.classList.add('sd-placeholder');
        document.getElementById('sdClearBtn').style.display = 'none';
        filterUsers('');
        loadPermissions();
    }

 // Close on outside click
    document.addEventListener('click', (e) => {
        if (!document.getElementById('userDropdown')?.contains(e.target)) {
            if (_sdOpen) closeDropdown();
        }
    });

 // ===== PERMISSION LOADER =====
    function loadPermissions() {
        const type = currentMode;
        let target, perms;
        const hint = document.getElementById('inheritedHint');

        if (type === 'role') {
            target = document.getElementById('target_role').value;
            document.getElementById('target_name').value = target;
            perms = allData?.roles?.[target] || {};
            hint.style.display = 'none';
        } else {
            target = _sdSelectedValue;
            document.getElementById('target_name').value = target;

            if (!target) {
                resetCheckboxes(false);
                hint.style.display = 'none';
                return;
            }

            if (allData?.users?.[target]) {
                perms = allData.users[target];
                hint.style.display = 'none';
            } else {
                perms = allData?.roles?.[_sdSelectedRole] || {};
                hint.style.display = 'flex';
                document.getElementById('roleNameText').innerText = (_sdSelectedRole || '').toUpperCase();
            }
        }
        applyPermissions(perms);
    }

    function applyPermissions(perms) {
        const features = ['initial_info', 'select_info', 'cargo_counter', 'manage_ships', 'manage_users', 'admin_chat', 'edit_cargo', 'delete_cargo', 'view_reports', 'tonnage_warning', 'manage_quotas', 'view_monitoring'];
        features.forEach(f => {
            const el = document.getElementById('perm_' + f);
            if(el) el.checked = !!perms[f];
        });
    }

    function resetCheckboxes(val) {
        document.querySelectorAll('.permissions-grid input[type="checkbox"]').forEach(i => i.checked = val);
    }

    window.onload = () => { if (allData) loadPermissions(); };
    <?php endif; ?>
</script>

</body>
</html>
