<?php
// PHP/src/Services/DatabaseCleanupService.php — پاکسازی داده‌های قدیمی صرفاً بر اساس Ruleهای صریح و فعال‌شده
// توسط Administrator. هیچ جدول یا داده‌ای بدون یک ردیف db_cleanup_rules از پیش تعریف‌شده حذف نمی‌شود — این یک
// قانون امنیتی سخت است، نه صرفاً یک پیش‌فرض UI (بند ۱۷ spec).

declare(strict_types=1);

namespace App\Services;

use App\Core\Database;
use App\Exceptions\ApiException;
use App\Repositories\MySQLMetaRepository;
use PDO;
use Throwable;

class DatabaseCleanupService {
    // سقف ایمنی روی هر اجرا تا یک Rule با retention_days اشتباه کل جدول را یک‌جا قفل نکند
    private const BATCH_SIZE = 1000;
    private const MAX_BATCHES_PER_RUN = 200;

    private PDO $pdo;
    private MySQLMetaRepository $meta;

    public function __construct() {
        $this->pdo = Database::getInstance()->getPdoConnection();
        $this->meta = new MySQLMetaRepository();
    }

    public function listRules(): array {
        $stmt = $this->pdo->prepare('SELECT * FROM db_cleanup_rules ORDER BY table_name ASC');
        $stmt->execute();
        return $stmt->fetchAll();
    }

    public function saveRule(?int $id, string $tableName, string $dateColumn, int $retentionDays, bool $enabled, string $actor): array {
        if (!$this->meta->tableExists($tableName)) {
            throw new ApiException("جدول «{$tableName}» در این دیتابیس یافت نشد.", 422);
        }
        $columns = array_column($this->meta->tableColumns($tableName), 'name');
        if (!in_array($dateColumn, $columns, true)) {
            throw new ApiException("ستون «{$dateColumn}» در جدول «{$tableName}» یافت نشد.", 422);
        }
        if ($retentionDays < 1) {
            throw new ApiException('تعداد روزهای نگهداری باید حداقل ۱ باشد.', 422);
        }

        if ($id === null) {
            $stmt = $this->pdo->prepare(
                'INSERT INTO db_cleanup_rules (table_name, date_column, retention_days, is_enabled)
                 VALUES (:table_name, :date_column, :retention_days, :enabled)
                 ON DUPLICATE KEY UPDATE date_column = VALUES(date_column), retention_days = VALUES(retention_days), is_enabled = VALUES(is_enabled)'
            );
            $stmt->execute([
                ':table_name' => $tableName,
                ':date_column' => $dateColumn,
                ':retention_days' => $retentionDays,
                ':enabled' => $enabled ? 1 : 0,
            ]);
        } else {
            $stmt = $this->pdo->prepare(
                'UPDATE db_cleanup_rules SET date_column = :date_column, retention_days = :retention_days, is_enabled = :enabled WHERE id = :id'
            );
            $stmt->execute([
                ':date_column' => $dateColumn,
                ':retention_days' => $retentionDays,
                ':enabled' => $enabled ? 1 : 0,
                ':id' => $id,
            ]);
        }

        AuditLogger::log($actor, 'mysql.cleanup.rule_save', 'mysql_cleanup_rule', $tableName, [
            'dateColumn' => $dateColumn,
            'retentionDays' => $retentionDays,
            'enabled' => $enabled,
        ]);

        return $this->findRuleByTable($tableName);
    }

    public function toggleRule(int $id, bool $enabled, string $actor): array {
        $rule = $this->findRule($id);
        $stmt = $this->pdo->prepare('UPDATE db_cleanup_rules SET is_enabled = :enabled WHERE id = :id');
        $stmt->execute([':enabled' => $enabled ? 1 : 0, ':id' => $id]);
        AuditLogger::log($actor, 'mysql.cleanup.rule_toggle', 'mysql_cleanup_rule', $rule['table_name'], ['enabled' => $enabled]);
        return $this->findRule($id);
    }

    public function deleteRule(int $id, string $actor): void {
        $rule = $this->findRule($id);
        $stmt = $this->pdo->prepare('DELETE FROM db_cleanup_rules WHERE id = :id');
        $stmt->execute([':id' => $id]);
        AuditLogger::log($actor, 'mysql.cleanup.rule_delete', 'mysql_cleanup_rule', $rule['table_name'], []);
    }

    // Dry Run — بند ۳۵ spec: تعداد رکورد و تخمین حجم قبل از حذف واقعی نمایش داده شود
    public function preview(int $ruleId): array {
        $rule = $this->findRule($ruleId);
        [$countSql, $params] = $this->buildMatchQuery($rule, 'COUNT(*) AS cnt');
        $stmt = $this->pdo->prepare($countSql);
        $stmt->execute($params);
        $count = (int)($stmt->fetch()['cnt'] ?? 0);

        $avgRowLength = $this->averageRowLength($rule['table_name']);
        $estimatedBytes = (int)round($count * $avgRowLength);

        return [
            'ruleId' => $ruleId,
            'tableName' => $rule['table_name'],
            'matchingRecords' => $count,
            'estimatedSizeBytes' => $estimatedBytes,
        ];
    }

    // اجرای واقعی حذف — پارامتر confirm صریح در سطح API هم بررسی می‌شود، نه فقط در UI (دفاع در عمق برای عملیات مخرب)
    public function run(int $ruleId, bool $confirm, string $actor): array {
        if (!$confirm) {
            throw new ApiException('برای اجرای پاکسازی باید تأیید صریح ارسال شود.', 422);
        }

        $rule = $this->findRule($ruleId);
        if (!$rule['is_enabled']) {
            throw new ApiException('این Rule غیرفعال است. ابتدا آن را فعال کنید.', 422);
        }

        $escapedTable = $this->escapeIdentifier($rule['table_name']);
        $escapedColumn = $this->escapeIdentifier($rule['date_column']);
        $deleteSql = "DELETE FROM {$escapedTable} WHERE {$escapedColumn} < :cutoff LIMIT " . self::BATCH_SIZE;
        $cutoff = date('Y-m-d H:i:s', strtotime('-' . (int)$rule['retention_days'] . ' days'));

        $totalDeleted = 0;
        $batches = 0;
        try {
            do {
                $stmt = $this->pdo->prepare($deleteSql);
                $stmt->execute([':cutoff' => $cutoff]);
                $affected = $stmt->rowCount();
                $totalDeleted += $affected;
                $batches++;
            } while ($affected === self::BATCH_SIZE && $batches < self::MAX_BATCHES_PER_RUN);
        } catch (Throwable $e) {
            error_log('DatabaseCleanupService::run failed: ' . $e->getMessage());
            throw new ApiException('اجرای پاکسازی با خطا مواجه شد.', 500);
        }

        $stmt = $this->pdo->prepare('UPDATE db_cleanup_rules SET last_run_at = NOW(), last_run_deleted_count = :count WHERE id = :id');
        $stmt->execute([':count' => $totalDeleted, ':id' => $ruleId]);

        $hitSafetyCap = $batches >= self::MAX_BATCHES_PER_RUN;
        AuditLogger::log($actor, 'mysql.cleanup.run', 'mysql_cleanup_rule', $rule['table_name'], [
            'deletedCount' => $totalDeleted,
            'retentionDays' => $rule['retention_days'],
            'hitSafetyCap' => $hitSafetyCap,
        ]);

        return [
            'ruleId' => $ruleId,
            'tableName' => $rule['table_name'],
            'deletedCount' => $totalDeleted,
            'hitSafetyCap' => $hitSafetyCap,
        ];
    }

    private function buildMatchQuery(array $rule, string $selectExpr): array {
        $escapedTable = $this->escapeIdentifier($rule['table_name']);
        $escapedColumn = $this->escapeIdentifier($rule['date_column']);
        $cutoff = date('Y-m-d H:i:s', strtotime('-' . (int)$rule['retention_days'] . ' days'));
        return ["SELECT {$selectExpr} FROM {$escapedTable} WHERE {$escapedColumn} < :cutoff", [':cutoff' => $cutoff]];
    }

    private function averageRowLength(string $tableName): float {
        $detail = $this->meta->tableDetail($tableName);
        if ($detail === null || (int)($detail['rows'] ?? 0) <= 0) {
            return 0.0;
        }
        return ((int)$detail['data_length']) / max(1, (int)$detail['rows']);
    }

    private function findRule(int $id): array {
        $stmt = $this->pdo->prepare('SELECT * FROM db_cleanup_rules WHERE id = :id');
        $stmt->execute([':id' => $id]);
        $row = $stmt->fetch();
        if ($row === false) {
            throw new ApiException('Rule مورد نظر یافت نشد.', 404);
        }
        return $row;
    }

    private function findRuleByTable(string $tableName): array {
        $stmt = $this->pdo->prepare('SELECT * FROM db_cleanup_rules WHERE table_name = :table_name');
        $stmt->execute([':table_name' => $tableName]);
        $row = $stmt->fetch();
        if ($row === false) {
            throw new ApiException('Rule مورد نظر یافت نشد.', 404);
        }
        return $row;
    }

    private function escapeIdentifier(string $name): string {
        return '`' . str_replace('`', '``', $name) . '`';
    }
}
