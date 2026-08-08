<?php
// PHP/src/Core/Request.php

declare(strict_types=1);

namespace App\Core;

use Exception;

class Request {
    private string $method;
    private array $queryParams = [];
    private array $postParams = [];
    private ?array $jsonParams = null;
    private ?array $headers = null;

    public function __construct() {
        $this->method = $_SERVER['REQUEST_METHOD'] ?? 'GET';
        $this->queryParams = $_GET;
        $this->postParams = $_POST;
    }

    // خواندن php://input و پارس هدرها فقط در صورت نیاز واقعی انجام می‌شود
    // (getHeader در کل پروژه استفاده نمی‌شود؛ jsonParams فقط وقتی لازم است پارس می‌شود)
    private function getJsonParams(): array {
        if ($this->jsonParams === null) {
            $this->jsonParams = [];
            $rawInput = file_get_contents('php://input');
            if (!empty($rawInput)) {
                try {
                    $decoded = json_decode($rawInput, true);
                    if (json_last_error() === JSON_ERROR_NONE && is_array($decoded)) {
                        $this->jsonParams = $decoded;
                    }
                } catch (Exception $e) {
                    // خطای پارس نامعتبر JSON
                }
            }
        }
        return $this->jsonParams;
    }

    private function getHeadersParsed(): array {
        if ($this->headers === null) {
            $this->headers = [];
            if (function_exists('getallheaders')) {
                $this->headers = getallheaders();
            } else {
                foreach ($_SERVER as $name => $value) {
                    if (substr($name, 0, 5) == 'HTTP_') {
                        $this->headers[str_replace(' ', '-', ucwords(strtolower(str_replace('_', ' ', substr($name, 5)))))] = $value;
                    }
                }
            }
        }
        return $this->headers;
    }

    public function getMethod(): string {
        return strtoupper($this->method);
    }

    public function isPost(): bool {
        return $this->getMethod() === 'POST';
    }

    public function isGet(): bool {
        return $this->getMethod() === 'GET';
    }

    /**
     * دریافت هدر خاص
     */
    public function getHeader(string $name): ?string {
        $headers = $this->getHeadersParsed();
        return $headers[$name] ?? $headers[strtolower($name)] ?? null;
    }

    /**
     * دریافت مقدار یک پارامتر از تمام منابع ورودی (JSON, POST, GET) به ترتیب اولویت
     */
    public function get(string $key, $default = null) {
        $jsonParams = $this->getJsonParams();
        if (isset($jsonParams[$key])) {
            return $jsonParams[$key];
        }
        if (isset($this->postParams[$key])) {
            return $this->postParams[$key];
        }
        return $this->queryParams[$key] ?? $default;
    }

    /**
     * دریافت تمام پارامترهای ورودی
     */
    public function all(): array {
        return array_merge($this->queryParams, $this->postParams, $this->getJsonParams());
    }

    /**
     * دریافت آی‌پی کلاینت به صورت امن
     */
    public function getClientIp(): string {
        return $_SERVER['HTTP_CLIENT_IP'] 
            ?? $_SERVER['HTTP_X_FORWARDED_FOR'] 
            ?? $_SERVER['REMOTE_ADDR'] 
            ?? '127.0.0.1';
    }

    /**
     * پاک‌سازی و فیلتر کردن مقادیر رشته‌ای
     */
    public function sanitize(string $value): string {
        return htmlspecialchars(strip_tags(trim($value)), ENT_QUOTES, 'UTF-8');
    }
}
