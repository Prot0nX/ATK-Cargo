<?php
// PHP/PermissionManager.php پنل مدیریت متمرکز نقش‌ها و دسترسی‌های اختصاصی کاربران (V3 — هم‌راستا با زبان طراحی Lic/Realtime_Dashboard).

declare(strict_types=1);

// نمایش خطا در production غیرفعال است تا جزئیات داخلی افشا نشود؛ خطاها همچنان لاگ می‌شوند
error_reporting(E_ALL);
ini_set('display_errors', '0');
ini_set('log_errors', '1');

// کوکی نشست باید قبل از session_start تنظیم شود. عمداً session_name اختصاصی ندارد و از نام
// پیش‌فرض PHP استفاده می‌کند چون file_manager.php همین نشست را می‌خواند (نگاه کنید به کامنت آن‌جا)؛
// برخلاف Lic/Quota_Reports/Monitoring که هرکدام نشست مجزا دارند.
session_set_cookie_params([
    'lifetime' => 0,
    'path' => '/',
    'secure' => !empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off',
    'httponly' => true,
    'samesite' => 'Lax',
]);
session_start();

require_once __DIR__ . '/src/bootstrap.php';

use App\Core\Csrf;
use App\Core\MicroCache;
use App\Services\LoginAttemptLimiter;
use App\Services\PermissionService;
use App\Repositories\PermissionRepository;

const PERM_SESSION_KEY = 'perm_manager_auth';

/** نامی که در LoginAttemptLimiter برای این پنل استفاده می‌شود — رمز این پنل مشترک است، نه هویت فردی. */
const PERM_ACTOR = 'permmgr';

function permmgr_send_page_headers(): void {
    header('Content-Type: text/html; charset=UTF-8');
    header("Content-Security-Policy: default-src 'self'; img-src 'self' data:; style-src 'self'; script-src 'self'; font-src 'self'; form-action 'self'; base-uri 'none'; frame-ancestors 'none'");
    header('X-Content-Type-Options: nosniff');
    header('X-Frame-Options: DENY');
    header('Referrer-Policy: strict-origin-when-cross-origin');
    header('Permissions-Policy: geolocation=(), microphone=(), camera=()');
    header('Strict-Transport-Security: max-age=31536000; includeSubDomains');
    header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
}

function permmgr_is_authenticated(): bool {
    if (empty($_SESSION[PERM_SESSION_KEY])) {
        return false;
    }
    $lastActivity = $_SESSION['last_activity'] ?? 0;
    if (!is_int($lastActivity) || (time() - $lastActivity) > 1800) {
        permmgr_destroy_session();
        return false;
    }
    $_SESSION['last_activity'] = time();
    return true;
}

function permmgr_destroy_session(): void {
    Csrf::forget();
    $_SESSION = [];
    if (ini_get('session.use_cookies')) {
        $params = session_get_cookie_params();
        setcookie(session_name(), '', [
            'expires' => time() - 42000,
            'path' => $params['path'],
            'domain' => $params['domain'],
            'secure' => $params['secure'],
            'httponly' => $params['httponly'],
            'samesite' => $params['samesite'],
        ]);
    }
    session_destroy();
}

// تم انتخابی کاربر از کوکی — سمت سرور خوانده می‌شود تا FOUC هنگام بارگذاری رخ ندهد. @return string 'light' | 'dark' | '' (خالی = پیروی از تنظیم سیستم)
function permmgr_theme(): string {
    $theme = $_COOKIE['permission_manager_theme'] ?? '';
    return in_array($theme, ['light', 'dark'], true) ? $theme : '';
}

function e(?string $value): string {
    return htmlspecialchars((string)$value, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}

permmgr_send_page_headers();

// منبع مجوزها از config/permissions.json به جداول دیتابیس منتقل شده؛ اگر migration هنوز اجرا نشده این پنل خطای صریح می‌دهد ولی بررسی مجوز همچنان به فایل قدیمی fallback می‌کند
$permissionRepository = new PermissionRepository();
$dbMigrated = true;
try {
    $permissionRepository->getAllRolePermissions();
} catch (\Throwable $e) {
    $dbMigrated = false;
}

$ADMIN_PASSWORD_HASH = $_ENV['ADMIN_PASSWORD_HASH'] ?? getenv('ADMIN_PASSWORD_HASH') ?: '';

$is_authenticated = permmgr_is_authenticated();

// هندل لاگین با همان LoginAttemptLimiter مورد استفاده در API برای جلوگیری از brute-force روی رمز ادمین
$loginAttemptLimiter = new LoginAttemptLimiter();
$clientIp = (string)($_SERVER['REMOTE_ADDR'] ?? 'unknown');

if (isset($_POST['login'])) {
    if (!Csrf::validate($_POST['csrf_token'] ?? null)) {
        $login_error = "توکن امنیتی (CSRF) نامعتبر است. لطفاً صفحه را بازنشانی کنید.";
    } elseif ($loginAttemptLimiter->isLocked(PERM_ACTOR, $clientIp)) {
        $login_error = "تعداد تلاش‌های ناموفق بیش از حد مجاز است. لطفاً ۱۵ دقیقه دیگر تلاش کنید.";
    } elseif (empty($ADMIN_PASSWORD_HASH)) {
        $login_error = "خطای پیکربندی: متغیر ADMIN_PASSWORD_HASH در فایل .env تنظیم نشده است.";
    } elseif (password_verify((string)($_POST['password'] ?? ''), $ADMIN_PASSWORD_HASH)) {
        $loginAttemptLimiter->resetAttempts(PERM_ACTOR, $clientIp);
        session_regenerate_id(true);
        $_SESSION[PERM_SESSION_KEY] = true;
        $_SESSION['last_activity'] = time();
        Csrf::token();
        header("Location: PermissionManager.php");
        exit;
    } else {
        $loginAttemptLimiter->registerFailedAttempt(PERM_ACTOR, $clientIp);
        $login_error = "رمز عبور اشتباه است!";
    }
}

// خروج فقط با POST و توکن CSRF معتبر پذیرفته می‌شود
if ($is_authenticated && $_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['logout'])) {
    if (Csrf::validate($_POST['csrf_token'] ?? null)) {
        permmgr_destroy_session();
    }
    header("Location: PermissionManager.php");
    exit;
}

// $all_data همان ساختار قبلی (roles/users) را حفظ می‌کند تا تمپلیت پایین بدون تغییر کار کند
$all_data = ['roles' => ['admin' => [], 'operator' => [], 'verifier' => []], 'users' => []];
if ($dbMigrated) {
    $all_data = [
        'roles' => $permissionRepository->getAllRolePermissions(),
        'users' => $permissionRepository->getAllUserPermissions(),
    ];
}

$features = [
    'initial_info', 'select_info', 'cargo_counter', 'manage_ships',
    'manage_users', 'admin_chat', 'edit_cargo', 'delete_cargo',
    'view_reports', 'tonnage_warning', 'manage_quotas',
    'view_monitoring'
];

// هندل کردن ذخیره‌سازی
if ($is_authenticated && isset($_POST['save_permissions'])) {
    if (!$dbMigrated) {
        $error_msg = "دیتابیس مجوزها هنوز migrate نشده است. ابتدا migrations/2026_08_19_permissions_to_database.sql را روی این سرور اجرا کنید.";
    } elseif (!Csrf::validate($_POST['csrf_token'] ?? null)) {
        $error_msg = "توکن امنیتی (CSRF) نامعتبر است. لطفاً صفحه را بازنشانی کنید.";
    } else {
        $type = $_POST['target_type']; // 'role' or 'user'
        $target_name = $_POST['target_name']; // role name or username

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
    'initial_info' => ['label' => 'تعریف کشتی (Initial Info)', 'icon' => 'feat-initial-info'],
    'select_info' => ['label' => 'ثبت حواله (Select Info)', 'icon' => 'feat-select-info'],
    'cargo_counter' => ['label' => 'پایش بارگیری (Counter)', 'icon' => 'feat-cargo-counter'],
    'manage_ships' => ['label' => 'گزارشات و کشتی‌ها', 'icon' => 'feat-manage-ships'],
    'manage_users' => ['label' => 'مدیریت کاربران', 'icon' => 'roles'],
    'admin_chat' => ['label' => 'ارتباطات (چت ادمین)', 'icon' => 'feat-admin-chat'],
    'edit_cargo' => ['label' => 'ویرایش حواله', 'icon' => 'edit'],
    'delete_cargo' => ['label' => 'حذف حواله', 'icon' => 'trash'],
    'view_reports' => ['label' => 'مشاهده آمار تحلیلی', 'icon' => 'feat-view-reports'],
    'tonnage_warning' => ['label' => 'هشدار تناژ سیستمی', 'icon' => 'feat-tonnage-warning'],
    'manage_quotas' => ['label' => 'مدیریت کوتاژها', 'icon' => 'feat-manage-quotas'],
    // دسترسی مشترک رویدادهای مانیتورینگ سلامت و امنیت سیستم
    'view_monitoring' => ['label' => 'مانیتورینگ (رویدادهای سلامت و امنیت)', 'icon' => 'feat-view-monitoring'],
];

$csrfToken = Csrf::token();
$theme = permmgr_theme();
?>
<!DOCTYPE html>
<html lang="fa" dir="rtl"<?= $theme !== '' ? ' data-theme="' . e($theme) . '"' : '' ?>>

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>مدیریت پیشرفته دسترسی — ATK Cargo</title>
    <meta name="description" content="پنل مدیریت متمرکز نقش‌ها و دسترسی‌های کاربران سیستم ATK-Cargo">
    <link rel="preload" href="assets/ui/fonts/Vazirmatn-Regular.woff2" as="font" type="font/woff2" crossorigin>
    <link rel="stylesheet" href="assets/ui/core.css?v=<?= filemtime(__DIR__ . '/assets/ui/core.css') ?>">
    <link rel="stylesheet" href="assets/permission-manager.css?v=<?= filemtime(__DIR__ . '/assets/permission-manager.css') ?>">
</head>

<body<?= !$is_authenticated ? ' class="page-centered"' : '' ?>>
<svg xmlns="http://www.w3.org/2000/svg" class="icon-sprite" aria-hidden="true" focusable="false">
    <symbol id="shield" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <path d="M12 3l7 4v5c0 5-3.5 8-7 9-3.5-1-7-4-7-9V7l7-4Z" />
        <path d="m9 12 2 2 4-4" />
    </symbol>
    <symbol id="moon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <path d="M20 14.5A8.5 8.5 0 0 1 9.5 4a8.5 8.5 0 1 0 10.5 10.5Z" />
    </symbol>
    <symbol id="sun" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="4" />
        <path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" />
    </symbol>
    <symbol id="logout" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <path d="M14 4h4a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1h-4" />
        <path d="M9 16l-4-4 4-4" />
        <path d="M5 12h9" />
    </symbol>
    <symbol id="eye" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7-10-7-10-7Z" />
        <circle cx="12" cy="12" r="3" />
    </symbol>
    <symbol id="eye-off" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <path d="M10.6 5.2A9.9 9.9 0 0 1 12 5c6.4 0 10 7 10 7a17.6 17.6 0 0 1-3.4 4.2" />
        <path d="M6.2 6.6A17.4 17.4 0 0 0 2 12s3.6 7 10 7a9.8 9.8 0 0 0 4.3-.95" />
        <path d="M9.9 9.9a3 3 0 0 0 4.2 4.2" />
        <path d="m3 3 18 18" />
    </symbol>
    <symbol id="alert-circle" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10" />
        <line x1="15" y1="9" x2="9" y2="15" />
        <line x1="9" y1="9" x2="15" y2="15" />
    </symbol>
    <symbol id="info" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10" />
        <path d="M12 16v-4M12 8h.01" />
    </symbol>
    <symbol id="search" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="11" cy="11" r="7" />
        <path d="m20 20-3.5-3.5" />
    </symbol>
    <symbol id="close" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <path d="M18 6 6 18M6 6l12 12" />
    </symbol>
    <symbol id="chevron-down" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <path d="m6 9 6 6 6-6" />
    </symbol>
    <symbol id="save" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <path d="M8 7H5a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3m-1 4-3 3m0 0-3-3m3 3V4" />
    </symbol>
    <symbol id="edit" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z" />
        <path d="m15 5 4 4" />
    </symbol>
    <symbol id="trash" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <path d="M3 6h18" />
        <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6" />
        <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2" />
        <line x1="10" y1="11" x2="10" y2="17" />
        <line x1="14" y1="11" x2="14" y2="17" />
    </symbol>
    <symbol id="roles" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
        <circle cx="9" cy="7" r="4" />
        <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
        <path d="M16 3.13a4 4 0 0 1 0 7.75" />
    </symbol>
    <symbol id="user-single" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
        <circle cx="12" cy="7" r="4" />
    </symbol>
    <symbol id="clock" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10" />
        <polyline points="12 6 12 12 16 14" />
    </symbol>
    <symbol id="feat-initial-info" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <path d="M2 21c.6.5 1.2 1 2.5 1 2.5 0 3.2-1.2 5.5-1.2 2.3 0 3 1.2 5.5 1.2 1.3 0 1.9-.5 2.5-1M19.38 20A11.6 11.6 0 0 0 21 14l-9-4-9 4c0 2.9.94 5.34 2.81 7.76" />
    </symbol>
    <symbol id="feat-select-info" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z" />
        <polyline points="14 2 14 8 20 8" />
        <line x1="16" y1="13" x2="8" y2="13" />
        <line x1="16" y1="17" x2="8" y2="17" />
        <line x1="10" y1="9" x2="8" y2="9" />
    </symbol>
    <symbol id="feat-cargo-counter" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <rect width="16" height="20" x="4" y="2" rx="2" />
        <line x1="8" x2="16" y1="6" y2="6" />
        <line x1="16" x2="16.01" y1="10" y2="10" />
        <line x1="12" x2="12.01" y1="10" y2="10" />
        <line x1="8" x2="8.01" y1="10" y2="10" />
        <line x1="16" x2="16.01" y1="14" y2="14" />
        <line x1="12" x2="12.01" y1="14" y2="14" />
        <line x1="8" x2="8.01" y1="14" y2="14" />
        <line x1="16" x2="16.01" y1="18" y2="18" />
        <line x1="12" x2="12.01" y1="18" y2="18" />
        <line x1="8" x2="8.01" y1="18" y2="18" />
    </symbol>
    <symbol id="feat-manage-ships" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <path d="M3 3v18h18" />
        <path d="m19 9-5 5-4-4-3 3" />
    </symbol>
    <symbol id="feat-admin-chat" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <path d="M7.9 20A9 9 0 1 0 4 16.1L2 22Z" />
    </symbol>
    <symbol id="feat-view-reports" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21.21 15.89A10 10 0 1 1 8 2.83" />
        <path d="M22 12A10 10 0 0 0 12 2v10z" />
    </symbol>
    <symbol id="feat-tonnage-warning" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z" />
        <path d="M12 9v4" />
        <path d="M12 17h.01" />
    </symbol>
    <symbol id="feat-manage-quotas" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <rect width="8" height="4" x="8" y="2" rx="1" ry="1" />
        <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2" />
        <path d="M12 11h4" />
        <path d="M12 16h4" />
        <path d="M8 11h.01" />
        <path d="M8 16h.01" />
    </symbol>
    <symbol id="feat-view-monitoring" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">
        <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
    </symbol>
</svg>

<?php if (!$is_authenticated): ?>
    <main class="auth-card">
        <div class="auth-brand">
            <svg class="icon icon-lg" aria-hidden="true"><use href="#shield"></use></svg>
            <div>
                <h1>مدیریت پیشرفته دسترسی</h1>
                <p class="muted">ATK Cargo</p>
            </div>
        </div>

        <?php if (isset($login_error)): ?>
            <p class="alert alert-error" role="alert"><?= e($login_error) ?></p>
        <?php endif; ?>

        <form method="POST" autocomplete="off">
            <input type="hidden" name="csrf_token" value="<?= e($csrfToken) ?>">

            <label class="field">
                <span class="field-label">رمز عبور</span>
                <span class="field-input-group">
                    <input type="password" name="password" id="password" class="login-input" required autofocus
                        autocomplete="current-password" placeholder="رمز عبور پنل">
                    <button type="button" class="btn-icon" id="togglePassword" aria-label="نمایش رمز عبور">
                        <svg class="icon" aria-hidden="true"><use href="#eye"></use></svg>
                    </button>
                </span>
            </label>

            <button type="submit" name="login" class="btn btn-primary btn-block">تایید و ورود</button>
        </form>
    </main>
<?php else: ?>
    <header class="topbar">
        <div class="topbar-brand">
            <svg class="icon" aria-hidden="true"><use href="#shield"></use></svg>
            <span>مدیریت پیشرفته دسترسی (ACL)</span>
        </div>

        <div class="topbar-actions">
            <button type="button" class="btn-icon" id="themeToggle" aria-label="تغییر پوسته">
                <svg class="icon icon-light-only" aria-hidden="true"><use href="#moon"></use></svg>
                <svg class="icon icon-dark-only" aria-hidden="true"><use href="#sun"></use></svg>
            </button>
            <form method="POST" class="inline-form">
                <input type="hidden" name="csrf_token" value="<?= e($csrfToken) ?>">
                <input type="hidden" name="logout" value="1">
                <button type="submit" class="btn btn-ghost">
                    <svg class="icon" aria-hidden="true"><use href="#logout"></use></svg>
                    <span>خروج</span>
                </button>
            </form>
        </div>
    </header>

    <main class="container">
        <?php if (!$dbMigrated): ?>
            <p class="alert alert-info">
                <svg class="icon" aria-hidden="true"><use href="#info"></use></svg>
                دیتابیس مجوزها هنوز راه‌اندازی نشده — <code>migrations/2026_08_19_permissions_to_database.sql</code>
                را روی این سرور اجرا کنید. مقادیر نمایش‌داده‌شده در این صفحه فعلاً پیش‌فرض خالی هستند
                (منطق بررسی مجوز در باقی برنامه هنوز از <code>config/permissions.json</code> استفاده می‌کند و کار می‌کند؛
                فقط این پنل مدیریت غیرفعال است).
            </p>
        <?php endif; ?>

        <section class="stats" aria-label="آمار دسترسی‌ها">
            <article class="stat">
                <p class="stat-label">نقش‌های تعریف‌شده</p>
                <p class="stat-value"><?= count($all_data['roles']) ?></p>
            </article>
            <article class="stat">
                <p class="stat-label">تنظیمات اختصاصی</p>
                <p class="stat-value"><?= count($all_data['users']) ?></p>
            </article>
            <article class="stat">
                <p class="stat-label">دسترسی‌های قابل مدیریت</p>
                <p class="stat-value"><?= count($feature_labels) ?></p>
            </article>
            <article class="stat">
                <p class="stat-label">آخرین ویرایش</p>
                <p class="stat-value stat-value-date"><?php
                    $lastUpdatedAt = $dbMigrated ? $permissionRepository->getLastUpdatedAt() : null;
                    echo $lastUpdatedAt ? e(date('H:i — Y/m/d', strtotime($lastUpdatedAt))) : '—';
                ?></p>
            </article>
        </section>

        <section class="toolbar">
            <div class="toolbar-filters" role="group" aria-label="حالت ویرایش">
                <button type="button" class="chip is-selected" data-mode="role" id="modeRoleBtn">
                    <svg class="icon" aria-hidden="true"><use href="#roles"></use></svg>
                    پارامترهای نقش
                </button>
                <button type="button" class="chip" data-mode="user" id="modeUserBtn">
                    <svg class="icon" aria-hidden="true"><use href="#user-single"></use></svg>
                    تنظیمات اختصاصی
                </button>
            </div>
        </section>

        <form method="POST" class="panel" id="permissionsForm">
            <input type="hidden" name="csrf_token" value="<?= e($csrfToken) ?>">
            <input type="hidden" name="target_type" id="target_type" value="role">

            <div class="control-group" id="roleSelectorGroup">
                <label class="field">
                    <span class="field-label">انتخاب نقش کاربری</span>
                    <select name="target_role" id="target_role">
                        <option value="admin">مدیر سیستم (Admin)</option>
                        <option value="operator">کاربر عملیات / باسکول (Operator)</option>
                        <option value="verifier">ناظر / بارشمار (Verifier)</option>
                    </select>
                </label>
            </div>

            <div class="control-group is-hidden" id="userSelectorGroup">
                <label class="field">
                    <span class="field-label">انتخاب حساب کاربری</span>
                    <div class="searchable-dropdown" id="userDropdown">
                        <div class="sd-input-wrapper" id="sdInputWrapper">
                            <svg class="icon sd-search-icon" aria-hidden="true"><use href="#search"></use></svg>
                            <input type="text" id="userSearchInput" class="sd-search-input" placeholder="جستجوی نام یا نام کاربری…" autocomplete="off">
                            <span id="sdSelectedLabel" class="sd-placeholder">انتخاب کاربر</span>
                            <button type="button" class="btn-icon sd-clear-btn is-hidden" id="sdClearBtn" aria-label="پاک‌کردن انتخاب">
                                <svg class="icon" aria-hidden="true"><use href="#close"></use></svg>
                            </button>
                            <svg class="icon sd-chevron" aria-hidden="true"><use href="#chevron-down"></use></svg>
                        </div>
                        <div class="sd-dropdown-list" id="sdDropdownList">
                            <div class="sd-empty is-hidden" id="sdEmpty">کاربری یافت نشد</div>
                            <?php foreach ($users_list as $u): ?>
                                <div class="sd-option"
                                    data-value="<?= e($u['username']) ?>"
                                    data-role="<?= e($u['userType']) ?>"
                                    data-label="<?= e($u['fullName'] . ' (' . $u['username'] . ')') ?>"
                                    data-search="<?= strtolower(e($u['fullName'] . ' ' . $u['username'])) ?>">
                                    <div class="sd-option-main"><?= e($u['fullName']) ?></div>
                                    <div class="sd-option-sub"><?= e($u['username']) ?> &nbsp;·&nbsp; <?= e($u['userType']) ?></div>
                                </div>
                            <?php endforeach; ?>
                        </div>
                    </div>
                </label>
                <input type="hidden" name="target_user" id="target_user" value="">
                <input type="hidden" name="target_name" id="target_name" value="">
            </div>

            <p class="alert alert-info is-hidden" id="inheritedHint">
                <svg class="icon" aria-hidden="true"><use href="#info"></use></svg>
                کاربر انتخاب شده فاقد تنظیمات اختصاصی است و وضعیت فعلی از نقش
                <b class="mono" id="roleNameText"></b> به ارث برده شده است. با تغییر موارد زیر، یک پیکربندی مستقل برای این شخص ایجاد می‌گردد.
            </p>

            <div class="permissions-grid">
                <?php foreach ($feature_labels as $key => $data): ?>
                    <label class="perm-card">
                        <span class="perm-info">
                            <svg class="icon perm-icon" aria-hidden="true"><use href="#<?= e($data['icon']) ?>"></use></svg>
                            <span class="perm-label"><?= e($data['label']) ?></span>
                        </span>
                        <span class="toggle-switch">
                            <input type="checkbox" name="perm_<?= e($key) ?>" id="perm_<?= e($key) ?>">
                            <span class="slider"></span>
                        </span>
                    </label>
                <?php endforeach; ?>
            </div>

            <footer class="footer-actions">
                <label class="field-check field-check-danger is-hidden" id="userActions">
                    <input type="checkbox" name="delete_user_custom" value="1">
                    <span>حذف پیکربندی اختصاصی و بازگشت به تنظیمات پیش‌فرض نقش</span>
                </label>
                <button type="submit" name="save_permissions" class="btn btn-primary">
                    <svg class="icon" aria-hidden="true"><use href="#save"></use></svg>
                    اعمال و ذخیره تغییرات
                </button>
            </footer>
        </form>
    </main>
<?php endif; ?>

<div class="toasts" id="toasts" aria-live="polite" aria-atomic="false"></div>

<?php
// داده‌ی سمت سرور از طریق data-attribute به جاوااسکریپت خارجی منتقل می‌شود، نه یک <script> درون‌خطی —
// CSP این صفحه عمداً script-src 'self' است و اجرای اسکریپت درون‌خطی را مسدود می‌کند.
$permToast = isset($success_msg) ? ['message' => $success_msg, 'type' => 'success']
    : (isset($error_msg) ? ['message' => $error_msg, 'type' => 'error'] : null);
?>
<div id="permApp" class="is-hidden"
    data-perm-data="<?= e(json_encode($is_authenticated ? $all_data : ['roles' => [], 'users' => []])) ?>"
    data-perm-toast="<?= e(json_encode($permToast)) ?>"></div>
<script src="assets/permission-manager.js?v=<?= filemtime(__DIR__ . '/assets/permission-manager.js') ?>" defer></script>
</body>

</html>
