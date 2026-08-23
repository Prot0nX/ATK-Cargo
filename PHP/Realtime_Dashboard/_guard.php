<?php
// PHP/Realtime Dashboard/_guard.php — پریمبل احراز هویت داشبورد گزارش لحظه‌ای؛ عمداً روی همان نشست پنل گزارش کوتاژ (ATKQR) سوار می‌شود، نه رمز/نشست جدید.

declare(strict_types=1);

use App\Core\Config;
use App\Core\Csrf;
use App\Core\Response;

// همان نام نشست Quota_Reports — عمدی: ورود به یکی از این دو پنل، دسترسی به هر دو را باز می‌کند.
session_name('ATKQR');
session_set_cookie_params([
    'lifetime' => 0,
    'path'     => '/',
    'secure'   => !empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off',
    'httponly' => true,
    'samesite' => 'Lax',
]);
session_start();

require_once __DIR__ . '/../src/bootstrap.php';

// همان کلید نشستی که Quota_Reports/_guard.php تنظیم می‌کند — عمداً بازتعریف نشده، فقط همان مقدار.
const RTD_SESSION_KEY = 'quota_reports_admin_auth';

/** نامی که در لاگ‌ها به‌عنوان عامل درخواست‌های این پنل ثبت می‌شود (نشست مشترک است، هویت فردی نیست). */
const RTD_ACTOR = 'realtime_dashboard_viewer';

/** مسیر صفحه‌ی ورود مشترک — این پنل فرم ورود جداگانه ندارد. */
const RTD_LOGIN_URL = '../Quota_Reports/login.php';

/** مسیر همین صفحه (از دید Quota_Reports/login.php و logout.php) برای بازگشت بعد از ورود/خروج. */
const RTD_RETURN_URL = '../Realtime_Dashboard/index.php';

/** آدرس ورود مشترک، به‌همراه پارامتر بازگشت، تا بعد از ورود دوباره به همین پنل برگردیم — نه به Quota_Reports. */
function rtd_login_url(): string {
    return RTD_LOGIN_URL . '?return=' . urlencode(RTD_RETURN_URL);
}

function rtd_send_page_headers(): void {
    header('Content-Type: text/html; charset=UTF-8');
    header("Content-Security-Policy: default-src 'self'; img-src 'self' data:; style-src 'self'; script-src 'self'; font-src 'self'; form-action 'self'; base-uri 'none'; frame-ancestors 'none'");
    header('X-Content-Type-Options: nosniff');
    header('X-Frame-Options: DENY');
    header('Referrer-Policy: strict-origin-when-cross-origin');
    header('Permissions-Policy: geolocation=(), microphone=(), camera=()');
    header('Strict-Transport-Security: max-age=31536000; includeSubDomains');
    header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
}

function rtd_idle_timeout(): int {
    // همان کلید کانفیگ Quota_Reports — سیاست انقضای نشست برای هر دو پنل یکسان است چون خودِ نشست یکی است.
    $timeout = (int)Config::getInstance()->get('quota_reports_session_idle_timeout', 1800);
    return $timeout > 0 ? $timeout : 1800;
}

function rtd_is_authenticated(): bool {
    if (empty($_SESSION[RTD_SESSION_KEY])) {
        return false;
    }

    $lastActivity = $_SESSION['last_activity'] ?? 0;
    if (!is_int($lastActivity) || (time() - $lastActivity) > rtd_idle_timeout()) {
        rtd_destroy_session();
        return false;
    }

    $_SESSION['last_activity'] = time();
    return true;
}

function rtd_destroy_session(): void {
    Csrf::forget();
    $_SESSION = [];
    if (ini_get('session.use_cookies')) {
        $params = session_get_cookie_params();
        setcookie(session_name(), '', [
            'expires'  => time() - 42000,
            'path'     => $params['path'],
            'domain'   => $params['domain'],
            'secure'   => $params['secure'],
            'httponly' => $params['httponly'],
            'samesite' => $params['samesite'],
        ]);
    }
    session_destroy();
}

function rtd_require_auth_page(): void {
    if (!rtd_is_authenticated()) {
        header('Location: ' . rtd_login_url());
        exit;
    }
}

function rtd_require_auth_json(): void {
    if (!rtd_is_authenticated()) {
        Response::json([
            'success' => false,
            'code'    => 'session_invalid',
            'message' => 'نشست شما منقضی شده است. لطفاً دوباره وارد شوید.',
        ], 401);
    }
}

// تم انتخابی کاربر — کوکی جداگانه از Quota_Reports چون فقط نشست/رمز مشترک است، نه ترجیحات نمایشی.
// @return string 'light' | 'dark' | '' (خالی = پیروی از تنظیم سیستم)
function rtd_theme(): string {
    $theme = $_COOKIE['realtime_dashboard_theme'] ?? '';
    return in_array($theme, ['light', 'dark'], true) ? $theme : '';
}

function e(?string $value): string {
    return htmlspecialchars((string)$value, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}
