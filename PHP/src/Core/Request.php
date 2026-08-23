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

 // خواندن php://input و پارس آن فقط در صورت نیاز واقعی انجام می‌شود
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

 // استخراج و نرمال‌سازی هدرهای HTTP به lowercase طبق RFC 7230 §3.2
    private function getHeadersParsed(): array {
        if ($this->headers === null) {
            $raw = [];
            if (function_exists('getallheaders')) {
                $raw = getallheaders();
            } else {
                foreach ($_SERVER as $name => $value) {
                    if (substr($name, 0, 5) === 'HTTP_') {
                        $key = str_replace(' ', '-', ucwords(strtolower(str_replace('_', ' ', substr($name, 5)))));
                        $raw[$key] = $value;
                    }
                }
            }
 // نرمال‌سازی کلیدها به lowercase
            $this->headers = [];
            foreach ($raw as $key => $value) {
                $this->headers[strtolower($key)] = $value;
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

 // آیا این یک درخواست نوشتنی (غیر GET) است؛ برای شیم‌های چندعملیاتی با چند فعل HTTP
    public function isWrite(): bool {
        return in_array($this->getMethod(), ['POST', 'PUT', 'PATCH', 'DELETE'], true);
    }

 // دریافت هدر خاص با جستجوی case-insensitive
    public function getHeader(string $name): ?string {
        $headers = $this->getHeadersParsed();
        return $headers[strtolower($name)] ?? null;
    }

 // دریافت مقدار یک پارامتر از تمام منابع ورودی به ترتیب اولویت JSON، POST، GET
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

 // دریافت تمام پارامترهای ورودی
    public function all(): array {
        return array_merge($this->queryParams, $this->postParams, $this->getJsonParams());
    }

 // دریافت آی‌پی کلاینت؛ عمداً فقط REMOTE_ADDR چون هدرهای HTTP_X_FORWARDED_FOR قابل جعل‌اند
    public function getClientIp(): string {
        return $_SERVER['REMOTE_ADDR'] ?? '127.0.0.1';
    }

 // پاک‌سازی و فیلتر کردن مقادیر رشته‌ای
    public function sanitize(string $value): string {
        return htmlspecialchars(strip_tags(trim($value)), ENT_QUOTES, 'UTF-8');
    }
}
