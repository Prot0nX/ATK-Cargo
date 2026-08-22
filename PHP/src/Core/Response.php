<?php
// PHP/src/Core/Response.php

declare(strict_types=1);

namespace App\Core;

use App\Exceptions\ResponseSentException;

class Response {
    // پایین‌ترین X-App-Version که کدهای HTTP معنادار (401/403/409/422/429 و...) به‌جای 200 می‌گیرد؛
    // نسخه‌های قدیمی‌تر همچنان 200 + success:false می‌گیرند تا نشکنند (DEEP_CODE_AUDIT.md فاز۳ #۲۸)
    public const HTTP_CODES_MIN_APP_VERSION = '4.1.0';

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
        // زیر PHPUnit، exit جایگزین یک exception قابل‌catch می‌شود تا کنترلرها بدون kill شدن پروسه قابل تست باشند
        if (defined('TESTING_MODE') && TESTING_MODE) {
            throw new ResponseSentException($data, $httpCode);
        }

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

    // بدنه‌ی خطای واحد با کد HTTP وابسته به نسخه‌ی کلاینت؛ قرارداد یکسان جایگزین ۲۰۰-همیشگی قدیمی
    // در AuthController/ChatController (DEEP_CODE_AUDIT.md فاز۳ #۲۸) — $body باید شامل success:false باشد
    public static function versionGatedJson(array $body, int $legacyHttpCode, int $newHttpCode): void {
        $appVersion = (new Request())->getHeader('X-App-Version');
        $useNewCode = $appVersion !== null && version_compare((string)$appVersion, self::HTTP_CODES_MIN_APP_VERSION, '>=');
        self::json($body, $useNewCode ? $newHttpCode : $legacyHttpCode);
    }

    // پاسخ GET قابل‌کش با ETag/304؛ برای اندپوینت‌های غیرقابل‌تغییر که فقط باید وقتی محتوا واقعاً عوض شده دوباره دانلود شوند
    public static function cacheableJson(array $data, array $etagSource, int $maxAgeSeconds): void {
        $etag = '"' . md5(json_encode($etagSource, JSON_UNESCAPED_UNICODE)) . '"';
        header('Cache-Control: private, max-age=' . $maxAgeSeconds);
        header("ETag: $etag");

        $ifNoneMatch = (new Request())->getHeader('If-None-Match');
        if ($ifNoneMatch !== null && trim($ifNoneMatch) === $etag) {
            http_response_code(304);
            exit;
        }

        self::json($data);
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
