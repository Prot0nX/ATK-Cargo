<?php
// PHP/src/Core/Response.php

declare(strict_types=1);

namespace App\Core;

class Response {
    // ارسال هدرهای امنیتی استاندارد سیستم
    public static function sendSecurityHeaders(): void {
        if (headers_sent()) {
            return;
        }
        header('Content-Type: application/json; charset=UTF-8');
        header('X-Content-Type-Options: nosniff');
        header('X-Frame-Options: DENY');
        header('X-XSS-Protection: 1; mode=block');
        header('Content-Security-Policy: default-src \'self\'');
        header('Strict-Transport-Security: max-age=31536000; includeSubDomains; preload');

        // اگر endpoint از قبل Cache-Control خودش را ست کرده، اینجا بازنویسی نمی‌شود
        if (!self::hasHeader('Cache-Control')) {
            header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
            header('Pragma: no-cache');
        }
    }

    private static function hasHeader(string $name): bool {
        foreach (headers_list() as $header) {
            if (stripos($header, $name . ':') === 0) {
                return true;
            }
        }
        return false;
    }

    // ارسال پاسخ JSON و خروج از برنامه
    public static function json($data, int $httpCode = 200): void {
        self::sendSecurityHeaders();
        http_response_code($httpCode);

        // فشرده‌سازی PHP-level حذف شد؛ mod_deflate در .htaccess همین کار را انجام می‌دهد
        echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
        exit;
    }

    // ارسال پاسخ خطای استاندارد
    public static function error(string $message, int $httpCode = 400, ?array $details = null): void {
        $response = [
            'success' => false,
            'message' => $message
        ];
        
        if ($details !== null) {
            $response['details'] = $details;
        }
        
        self::json($response, $httpCode);
    }

    // ارسال پاسخ موفقیت استاندارد
    public static function success(string $message = '', ?array $data = null, int $httpCode = 200): void {
        $response = [
            'success' => true
        ];
        
        if ($message !== '') {
            $response['message'] = $message;
        }
        
        if ($data !== null) {
            $response = array_merge($response, $data);
        }
        
        self::json($response, $httpCode);
    }
}
