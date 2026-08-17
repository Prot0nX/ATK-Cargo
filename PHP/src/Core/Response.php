<?php
// PHP/src/Core/Response.php

declare(strict_types=1);

namespace App\Core;

class Response {
    /**
     * ارسال هدرهای امنیتی استاندارد سیستم
     */
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

        // برخی endpointها (مثل AppApiController::sendCacheableJsonResponse،
        // AnalyticsController::sendCacheableAnalyticsResponse) عمداً قبل از
        // فراخوانی json()/success() یک Cache-Control با max-age مجاز
        // (ETag/private) تنظیم می‌کنند تا OkHttp/CDN بتواند پاسخ را کش کند؛
        // اگر اینجا همیشه no-store ست شود، آن هدر بی‌سروصدا بازنویسی و کل
        // مکانیزم کش/ETag آن endpointها خنثی می‌شد. پس فقط وقتی هیچ
        // Cache-Control از قبل صف نشده، مقدار پیش‌فرض no-store اعمال می‌شود.
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

    /**
     * ارسال پاسخ JSON و خروج از برنامه
     */
    public static function json($data, int $httpCode = 200): void {
        self::sendSecurityHeaders();
        http_response_code($httpCode);

        // فشرده‌سازی PHP-level حذف شد (P-06/P-08): .htaccess از قبل
        // mod_deflate را برای application/json فعال کرده، پس فشرده‌سازی
        // دوباره‌ی اینجا فقط هزینه‌ی CPU اضافه بود — علاوه بر این، چون بعد از
        // http_response_code()/header() فراخوانی می‌شد، در برخی پیکربندی‌های
        // SAPI اصلاً gzip واقعی اعمال نمی‌شد و فقط هزینه‌ی بافر می‌ماند.
        echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
        exit;
    }

    /**
     * ارسال پاسخ خطای استاندارد
     */
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

    /**
     * ارسال پاسخ موفقیت استاندارد
     */
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
