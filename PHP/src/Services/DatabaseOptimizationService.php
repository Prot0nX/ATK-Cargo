<?php
// PHP/src/Services/DatabaseOptimizationService.php — اجرای OPTIMIZE/ANALYZE/CHECK/REPAIR TABLE برای پنل MySQL_Manager
// (فاز ۲). قانون امنیتی مهم: نام جدول هرگز مستقیم از ورودی کاربر داخل SQL درج نمی‌شود؛ ابتدا با
// MySQLMetaRepository::tableExists() در برابر لیست واقعی جدول‌های دیتابیس تأیید (whitelist) می‌شود،
// چون PDO نمی‌تواند شناسه‌ها (identifier) را parameterize کند — این یعنی تنها راه امن، تأیید از قبل است.

declare(strict_types=1);

namespace App\Services;

use App\Core\DatabaseManager;
use App\Exceptions\ApiException;
use App\Repositories\MySQLMetaRepository;
use PDOException;
use Throwable;

class DatabaseOptimizationService {
    private const REPAIR_SUPPORTED_ENGINES = ['MyISAM', 'ARCHIVE', 'CSV'];

    private DatabaseManager $db;
    private MySQLMetaRepository $meta;

    public function __construct() {
        $this->db = new DatabaseManager();
        $this->meta = new MySQLMetaRepository();
    }

    public function optimize(string $tableName, string $actor): array {
        return $this->run($tableName, 'optimize', 'OPTIMIZE TABLE', $actor);
    }

    public function analyze(string $tableName, string $actor): array {
        return $this->run($tableName, 'analyze', 'ANALYZE TABLE', $actor);
    }

    public function check(string $tableName, string $actor): array {
        return $this->run($tableName, 'check', 'CHECK TABLE', $actor);
    }

    public function repair(string $tableName, string $actor): array {
        $table = $this->assertTableExists($tableName);
        $engine = (string)($table['engine'] ?? '');
        if (!in_array($engine, self::REPAIR_SUPPORTED_ENGINES, true)) {
            throw new ApiException(
                "این جدول با موتور «{$engine}» از REPAIR TABLE پشتیبانی نمی‌کند.",
                422
            );
        }
        return $this->run($tableName, 'repair', 'REPAIR TABLE', $actor);
    }

    // پیش‌نمایش پیش از اجرای عملیات سنگین — بند ۴۳ spec: Engine/حجم/تخمین فضای قابل‌بازیابی/ریسک قفل
    public function preview(string $tableName): array {
        $table = $this->assertTableExists($tableName);
        $dataLength = (int)($table['data_length'] ?? 0);
        $indexLength = (int)($table['index_length'] ?? 0);
        $dataFree = (int)($table['data_free'] ?? 0);
        $total = $dataLength + $indexLength;
        $engine = (string)($table['engine'] ?? '');

        return [
            'tableName' => $table['name'],
            'engine' => $engine,
            'rows' => (int)($table['rows'] ?? 0),
            'totalSize' => $total,
            'reclaimableSpace' => $dataFree,
            // InnoDB با OPTIMIZE TABLE عملاً rebuild کامل می‌شود (کپی کل جدول)؛ برای جدول‌های بزرگ می‌تواند طولانی/سنگین باشد
            'lockRisk' => $engine === 'InnoDB' ? 'medium' : 'high',
            'repairSupported' => in_array($engine, self::REPAIR_SUPPORTED_ENGINES, true),
        ];
    }

    // آخرین N عملیات ثبت‌شده — برای نمایش تاریخچه در صفحه‌ی Optimization
    public function history(int $limit = 30, string $tableName = ''): array {
        $limit = max(1, min($limit, 200));
        $where = '';
        $params = [];
        if ($tableName !== '') {
            $where = 'WHERE table_name = :table';
            $params[':table'] = $tableName;
        }
        $stmt = $this->db->prepare(
            "SELECT id, table_name, operation, status, message, size_before_bytes, size_after_bytes,
                    duration_ms, performed_by, created_at
             FROM db_optimization_history
             {$where}
             ORDER BY created_at DESC, id DESC
             LIMIT " . (int)$limit
        );
        $stmt->execute($params);
        return $stmt->fetchAll();
    }

    // تعداد جدول‌هایی که آخرین CHECK TABLE ثبت‌شده‌شان با وضعیت غیر «ok» بوده — برای کارت «Tables With
    // Errors» در داشبورد و کسر امتیاز Health Score. جدولی که هرگز CHECK نشده، خطا محسوب نمی‌شود.
    // آخرین باری که یک عملیات مشخص توسط یک actor مشخص (مثلاً زمان‌بند خودکار) روی هر جدولی اجرا شده — برای
    // تشخیص «آیا نوبت اجرای هفتگی رسیده؟» در MaintenanceScheduler
    public function lastRunAt(string $operation, string $actor): ?string {
        $stmt = $this->db->prepare(
            'SELECT MAX(created_at) AS last_run FROM db_optimization_history WHERE operation = :operation AND performed_by = :actor'
        );
        $stmt->execute([':operation' => $operation, ':actor' => $actor]);
        $value = $stmt->fetch()['last_run'] ?? null;
        return $value === null ? null : (string)$value;
    }

    public function tablesWithErrorsCount(): int {
        try {
            $stmt = $this->db->prepare(
                "SELECT COUNT(*) AS cnt FROM (
                    SELECT status,
                           ROW_NUMBER() OVER (PARTITION BY table_name ORDER BY created_at DESC, id DESC) AS rn
                    FROM db_optimization_history
                    WHERE operation = 'check'
                ) latest
                WHERE latest.rn = 1 AND latest.status <> 'ok'"
            );
            $stmt->execute();
            return (int)($stmt->fetch()['cnt'] ?? 0);
        } catch (Throwable $e) {
            // جدول db_optimization_history ممکن است هنوز روی این محیط migrate نشده باشد؛ نباید داشبورد را بترکاند
            error_log('DatabaseOptimizationService::tablesWithErrorsCount failed: ' . $e->getMessage());
            return 0;
        }
    }

    // توصیه‌های هوشمند بر اساس fragmentation فعلی — بند ۱۱ spec؛ صرفاً پیشنهاد، هیچ عملیاتی خودکار اجرا نمی‌شود
    public function recommendations(): array {
        $tables = $this->meta->allTablesRaw();
        $result = [];
        foreach ($tables as $table) {
            $dataLength = (int)($table['data_length'] ?? 0);
            $indexLength = (int)($table['index_length'] ?? 0);
            $dataFree = (int)($table['data_free'] ?? 0);
            $total = $dataLength + $indexLength;
            $percent = $total > 0 ? round(($dataFree / $total) * 100, 2) : 0.0;

            if ($percent >= MySQLMetaRepository::FRAGMENTATION_HIGH) {
                $result[] = [
                    'severity' => 'high',
                    'tableName' => $table['name'],
                    'reason' => 'فضای هدررفته‌ی بالا شناسایی شد (' . $percent . '٪).',
                ];
            } elseif ($percent >= MySQLMetaRepository::FRAGMENTATION_MEDIUM) {
                $result[] = [
                    'severity' => 'medium',
                    'tableName' => $table['name'],
                    'reason' => 'فضای بلااستفاده‌ی قابل‌توجهی در این جدول وجود دارد.',
                ];
            } elseif ((int)($table['rows'] ?? 0) > 0) {
                $result[] = [
                    'severity' => 'low',
                    'tableName' => $table['name'],
                    'reason' => 'به‌روزرسانی آمار (ANALYZE) توصیه می‌شود.',
                ];
            }
        }

        $order = ['high' => 0, 'medium' => 1, 'low' => 2];
        usort($result, fn (array $a, array $b) => $order[$a['severity']] <=> $order[$b['severity']]);
        return $result;
    }

    private function assertTableExists(string $tableName): array {
        if (!$this->meta->tableExists($tableName)) {
            throw new ApiException('جدول مورد نظر در این دیتابیس یافت نشد.', 404);
        }
        $detail = $this->meta->tableDetail($tableName);
        if ($detail === null) {
            throw new ApiException('جدول مورد نظر در این دیتابیس یافت نشد.', 404);
        }
        return $detail;
    }

    private function run(string $tableName, string $operation, string $sqlVerb, string $actor): array {
        $before = $this->assertTableExists($tableName);
        $sizeBefore = (int)($before['data_length'] ?? 0) + (int)($before['index_length'] ?? 0);

        $escapedName = '`' . str_replace('`', '``', $tableName) . '`';
        $startedAt = microtime(true);

        try {
            $stmt = $this->db->prepare("{$sqlVerb} {$escapedName}");
            $stmt->execute();
            $rows = $stmt->fetchAll();
        } catch (PDOException $e) {
            error_log("DatabaseOptimizationService::run({$operation}) failed: " . $e->getMessage());
            throw new ApiException('اجرای عملیات روی پایگاه داده با خطا مواجه شد.', 500);
        }

        $durationMs = (int)round((microtime(true) - $startedAt) * 1000);
        [$status, $message] = $this->interpretResultRows($rows);

        $after = $this->meta->tableDetail($tableName);
        $sizeAfter = $after !== null
            ? (int)($after['data_length'] ?? 0) + (int)($after['index_length'] ?? 0)
            : null;

        $this->recordHistory($tableName, $operation, $status, $message, $sizeBefore, $sizeAfter, $durationMs, $actor);
        AuditLogger::log($actor, "mysql.{$operation}.table", 'mysql_table', $tableName, [
            'status' => $status,
            'durationMs' => $durationMs,
            'sizeBefore' => $sizeBefore,
            'sizeAfter' => $sizeAfter,
        ]);

        return [
            'tableName' => $tableName,
            'operation' => $operation,
            'status' => $status,
            'message' => $message,
            'sizeBefore' => $sizeBefore,
            'sizeAfter' => $sizeAfter,
            'reclaimedBytes' => $sizeAfter !== null ? max(0, $sizeBefore - $sizeAfter) : null,
            'durationMs' => $durationMs,
        ];
    }

    // ردیف‌های OPTIMIZE/ANALYZE/CHECK/REPAIR شامل ستون‌های Msg_type/Msg_text هستند؛ هر خطای واقعی
    // باعث می‌شود کل عملیات «error» گزارش شود، هشدار باعث «warning»، در غیر این صورت «ok»
    private function interpretResultRows(array $rows): array {
        if (empty($rows)) {
            return ['ok', 'عملیات بدون پیام اضافی انجام شد.'];
        }

        $status = 'ok';
        $messages = [];
        foreach ($rows as $row) {
            $msgType = strtolower((string)($row['Msg_type'] ?? ''));
            $msgText = (string)($row['Msg_text'] ?? '');
            $messages[] = trim(($row['Msg_type'] ?? '') . ': ' . $msgText);

            if ($msgType === 'error') {
                $status = 'error';
            } elseif ($msgType === 'warning' && $status !== 'error') {
                $status = 'warning';
            }
        }

        return [$status, implode(' | ', $messages)];
    }

    private function recordHistory(
        string $tableName,
        string $operation,
        string $status,
        string $message,
        int $sizeBefore,
        ?int $sizeAfter,
        int $durationMs,
        string $actor
    ): void {
        try {
            $stmt = $this->db->prepare(
                'INSERT INTO db_optimization_history
                    (table_name, operation, status, message, size_before_bytes, size_after_bytes, duration_ms, performed_by)
                 VALUES (:table_name, :operation, :status, :message, :size_before, :size_after, :duration_ms, :performed_by)'
            );
            $stmt->execute([
                ':table_name' => $tableName,
                ':operation' => $operation,
                ':status' => $status,
                ':message' => $message,
                ':size_before' => $sizeBefore,
                ':size_after' => $sizeAfter,
                ':duration_ms' => $durationMs,
                ':performed_by' => $actor,
            ]);
        } catch (Throwable $e) {
            // ثبت تاریخچه best-effort است — شکست آن نباید نتیجه‌ی واقعی عملیات را از کاربر پنهان کند
            error_log('DatabaseOptimizationService::recordHistory failed: ' . $e->getMessage());
        }
    }
}
