<?php

declare(strict_types=1);

namespace AtkCargo\Core;

/**
 * Standard JSON Response builder with security headers
 */
class Response
{
    /**
     * Send standard JSON response with security headers and HTTP code
     * 
     * @param mixed $data Data payload to be serialized as JSON
     * @param int $httpStatusCode HTTP Status Code (default: 200)
     */
    public static function json($data, int $httpStatusCode = 200): void
    {
        // 1. Set Security Headers
        self::setSecurityHeaders();

        // 2. Set Content Type
        header('Content-Type: application/json; charset=UTF-8');

        // 3. Set Status Code
        http_response_code($httpStatusCode);

        // 4. Send JSON Payload
        try {
            echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
        } catch (\JsonException $e) {
            error_log("JSON Encoding Failure: " . $e->getMessage());
            echo json_encode([
                'success' => false,
                'message' => 'خطای داخلی در فرمت خروجی سرور'
            ]);
        }
        exit;
    }

    /**
     * Send generic error response
     */
    public static function error(string $message, int $httpStatusCode = 400): void
    {
        self::json([
            'success' => false,
            'message' => $message
        ], $httpStatusCode);
    }

    /**
     * Set essential security headers
     */
    private static function setSecurityHeaders(): void
    {
        if (headers_sent()) {
            return;
        }

        header('X-Content-Type-Options: nosniff');
        header('X-Frame-Options: DENY');
        header('X-XSS-Protection: 1; mode=block');
        header('Content-Security-Policy: default-src \'self\'');
        header('Strict-Transport-Security: max-age=31536000; includeSubDomains; preload');
        header('Cache-Control: no-store, no-cache, must-revalidate');
    }
}
