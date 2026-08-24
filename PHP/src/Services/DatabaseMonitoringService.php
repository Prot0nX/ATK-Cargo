<?php
// PHP/src/Services/DatabaseMonitoringService.php — Processlist/Kill Query/متغیرهای وضعیت زنده و (در صورت دسترسی)
// Performance Schema برای پنل MySQL_Manager. تمام کوئری‌های اطلاعاتی از MySQLMetaRepository می‌آیند؛ این سرویس
// فقط شکل‌دهی و اعتبارسنجی/audit عملیات مدیریتی (Kill Query) را انجام می‌دهد.

declare(strict_types=1);

namespace App\Services;

use App\Core\Database;
use App\Exceptions\ApiException;
use App\Repositories\MySQLMetaRepository;
use PDO;
use Throwable;

class DatabaseMonitoringService {
    private const STATUS_KEYS = [
        'Threads_connected', 'Threads_running', 'Connections', 'Questions', 'Slow_queries',
        'Uptime', 'Bytes_received', 'Bytes_sent', 'Innodb_buffer_pool_reads', 'Innodb_buffer_pool_read_requests',
        'Aborted_connects', 'Aborted_clients',
    ];
    private const VARIABLE_KEYS = ['max_connections', 'innodb_buffer_pool_size', 'version'];

    private PDO $pdo;
    private MySQLMetaRepository $meta;

    public function __construct() {
        $this->pdo = Database::getInstance()->getPdoConnection();
        $this->meta = new MySQLMetaRepository();
    }

    public function metrics(): array {
        $status = $this->meta->globalStatus(self::STATUS_KEYS);
        $variables = $this->meta->globalVariables(self::VARIABLE_KEYS);

        $bufferReads = (float)($status['Innodb_buffer_pool_reads'] ?? 0);
        $bufferRequests = (float)($status['Innodb_buffer_pool_read_requests'] ?? 0);
        $cacheHitRatio = $bufferRequests > 0 ? round((1 - ($bufferReads / $bufferRequests)) * 100, 2) : null;

        return [
            'threadsConnected' => (int)($status['Threads_connected'] ?? 0),
            'threadsRunning' => (int)($status['Threads_running'] ?? 0),
            'maxConnections' => (int)($variables['max_connections'] ?? 0),
            'totalConnections' => (int)($status['Connections'] ?? 0),
            'questions' => (int)($status['Questions'] ?? 0),
            'slowQueries' => (int)($status['Slow_queries'] ?? 0),
            'uptimeSeconds' => (int)($status['Uptime'] ?? 0),
            'bytesReceived' => (int)($status['Bytes_received'] ?? 0),
            'bytesSent' => (int)($status['Bytes_sent'] ?? 0),
            'abortedConnects' => (int)($status['Aborted_connects'] ?? 0),
            'innodbBufferPoolSize' => (int)($variables['innodb_buffer_pool_size'] ?? 0),
            'innodbCacheHitRatioPercent' => $cacheHitRatio,
            'hasAccess' => !empty($status),
        ];
    }

    public function processList(): array {
        $rows = $this->meta->processList();
        return array_map(function (array $row): array {
            return [
                'id' => (int)($row['Id'] ?? 0),
                'user' => (string)($row['User'] ?? ''),
                'host' => (string)($row['Host'] ?? ''),
                'db' => $row['db'] ?? null,
                'command' => (string)($row['Command'] ?? ''),
                'time' => (int)($row['Time'] ?? 0),
                'state' => $row['State'] ?? null,
                'info' => $row['Info'] ?? null,
            ];
        }, $rows);
    }

    // KILL یک دستور مدیریتی MySQL است، نه DML؛ PDO آن را parameterize نمی‌کند — به همین دلیل شناسه‌ی Thread
    // پیش از استفاده صراحتاً به int cast می‌شود (نه اعتماد به رشته‌ی ورودی خام) تا امکان تزریق وجود نداشته باشد.
    public function killQuery(int $processId, string $actor): void {
        if ($processId <= 0) {
            throw new ApiException('شناسه‌ی Query نامعتبر است.', 422);
        }

        // دفاع در عمق: حتی اگر UI دکمه‌ی Kill را برای Threadهای سیستمی مخفی می‌کند، سرور هم مستقل بررسی می‌کند —
        // Kill کردن یک Thread داخلی MySQL (system user / Daemon مثل InnoDB purge worker) می‌تواند سرور را بی‌ثبات کند
        $target = null;
        foreach ($this->meta->processList() as $row) {
            if ((int)($row['Id'] ?? 0) === $processId) {
                $target = $row;
                break;
            }
        }
        if ($target === null) {
            throw new ApiException('این Query یافت نشد (ممکن است از قبل پایان یافته باشد).', 404);
        }
        if (($target['User'] ?? '') === 'system user' || ($target['Command'] ?? '') === 'Daemon') {
            throw new ApiException('امکان متوقف کردن Threadهای سیستمی MySQL وجود ندارد.', 403);
        }

        try {
            $this->pdo->exec('KILL QUERY ' . $processId);
        } catch (Throwable $e) {
            error_log('DatabaseMonitoringService::killQuery failed: ' . $e->getMessage());
            throw new ApiException('امکان متوقف کردن این Query وجود ندارد (ممکن است از قبل پایان یافته باشد).', 422);
        }
        AuditLogger::log($actor, 'mysql.monitoring.kill_query', 'mysql_process', (string)$processId, []);
    }

    // بهترین تلاش برای خواندن Performance Schema — طبق بند ۲۲ spec «اگر قابل دسترسی باشد»؛ نبود GRANT کافی
    // یا غیرفعال بودن performance_schema نباید کل صفحه‌ی Monitoring را بترکاند
    public function slowQueries(int $limit = 20): array {
        $limit = max(1, min($limit, 100));
        try {
            $stmt = $this->pdo->prepare(
                "SELECT
                    DIGEST_TEXT AS query_text,
                    COUNT_STAR AS exec_count,
                    ROUND(AVG_TIMER_WAIT / 1000000000, 2) AS avg_ms,
                    ROUND(MAX_TIMER_WAIT / 1000000000, 2) AS max_ms,
                    SUM_ROWS_EXAMINED AS rows_examined,
                    SUM_ROWS_SENT AS rows_sent
                FROM performance_schema.events_statements_summary_by_digest
                WHERE DIGEST_TEXT IS NOT NULL
                ORDER BY AVG_TIMER_WAIT DESC
                LIMIT " . $limit
            );
            $stmt->execute();
            return ['available' => true, 'rows' => $stmt->fetchAll()];
        } catch (Throwable $e) {
            return ['available' => false, 'rows' => [], 'message' => 'دسترسی به Performance Schema موجود نیست.'];
        }
    }
}
