<?php
// PHP/src/Controllers/DiagnosticsController.php

declare(strict_types=1);

namespace App\Controllers;

use App\Core\Database;
use App\Core\Request;
use App\Core\Response;
use App\Services\CrashReportRateLimiter;
use PDOException;
use Throwable;

// health-check و دریافت گزارش کرش، عمداً بدون گیت auth تا برای مانیتورینگ و کرش پیش از لاگین در دسترس باشند (Phase2.13)
class DiagnosticsController {
    private Request $request;
    private CrashReportRateLimiter $rateLimiter;

    // سقف حجم crash_reports.log — DEEP_CODE_REVIEW.md Phase2.14.
    private const MAX_CRASH_LOG_BYTES = 50 * 1024 * 1024;

    public function __construct() {
        $this->request = new Request();
        // محدودکننده‌ی اختصاصی به‌جای LoginAttemptLimiter — سقف بالاتر و غیرمسدودکننده (DEEP_CODE_AUDIT.md #۲)
        $this->rateLimiter = new CrashReportRateLimiter();
    }

    // فهرست جداول لازم برای «سالم» دانستن سیستم، مطابق schema.sql (Phase 2.3)
    private const REQUIRED_TABLES = [
        'Users', 'user_sessions', 'CargoInfo', 'InitialInfo',
        'admin_chat_messages', 'admin_chat_reads', 'audit_log',
    ];

    // منطق واقعی health-check جدا از HTTP؛ health_monitor.php این متد را مستقیم صدا می‌زند (Phase3 #28)
    /** @return array{healthy: bool, status: array, missingTables: array} */
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
            // پیام خام اتصال هرگز به پاسخ نمی‌رود تا host/db name افشا نشود، فقط لاگ می‌شود
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

        // عبور از سقف بی‌صدا drop می‌شود (۲۰۰) نه ۴۲۹ تا کلاینت هنگام کرش پیاپی تشویق به retry نشود (DEEP_CODE_AUDIT.md #۲)
        $clientIp = $this->request->getClientIp();
        if ($this->rateLimiter->isOverLimit($clientIp)) {
            Response::json(['success' => true]);
        }
        $this->rateLimiter->registerReport($clientIp);

        $stackTrace = (string)$this->request->get('stackTrace', '');
        if (trim($stackTrace) === '') {
            Response::json(['success' => false, 'message' => 'stackTrace الزامی است'], 400);
        }

        // ثبت فقط برای مرجع/دیباگ؛ username معتبرشده نیست چون این endpoint گیت auth ندارد
        $entry = [
            'timestamp' => date('Y-m-d H:i:s'),
            'username' => $this->request->sanitize((string)$this->request->get('username', '')),
            'appVersion' => $this->request->sanitize((string)$this->request->get('appVersion', '')),
            'deviceModel' => $this->request->sanitize((string)$this->request->get('deviceModel', '')),
            'androidVersion' => $this->request->sanitize((string)$this->request->get('androidVersion', '')),
            // stackTrace عمداً sanitize نمی‌شود تا برای deobfuscate با mapping.txt خوانا بماند؛ طولش محدود می‌شود
            'stackTrace' => mb_substr($stackTrace, 0, 8000),
        ];

        try {
            $logDir = APP_ROOT . '/logs';
            if (!is_dir($logDir)) {
                mkdir($logDir, 0755, true);
            }
            $logFile = $logDir . '/crash_reports.log';

            // نوشتن متوقف می‌شود اگر فایل از سقف عبور کند، چون logrotate روزانه برای این سرعت حمله کند است
            if (!file_exists($logFile) || filesize($logFile) <= self::MAX_CRASH_LOG_BYTES) {
                file_put_contents(
                    $logFile,
                    json_encode($entry, JSON_UNESCAPED_UNICODE) . "\n",
                    FILE_APPEND | LOCK_EX
                );
            }
        } catch (Throwable $e) {
            error_log('DiagnosticsController::reportCrash - ' . $e->getMessage());
            // best-effort: حتی اگر نوشتن لاگ شکست بخورد، success برمی‌گردانیم تا کلاینت retry نکند
        }

        Response::json(['success' => true]);
    }
}
