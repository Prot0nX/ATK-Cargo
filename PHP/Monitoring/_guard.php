<?php
// PHP/Monitoring/_guard.php — پریمبل مشترک داشبورد مانیتورینگ: پیکربندی نشست، بارگذاری bootstrap و گیت احراز هویت.
//
// دقیقاً هم‌الگو با PHP/Lic/_guard.php (DEEP_CODE_AUDIT.md فاز۳ #۳۲ فاز ب) —
// یک رمز مشترک مستقل، نه ورود با نام‌کاربری/رمز واقعی اپ. دلیل: سیستم نشست
// اپ (SessionService) هر کاربر را فقط روی یک دستگاه هم‌زمان مجاز می‌داند؛
// ورود از مرورگر با همان نام‌کاربری یک ادمین، نشست موبایل او را می‌شکست
// (دقیقاً سناریویی که این داشبورد برای رصدش ساخته شده: زمانی که یک ادمین
// در حال کار با اپ است). API مانیتورینگ (`api/v2/monitoring/*`, فاز الف) از
// این پنل استفاده نمی‌شود؛ این پنل مستقیماً MonitoringController را in-process
// صدا می‌زند، دقیقاً مثل الگوی Lic/api.php با LicenseAdminService.

declare(strict_types=1);

use App\Core\Config;
use App\Core\Csrf;
use App\Core\Response;

// نام نشست جداگانه تا کوکی این پنل با PermissionManager/Lic تداخل نکند و خروج از یکی دیگری را نبندد.
session_name('ATKMON');
session_set_cookie_params([
    'lifetime' => 0,
    'path'     => '/',
    'secure'   => !empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off',
    'httponly' => true,
    'samesite' => 'Lax',
]);
session_start();

require_once __DIR__ . '/../src/bootstrap.php';

const MON_SESSION_KEY = 'monitoring_admin_auth';

// نامی که در acknowledged_by ثبت می‌شود — رمز این پنل مشترک است، نه هویت فردی
const MON_ACTOR = 'monitoring_admin';

function mon_send_page_headers(): void {
    header('Content-Type: text/html; charset=UTF-8');
    header("Content-Security-Policy: default-src 'self'; img-src 'self' data:; style-src 'self'; script-src 'self'; font-src 'self'; form-action 'self'; base-uri 'none'; frame-ancestors 'none'");
    header('X-Content-Type-Options: nosniff');
    header('X-Frame-Options: DENY');
    header('Referrer-Policy: strict-origin-when-cross-origin');
    header('Permissions-Policy: geolocation=(), microphone=(), camera=()');
    header('Strict-Transport-Security: max-age=31536000; includeSubDomains');
    header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
}

function mon_idle_timeout(): int {
    $timeout = (int)Config::getInstance()->get('monitoring_session_idle_timeout', 1800);
    return $timeout > 0 ? $timeout : 1800;
}

function mon_is_authenticated(): bool {
    if (empty($_SESSION[MON_SESSION_KEY])) {
        return false;
    }

    $lastActivity = $_SESSION['last_activity'] ?? 0;
    if (!is_int($lastActivity) || (time() - $lastActivity) > mon_idle_timeout()) {
        mon_destroy_session();
        return false;
    }

    $_SESSION['last_activity'] = time();
    return true;
}

function mon_destroy_session(): void {
    Csrf::forget();
    $_SESSION = [];
    if (ini_get('session.use_cookies')) {
        $params = session_get_cookie_params();
        $sessionName = session_name();
        /** @var 'Lax'|'lax'|'None'|'none'|'Strict'|'strict' $sameSite */
        $sameSite = in_array($params['samesite'], ['Lax', 'lax', 'None', 'none', 'Strict', 'strict'], true)
            ? $params['samesite']
            : 'Lax';
        setcookie($sessionName !== false ? $sessionName : 'ATKMON', '', [
            'expires'  => time() - 42000,
            'path'     => $params['path'],
            'domain'   => $params['domain'],
            'secure'   => $params['secure'],
            'httponly' => $params['httponly'],
            'samesite' => $sameSite,
        ]);
    }
    session_destroy();
}

function mon_require_auth_page(): void {
    if (!mon_is_authenticated()) {
        header('Location: login.php');
        exit;
    }
}

function mon_require_auth_json(): void {
    if (!mon_is_authenticated()) {
        Response::json([
            'success' => false,
            'code'    => 'session_invalid',
            'message' => 'نشست شما منقضی شده است. لطفاً دوباره وارد شوید.',
        ], 401);
    }
}

// تم انتخابی کاربر از کوکی — سمت سرور خوانده می‌شود تا FOUC هنگام بارگذاری رخ ندهد.
// @return string 'light' | 'dark' | '' (خالی = پیروی از تنظیم سیستم)
function mon_theme(): string {
    $theme = $_COOKIE['monitoring_theme'] ?? '';
    return in_array($theme, ['light', 'dark'], true) ? $theme : '';
}

function e(?string $value): string {
    return htmlspecialchars((string)$value, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}
