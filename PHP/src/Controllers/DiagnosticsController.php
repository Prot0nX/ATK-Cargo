<?php
// PHP/src/Controllers/DiagnosticsController.php

declare(strict_types=1);

namespace App\Controllers;

use App\Core\Database;
use App\Core\Request;
use App\Core\Response;
use App\Services\LoginAttemptLimiter;
use PDOException;
use Throwable;

/**
 * health-check و دریافت گزارش کرش خودمیزبان (DEEP_CODE_AUDIT.md #Phase2.13).
 * عمداً بدون گیت auth — health-check باید برای ابزار مانیتورینگ خارجی در
 * دسترس باشد، و گزارش کرش باید حتی اگر نشست کاربر معتبر نیست (یا کرش قبل
 * از لاگین رخ داده) هم ارسال شود؛ هر دو پاسخ عمداً حداقلی است (بدون افشای
 * جزئیات داخلی) چون بدون احراز هویت قابل مشاهده‌اند.
 */
class DiagnosticsController {
    private Request $request;
    private LoginAttemptLimiter $rateLimiter;

    // سقف حجم crash_reports.log — DEEP_CODE_REVIEW.md Phase2.14.
    private const MAX_CRASH_LOG_BYTES = 50 * 1024 * 1024;

    public function __construct() {
        $this->request = new Request();
        $this->rateLimiter = new LoginAttemptLimiter();
    }

    // تعداد جداولی که باید موجود باشند تا سیستم را «سالم» بدانیم — همان
    // فهرست schema.sql (Phase 2.3)، بدون نیاز به schema_migrations چون این
    // پروژه چنین جدولی ندارد.
    private const REQUIRED_TABLES = [
        'Users', 'user_sessions', 'CargoInfo', 'InitialInfo',
        'admin_chat_messages', 'admin_chat_reads', 'audit_log',
    ];

    /**
     * منطق واقعی health-check، جدا از HTTP (Phase3 #28). scripts/health_monitor.php
     * که از cron اجرا می‌شود همین متد را مستقیماً صدا می‌زند (بدون HTTP
     * round-trip روی localhost و بدون برخورد با فیلتر User-Agent در
     * config/.htaccess که curl/wget را مسدود می‌کند)، تا منطق «سالم بودن»
     * در یک‌جا بماند و health() و مانیتور از آن دور نیفتند.
     *
     * @return array{healthy: bool, status: array, missingTables: array}
     */
    public function evaluateHealth(): array {
        $status = [
            'database' => false,
            'requiredTables' => false,
            'apcu' => function_exists('apcu_enabled') && apcu_enabled(),
        ];
        $missingTables = [];

        try {
            $pdo = Database::getInstance()->getPdoConnection();
            $status['database'] = true;

            $stmt = $pdo->query('SHOW TABLES');
            $existing = $stmt ? array_map(fn(array $row) => (string)reset($row), $stmt->fetchAll()) : [];
            foreach (self::REQUIRED_TABLES as $table) {
                if (!in_array($table, $existing, true)) {
                    $missingTables[] = $table;
                }
            }
            $status['requiredTables'] = empty($missingTables);
        } catch (PDOException|Throwable $e) {
            // پیام خام اتصال (که می‌تواند host/db name را افشا کند) هرگز به
            // پاسخ نمی‌رود، فقط لاگ می‌شود.
            error_log('DiagnosticsController::evaluateHealth - ' . $e->getMessage());
        }

        return [
            'healthy' => $status['database'] && $status['requiredTables'],
            'status' => $status,
            'missingTables' => $missingTables,
        ];
    }

    public function health(): void {
        $result = $this->evaluateHealth();
        Response::json([
            'success' => $result['healthy'],
            'status' => $result['status'],
            'missingTables' => $result['missingTables'],
        ], $result['healthy'] ? 200 : 503);
    }

    public function reportCrash(): void {
        if (!$this->request->isPost()) {
            Response::json(['success' => false, 'message' => 'روش درخواست مجاز نیست'], 405);
        }

        // بدون auth است (عمداً — کرش می‌تواند قبل از لاگین رخ دهد)، پس بدون
        // rate limit یک مهاجم می‌تواند دیسک را با نوشتن مکرر پر کند
        // (DEEP_CODE_REVIEW.md Phase2.14). از همان LoginAttemptLimiter موجود
        // با یک کلید مجزا (پیشوند crash_) استفاده می‌شود تا با شمارنده‌های
        // واقعی لاگین قاطی نشود؛ سقف مؤثر آن (۵ در ۱۵ دقیقه) برای این
        // endpoint کافی است.
        $rateLimitKey = 'crash_' . $this->request->getClientIp();
        if ($this->rateLimiter->isLocked($rateLimitKey, $rateLimitKey)) {
            Response::json(['success' => false, 'message' => 'تعداد درخواست‌ها بیش از حد مجاز است.'], 429);
        }
        $this->rateLimiter->registerFailedAttempt($rateLimitKey, $rateLimitKey);

        $stackTrace = (string)$this->request->get('stackTrace', '');
        if (trim($stackTrace) === '') {
            Response::json(['success' => false, 'message' => 'stackTrace الزامی است'], 400);
        }

        // فقط برای مرجع/دیباگ ثبت می‌شود، نه به‌عنوان هویت معتبرشده (این
        // endpoint گیت auth ندارد، پس username می‌تواند جعلی باشد).
        $entry = [
            'timestamp' => date('Y-m-d H:i:s'),
            'username' => $this->request->sanitize((string)$this->request->get('username', '')),
            'appVersion' => $this->request->sanitize((string)$this->request->get('appVersion', '')),
            'deviceModel' => $this->request->sanitize((string)$this->request->get('deviceModel', '')),
            'androidVersion' => $this->request->sanitize((string)$this->request->get('androidVersion', '')),
            // stackTrace عمداً sanitize نمی‌شود — یک stack trace خام Kotlin/Java
            // است، نه HTML، و escape کردن آن را برای deobfuscate با mapping.txt
            // ناخوانا می‌کند. طول آن محدود می‌شود تا یک payload بزرگ فایل لاگ را
            // پر نکند.
            'stackTrace' => mb_substr($stackTrace, 0, 8000),
        ];

        try {
            $logDir = APP_ROOT . '/logs';
            if (!is_dir($logDir)) {
                mkdir($logDir, 0755, true);
            }
            $logFile = $logDir . '/crash_reports.log';

            // اگر فایل از سقف عبور کرده، دیگر ننویس — logrotate روزانه اجرا
            // می‌شود که برای سرعت یک حمله‌ی نوشتن مکرر کند است.
            if (!file_exists($logFile) || filesize($logFile) <= self::MAX_CRASH_LOG_BYTES) {
                file_put_contents(
                    $logFile,
                    json_encode($entry, JSON_UNESCAPED_UNICODE) . "\n",
                    FILE_APPEND | LOCK_EX
                );
            }
        } catch (Throwable $e) {
            error_log('DiagnosticsController::reportCrash - ' . $e->getMessage());
            // best-effort — حتی اگر نوشتن لاگ شکست بخورد، به کلاینت success
            // برمی‌گردانیم تا کلاینت آن را دوباره و دوباره retry نکند.
        }

        Response::json(['success' => true]);
    }
}
