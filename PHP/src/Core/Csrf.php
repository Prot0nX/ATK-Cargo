<?php
// PHP/src/Core/Csrf.php

declare(strict_types=1);

namespace App\Core;

// توکن CSRF مشترک برای پنل‌های تحت‌وب مبتنی بر نشست؛ پیش‌نیاز: session_start قبلاً اجرا شده باشد
final class Csrf {
    private const SESSION_KEY = 'csrf_token';

 // توکن نشست جاری؛ اگر وجود نداشته باشد ساخته می‌شود
    public static function token(): string {
        if (empty($_SESSION[self::SESSION_KEY]) || !is_string($_SESSION[self::SESSION_KEY])) {
            $_SESSION[self::SESSION_KEY] = bin2hex(random_bytes(32));
        }
        return $_SESSION[self::SESSION_KEY];
    }

 // مقایسه‌ی زمان‌ثابت توکن ورودی با توکن نشست؛ ورودی نامعتبر صریحاً false برمی‌گرداند
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

 // نسخه‌ی JSON: در صورت نامعتبربودن، پاسخ 403 می‌فرستد و اجرا را متوقف می‌کند
    public static function requireValid(mixed $token): void {
        if (!self::validate($token)) {
            Logger::getInstance()->security('CSRF token validation failed for ' . ($_SERVER['REQUEST_URI'] ?? 'unknown'));
            Response::error('توکن امنیتی نامعتبر است. لطفاً صفحه را دوباره بارگذاری کنید.', 403);
        }
    }

 // حذف توکن هنگام خروج از حساب تا روی نشست جدید قابل استفاده نباشد
    public static function forget(): void {
        unset($_SESSION[self::SESSION_KEY]);
    }
}
