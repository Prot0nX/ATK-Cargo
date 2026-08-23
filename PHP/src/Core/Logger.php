<?php
// PHP/src/Core/Logger.php

declare(strict_types=1);

namespace App\Core;

class Logger {
    private static ?self $instance = null;
    private string $logDir;
    private array $buffer = [];
    private bool $shutdownRegistered = false;

    private function __construct() {
        $this->logDir = APP_ROOT . '/logs';
        if (!file_exists($this->logDir)) {
            mkdir($this->logDir, 0755, true);
        }
    }

    public static function getInstance(): self {
        if (self::$instance === null) {
            self::$instance = new self();
        }
        return self::$instance;
    }

 // ثبت لاگ با فرمت و سطح مشخص
    public function log(string $message, string $level = 'INFO', string $category = 'app'): void {
        $timestamp = date('Y-m-d H:i:s');
        $ip = $_SERVER['REMOTE_ADDR'] ?? '127.0.0.1';
        $sanitizedMessage = $this->sanitizeMessage($message);

        $logMessage = sprintf("[%s] [%s] [%s] [IP: %s] %s%s", $timestamp, $level, strtoupper($category), $ip, $sanitizedMessage, PHP_EOL);
        $logFile = sprintf("%s/%s.log", $this->logDir, $category);

 // پیام‌ها بافر شده و فقط یک‌بار در پایان اسکریپت روی فایل نوشته می‌شوند
        $this->buffer[$logFile] = ($this->buffer[$logFile] ?? '') . $logMessage;

        if (!$this->shutdownRegistered) {
            $this->shutdownRegistered = true;
            register_shutdown_function([$this, 'flush']);
        }
    }

    public function flush(): void {
        foreach ($this->buffer as $logFile => $contents) {
            file_put_contents($logFile, $contents, FILE_APPEND | LOCK_EX);
        }
        $this->buffer = [];
    }

    public function info(string $message, string $category = 'app'): void {
        $this->log($message, 'INFO', $category);
    }

    public function error(string $message, string $category = 'error'): void {
        $this->log($message, 'ERROR', $category);
    }

    public function security(string $message, string $category = 'security'): void {
        $this->log($message, 'SECURITY', $category);
    }

    public function database(string $message, string $category = 'database'): void {
        $this->log($message, 'DATABASE', $category);
    }

 // حذف اطلاعات حساس مانند پسورد از متن لاگ‌ها
    private function sanitizeMessage(string $message): string {
 // الگوهای تشخیص اطلاعات حساس و جایگزینی آنها
        $patterns = [
            '/("password"\s*:\s*")[^"]+(")/i' => '$1***$2',
            '/("csrf_token"\s*:\s*")[^"]+(")/i' => '$1***$2',
            '/("session_token"\s*:\s*")[^"]+(")/i' => '$1***$2',
        ];
        return preg_replace(array_keys($patterns), array_values($patterns), $message) ?? $message;
    }
}
