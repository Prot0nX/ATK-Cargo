<?php
// PHP/src/Services/SecurityAlerter.php

declare(strict_types=1);

namespace App\Services;

use App\Core\Logger;

// اعلان فوری رویدادهای امنیتی بحرانی از طریق Telegram Bot API؛ بدون تنظیم توکن/چت‌آیدی کاملاً no-op است و خطاهایش هرگز throw نمی‌شوند
class SecurityAlerter {
    private static ?self $instance = null;

    private const COOLDOWN_SECONDS = 300; // ۵ دقیقه — از اسپم یک رویداد تکراری جلوگیری می‌کند
    private const HTTP_TIMEOUT_SECONDS = 3;
    private const CACHE_PREFIX = 'security_alert_cooldown_';

    private ?string $botToken;
    private ?string $chatId;

    private function __construct() {
        $this->botToken = $_ENV['SECURITY_ALERT_TELEGRAM_BOT_TOKEN'] ?? (getenv('SECURITY_ALERT_TELEGRAM_BOT_TOKEN') ?: null);
        $this->chatId = $_ENV['SECURITY_ALERT_TELEGRAM_CHAT_ID'] ?? (getenv('SECURITY_ALERT_TELEGRAM_CHAT_ID') ?: null);
    }

    public static function getInstance(): self {
        if (self::$instance === null) {
            self::$instance = new self();
        }
        return self::$instance;
    }

    public function isConfigured(): bool {
        return !empty($this->botToken) && !empty($this->chatId);
    }

 /** @param string $event شناسه‌ی کوتاه رویداد مثل REFRESH_TOKEN_REUSE_DETECTED @param string $message متن فارسی برای ادمین @param string|null $dedupeKey کلید یکتا برای cooldown (پیش‌فرض: خود $event) */
    public function alert(string $event, string $message, ?string $dedupeKey = null): void {
        try {
            Logger::getInstance()->security("[ALERT] [$event] $message");

 // نوشتن best-effort در DB مانیتورینگ — تنها کانال هشدار واقعی روی سروری که اصلاً دسترسی خروجی به اینترنت ندارد (Telegram زیر همیشه no-op می‌ماند).
            MonitoringEventLogger::record(
                $event,
                'critical',
                $message,
                self::inferSource($event),
                $dedupeKey ?? $event
            );

            if (!$this->isConfigured()) {
                return;
            }

            $cooldownKey = self::CACHE_PREFIX . ($dedupeKey ?? $event);
            if ($this->isInCooldown($cooldownKey)) {
                return;
            }
            $this->markCooldown($cooldownKey);

            $this->sendTelegram("🔒 هشدار امنیتی ATK-Cargo\n\n[$event]\n$message\n\n" . date('Y-m-d H:i:s'));
        } catch (\Throwable $e) {
 // اعلان هرگز نباید مسیر اصلی (login/refresh/...) را بشکند.
            error_log('SecurityAlerter failed: ' . $e->getMessage());
        }
    }

 // تماس شبکه به بعد از پایان اسکریپت موکول می‌شود تا مسیر بحرانی login/refresh منتظر Telegram API نماند
    private function sendTelegram(string $text): void {
        register_shutdown_function(function () use ($text): void {
            if (function_exists('fastcgi_finish_request')) {
                @fastcgi_finish_request();
            }
            $this->sendTelegramNow($text);
        });
    }

    private function sendTelegramNow(string $text): void {
        $url = "https://api.telegram.org/bot{$this->botToken}/sendMessage";
        $payload = json_encode([
            'chat_id' => $this->chatId,
            'text' => $text,
        ], JSON_UNESCAPED_UNICODE);

        if ($payload === false) {
            return;
        }

        $context = stream_context_create([
            'http' => [
                'method' => 'POST',
                'header' => "Content-Type: application/json\r\n",
                'content' => $payload,
                'timeout' => self::HTTP_TIMEOUT_SECONDS,
                'ignore_errors' => true,
            ],
        ]);

 // @ عمدی: اگر شبکه/DNS در دسترس نباشد نباید warning درز کند؛ نتیجه هرچه باشد نادیده گرفته می‌شود.
        @file_get_contents($url, false, $context);
    }

    private function isInCooldown(string $key): bool {
        if (function_exists('apcu_fetch')) {
            $value = apcu_fetch($key, $ok);
            return $ok && $value !== false;
        }
        $file = $this->fallbackFile($key);
        if (!file_exists($file)) {
            return false;
        }
        $expiresAt = (int)file_get_contents($file);
        return $expiresAt > time();
    }

    private function markCooldown(string $key): void {
        if (function_exists('apcu_store')) {
            apcu_store($key, 1, self::COOLDOWN_SECONDS);
            return;
        }
        file_put_contents($this->fallbackFile($key), (string)(time() + self::COOLDOWN_SECONDS));
    }

    private function fallbackFile(string $key): string {
        $safeKey = preg_replace('/[^a-zA-Z0-9_]/', '_', $key) ?? 'unknown';
        $dir = APP_ROOT . '/log';
        if (!is_dir($dir)) {
            mkdir($dir, 0755, true);
        }
        return $dir . "/{$safeKey}.json";
    }

 // health_monitor.php رویدادهای HEALTH_CHECK_* می‌فرستد؛ بقیه از مسیرهای امنیتی می‌آیند
    private static function inferSource(string $event): string {
        return str_starts_with($event, 'HEALTH_CHECK') ? 'health_check' : 'security';
    }
}