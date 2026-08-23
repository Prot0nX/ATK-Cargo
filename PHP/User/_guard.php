<?php
// PHP/User/_guard.php — پریمبل مشترک پنل مدیریت کاربران آنلاین: پیکربندی نشست، بارگذاری bootstrap و گیت احراز هویت.

declare(strict_types=1);

use App\Core\Config;
use App\Core\Csrf;
use App\Core\Response;

// نام نشست جداگانه تا کوکی این پنل با PermissionManager/بقیه‌ی پنل‌ها تداخل نکند و خروج از یکی دیگری را نبندد.
session_name('ATKOU');
session_set_cookie_params([
    'lifetime' => 0,
    'path'     => '/',
    'secure'   => !empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off',
    'httponly' => true,
    'samesite' => 'Lax',
]);
session_start();

require_once __DIR__ . '/../src/bootstrap.php';

const OU_SESSION_KEY = 'online_users_admin_auth';

/** نامی که در audit_log/لاگ‌ها به‌عنوان عامل عملیات ثبت می‌شود — رمز این پنل مشترک است، نه هویت فردی. */
const OU_ACTOR = 'online_users_admin';

// هدرهای امنیتی صفحات HTML پنل؛ چون همه‌ی منابع محلی‌اند (بدون Tailwind/Font Awesome/فونت CDN) CSP می‌تواند کاملاً سخت‌گیرانه باشد.
function ou_send_page_headers(): void {
    header('Content-Type: text/html; charset=UTF-8');
    header("Content-Security-Policy: default-src 'self'; img-src 'self' data:; style-src 'self'; script-src 'self'; font-src 'self'; form-action 'self'; base-uri 'none'; frame-ancestors 'none'");
    header('X-Content-Type-Options: nosniff');
    header('X-Frame-Options: DENY');
    header('Referrer-Policy: strict-origin-when-cross-origin');
    header('Permissions-Policy: geolocation=(), microphone=(), camera=()');
    header('Strict-Transport-Security: max-age=31536000; includeSubDomains');
    header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
}

function ou_idle_timeout(): int {
    $timeout = (int)Config::getInstance()->get('online_users_session_idle_timeout', 1800);
    return $timeout > 0 ? $timeout : 1800;
}

function ou_is_authenticated(): bool {
    if (empty($_SESSION[OU_SESSION_KEY])) {
        return false;
    }

    $lastActivity = $_SESSION['last_activity'] ?? 0;
    if (!is_int($lastActivity) || (time() - $lastActivity) > ou_idle_timeout()) {
        ou_destroy_session();
        return false;
    }

    $_SESSION['last_activity'] = time();
    return true;
}

// پاک‌سازی کامل نشست — هم داده‌ها، هم کوکی سمت مرورگر.
function ou_destroy_session(): void {
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

// گیت صفحات HTML: کاربر احراز‌نشده به login هدایت می‌شود و هرگز بدنه‌ی صفحه را نمی‌بیند.
function ou_require_auth_page(): void {
    if (!ou_is_authenticated()) {
        header('Location: login.php');
        exit;
    }
}

// گیت endpointهای JSON: پاسخ ۴۰۱ با کد مشخص تا فرانت‌اند بتواند کاربر را به صفحه‌ی ورود بفرستد.
function ou_require_auth_json(): void {
    if (!ou_is_authenticated()) {
        Response::json([
            'success' => false,
            'code'    => 'session_invalid',
            'message' => 'نشست شما منقضی شده است. لطفاً دوباره وارد شوید.',
        ], 401);
    }
}

// تم انتخابی کاربر از کوکی — سمت سرور خوانده می‌شود تا FOUC هنگام بارگذاری رخ ندهد.
// @return string 'light' | 'dark' | '' (خالی = پیروی از تنظیم سیستم)
function ou_theme(): string {
    $theme = $_COOKIE['online_users_theme'] ?? '';
    return in_array($theme, ['light', 'dark'], true) ? $theme : '';
}

// کوتاه‌نویسی escape خروجی HTML.
function e(?string $value): string {
    return htmlspecialchars((string)$value, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}

// اعتبارسنجی مسیر بازگشت بعد از ورود/خروج — فقط index.php همین پنل یا index.php یک پنل خواهرِ
// هم‌سطح (../SomeDir/index.php) مجاز است؛ هر چیز دیگر رد می‌شود تا این پارامتر مسیر open redirect نشود.
function ou_sanitize_return(?string $raw): ?string {
    if ($raw === null || $raw === '') {
        return null;
    }
    if ($raw === 'index.php' || preg_match('#^\.\./[A-Za-z0-9_]+/index\.php$#', $raw) === 1) {
        return $raw;
    }
    return null;
}
