<?php
// PHP/protected_proxy.php

declare(strict_types=1);

// پروکسی محافظت‌شده: فایل‌های *.php موجود در همین پوشه (به‌جز موارد استثنا) را
// از طریق پارامتر target اجرا می‌کند. منطق کاملاً مطابق نسخه‌ی قبلی حفظ شده است.

const PROXY_LOG_DIR = 'log';
const PROXY_ACCESS_LOG = PROXY_LOG_DIR . '/proxy_access.log';
const PROXY_BLOCKED_IPS_FILE = PROXY_LOG_DIR . '/blocked_ips.txt';
const PROXY_EXCLUDED_FILES = ['protected_proxy.php', 'file_manager.php', 'proxy_generator.php'];
const PROXY_RATE_LIMIT_MAX = 60;
const PROXY_RATE_LIMIT_WINDOW = 60;
const PROXY_WHITELIST_CACHE_KEY = 'protected_proxy_whitelist';
const PROXY_WHITELIST_CACHE_TTL = 300;

if (!is_dir(PROXY_LOG_DIR)) {
    mkdir(PROXY_LOG_DIR, 0755, true);
}

header('X-Content-Type-Options: nosniff');
header('X-Frame-Options: DENY');
header('X-XSS-Protection: 1; mode=block');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if (($_SERVER['REQUEST_METHOD'] ?? '') === 'OPTIONS') {
    http_response_code(200);
    exit();
}

final class ProtectedProxy {
    private array $whitelist = [];
    private array $blockedIps = [];
    private ?string $pendingLogLine = null;

    public function __construct() {
        $this->loadWhitelist();
        $this->loadBlockedIps();
    }

    // whitelist در APCu کش می‌شود تا glob() روی هر درخواست دوباره اسکن دیسک نکند.
    private function loadWhitelist(): void {
        if (function_exists('apcu_fetch')) {
            $cached = apcu_fetch(PROXY_WHITELIST_CACHE_KEY, $ok);
            if ($ok && is_array($cached)) {
                $this->whitelist = $cached;
                return;
            }
        }

        $this->whitelist = array_values(array_diff(glob('*.php') ?: [], PROXY_EXCLUDED_FILES));

        if (function_exists('apcu_store')) {
            apcu_store(PROXY_WHITELIST_CACHE_KEY, $this->whitelist, PROXY_WHITELIST_CACHE_TTL);
        }
    }

    private function loadBlockedIps(): void {
        if (file_exists(PROXY_BLOCKED_IPS_FILE)) {
            $lines = file(PROXY_BLOCKED_IPS_FILE) ?: [];
            $this->blockedIps = array_filter(array_map('trim', $lines));
        }
    }

    private function clientIp(): string {
        return $_SERVER['REMOTE_ADDR'] ?? 'unknown';
    }

    private function logAccess(string $action, ?string $target = null, string $status = 'SUCCESS'): void {
        $timestamp = date('Y-m-d H:i:s');
        $ip = $this->clientIp();
        $userAgent = $_SERVER['HTTP_USER_AGENT'] ?? 'unknown';
        $method = $_SERVER['REQUEST_METHOD'] ?? 'unknown';

        $line = "[$timestamp] IP: $ip | Method: $method | Action: $action | Status: $status";
        if ($target) {
            $line .= " | Target: $target";
        }
        $line .= ' | User-Agent: ' . substr($userAgent, 0, 100) . "\n";

        $this->pendingLogLine = $line;
    }

    // نوشتن لاگ بعد از ارسال پاسخ به کلاینت انجام می‌شود تا I/O دیسک روی زمان پاسخ اثر نگذارد.
    public function flushLog(): void {
        if ($this->pendingLogLine !== null) {
            file_put_contents(PROXY_ACCESS_LOG, $this->pendingLogLine, FILE_APPEND | LOCK_EX);
            $this->pendingLogLine = null;
        }
    }

    private function checkRateLimit(): bool {
        $ip = $this->clientIp();
        $now = time();

        if (function_exists('apcu_fetch')) {
            $key = 'proxy_rate_' . $ip;
            $timestamps = apcu_fetch($key, $ok);
            if (!$ok || !is_array($timestamps)) {
                $timestamps = [];
            }
            $timestamps = array_values(array_filter($timestamps, fn($t) => ($now - $t) < PROXY_RATE_LIMIT_WINDOW));
            if (count($timestamps) >= PROXY_RATE_LIMIT_MAX) {
                return false;
            }
            $timestamps[] = $now;
            apcu_store($key, $timestamps, PROXY_RATE_LIMIT_WINDOW);
            return true;
        }

        // Fallback در صورت نبود extension مربوط به APCu روی سرور
        $safeIp = str_replace([':', '.', '/'], '_', $ip);
        $rateFile = PROXY_LOG_DIR . "/rate_limit_$safeIp.txt";
        $timestamps = [];
        if (file_exists($rateFile)) {
            $data = file_get_contents($rateFile);
            $timestamps = $data ? json_decode($data, true) : [];
        }
        if (!is_array($timestamps)) {
            $timestamps = [];
        }
        $timestamps = array_values(array_filter($timestamps, fn($t) => ($now - $t) < PROXY_RATE_LIMIT_WINDOW));
        if (count($timestamps) >= PROXY_RATE_LIMIT_MAX) {
            return false;
        }
        $timestamps[] = $now;
        file_put_contents($rateFile, json_encode($timestamps));
        return true;
    }

    private function isValidTarget(string $target): bool {
        if ($target === '' || str_contains($target, '..') || str_contains($target, '/') || str_contains($target, '\\')) {
            return false;
        }
        return in_array($target, $this->whitelist, true) && file_exists($target);
    }

    private function execute(string $target): string {
        $originalGet = $_GET;
        $originalPost = $_POST;
        $originalRequest = $_REQUEST;
        unset($_GET['target'], $_REQUEST['target']);

        ob_start();
        try {
            include $target;
            $output = ob_get_contents();
        } catch (Exception $e) {
            $output = json_encode([
                'error' => 'Execution error: ' . $e->getMessage(),
                'timestamp' => time(),
            ]);
        } finally {
            ob_end_clean();
        }

        $_GET = $originalGet;
        $_POST = $originalPost;
        $_REQUEST = $originalRequest;

        return (string)$output;
    }

    public function handle(): void {
        $ip = $this->clientIp();

        if (in_array($ip, $this->blockedIps, true)) {
            $this->logAccess('BLOCKED_IP_ACCESS', null, 'BLOCKED');
            http_response_code(403);
            echo json_encode(['error' => 'Access denied', 'code' => 'IP_BLOCKED']);
            return;
        }

        if (!$this->checkRateLimit()) {
            $this->logAccess('RATE_LIMIT_EXCEEDED', null, 'BLOCKED');
            http_response_code(429);
            echo json_encode(['error' => 'Rate limit exceeded', 'code' => 'RATE_LIMIT']);
            return;
        }

        $target = $_GET['target'] ?? $_POST['target'] ?? null;

        if (!$target) {
            $this->logAccess('MISSING_TARGET', null, 'ERROR');
            http_response_code(400);
            echo json_encode([
                'error' => 'Missing target parameter',
                'usage' => 'protected_proxy.php?target=filename.php',
                'available_files' => $this->whitelist,
            ]);
            return;
        }

        if (!$this->isValidTarget($target)) {
            $this->logAccess('INVALID_TARGET', $target, 'ERROR');
            http_response_code(404);
            echo json_encode([
                'error' => 'Invalid or unauthorized target file',
                'target' => $target,
                'available_files' => $this->whitelist,
            ]);
            return;
        }

        $this->logAccess('PROXY_ACCESS', $target);
        echo $this->execute($target);
    }
}

$proxy = null;
try {
    $proxy = new ProtectedProxy();
    $proxy->handle();
} catch (Exception $e) {
    error_log('Proxy Error: ' . $e->getMessage());
    http_response_code(500);
    echo json_encode([
        'error' => 'Internal server error',
        'message' => 'Please check server logs',
        'timestamp' => time(),
    ]);
}

if (function_exists('fastcgi_finish_request')) {
    fastcgi_finish_request();
}
if ($proxy !== null) {
    $proxy->flushLog();
}
