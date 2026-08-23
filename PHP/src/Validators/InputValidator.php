<?php
// PHP/src/Validators/InputValidator.php

declare(strict_types=1);

namespace App\Validators;

use App\Exceptions\ApiException;

class InputValidator {
 // پاک‌سازی رشته ورودی
    public static function sanitize(string $value): string {
        return htmlspecialchars(strip_tags(trim($value)), ENT_QUOTES, 'UTF-8');
    }

 // بررسی وجود فیلدهای الزامی در آرایه داده‌ها
    public static function validateRequired(array $data, array $requiredFields): void {
        $missing = [];
        foreach ($requiredFields as $field) {
            if (!isset($data[$field]) || (is_string($data[$field]) && trim($data[$field]) === '')) {
                $missing[] = $field;
            }
        }
        
        if (!empty($missing)) {
            throw new ApiException("فیلدهای الزامی فرستاده نشده‌اند: " . implode(', ', $missing), 400);
        }
    }

 // اعتبارسنجی اعداد اعشاری مثبت
    public static function validateFloat($value, string $fieldName): float {
        $filtered = filter_var($value, FILTER_VALIDATE_FLOAT);
        if ($filtered === false || $filtered <= 0) {
            throw new ApiException("فیلد {$fieldName} باید یک عدد مثبت و بزرگتر از صفر باشد.", 400);
        }
        return (float)$filtered;
    }

 // اعتبارسنجی مقدار فقط شامل عدد
    public static function validateDigits(string $value, string $fieldName): void {
        if (!ctype_digit($value)) {
            throw new ApiException("فیلد {$fieldName} باید فقط شامل اعداد باشد.", 400);
        }
    }

 // اعتبارسنجی نام کاربری
    public static function validateUsername(string $username): string {
        $cleaned = self::sanitize($username);
        if (strlen($cleaned) < 3) {
            throw new ApiException("نام کاربری باید حداقل ۳ کاراکتر باشد.", 400);
        }
        return $cleaned;
    }

 // اعتبارسنجی نام کامل
    public static function validateFullName(string $fullName): string {
        $cleaned = self::sanitize($fullName);
        if (strlen($cleaned) < 3) {
            throw new ApiException("نام و نام خانوادگی باید حداقل ۳ کاراکتر باشد.", 400);
        }
        return $cleaned;
    }

 // اعتبارسنجی طول رمز عبور در برابر کلاینت دستکاری‌شده‌ای که رمز کوتاه می‌فرستد
    public static function validatePassword(string $password): string {
        if (mb_strlen($password) < 8) {
            throw new ApiException("رمز عبور باید حداقل ۸ کاراکتر باشد.", 400);
        }
        return $password;
    }

 // برای مقادیری که با prepared statement مقایسه می‌شوند، نباید htmlspecialchars اعمال شود وگرنه مقایسه می‌شکند
    public static function validateIdentifier(string $input, int $maxLength = 150): string {
        $value = trim($input);
        if ($value === '' || mb_strlen($value) > $maxLength) {
            throw new \Exception('مقدار ورودی نامعتبر است');
        }
        return $value;
    }
}
