<?php
// PHP/src/Core/Csrf.php

declare(strict_types=1);

namespace App\Core;

/**
 * توکن CSRF مشترک برای پنل‌های تحت‌وبِ مبتنی بر نشست (PHP/Lic، و در آینده
 * PermissionManager.php).
 *
 * تا پیش از این هیچ helper مشترکی وجود نداشت: تنها اعتبارسنجی واقعی کل
 * مخزن یک hash_equals دستی در PermissionManager.php بود، و پنل لایسنس یک
 * endpoint جداگانه (Lic/get_csrf_token.php) داشت که توکن تولید می‌کرد اما
 * هیچ‌کجا بررسی نمی‌شد — یعنی محافظت CSRF فقط ظاهری بود.
 *
 * توکن به‌ازای هر نشست یک‌بار ساخته و تا پایان نشست ثابت می‌ماند (per-session
 * نه per-request)، چون per-request با چند تب باز هم‌زمان می‌شکند.
 *
 * پیش‌نیاز: نشست باید از قبل با session_start() آغاز شده باشد.
 */
final class Csrf {
    private const SESSION_KEY = 'csrf_token';

    /**
     * توکن نشست جاری؛ اگر وجود نداشته باشد ساخته می‌شود.
     */
    public static function token(): string {
        if (empty($_SESSION[self::SESSION_KEY]) || !is_string($_SESSION[self::SESSION_KEY])) {
            $_SESSION[self::SESSION_KEY] = bin2hex(random_bytes(32));
        }
        return $_SESSION[self::SESSION_KEY];
    }

    /**
     * مقایسه‌ی زمان‌ثابت توکن ورودی با توکن نشست.
     *
     * ورودی null/آرایه/خالی صریحاً false برمی‌گرداند — hash_equals با آرگومان
     * غیررشته‌ای در PHP 8 خطای TypeError می‌دهد.
     */
    public static function validate(mixed $token): bool {
        if (!is_string($token) || $token === '') {
            return false;
        }
        $expected = $_SESSION[self::SESSION_KEY] ?? null;
        if (!is_string($expected) || $expected === '') {
            return false;
        }
        return hash_equals($expected, $token);
    }

    /**
     * نسخه‌ی JSON: در صورت نامعتبربودن، پاسخ 403 می‌فرستد و اجرا را تمام
     * می‌کند (Response::error خودش exit می‌کند).
     */
    public static function requireValid(mixed $token): void {
        if (!self::validate($token)) {
            Logger::getInstance()->security('CSRF token validation failed for ' . ($_SERVER['REQUEST_URI'] ?? 'unknown'));
            Response::error('توکن امنیتی نامعتبر است. لطفاً صفحه را دوباره بارگذاری کنید.', 403);
        }
    }

    /**
     * حذف توکن — هنگام خروج از حساب، تا توکن نشست قبلی روی نشست جدید
     * قابل استفاده نباشد.
     */
    public static function forget(): void {
        unset($_SESSION[self::SESSION_KEY]);
    }
}
