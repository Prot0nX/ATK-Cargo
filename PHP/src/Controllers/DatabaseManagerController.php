<?php
// PHP/src/Controllers/DatabaseManagerController.php — هماهنگ‌کننده‌ی نازک اکشن‌های پنل MySQL_Manager؛
// منطق واقعی در Repositories/Services است، این کلاس فقط ورودی را اعتبارسنجی و خروجی سرویس‌ها را شکل می‌دهد.

declare(strict_types=1);

namespace App\Controllers;

use App\Repositories\AuditLogRepository;
use App\Repositories\MySQLMetaRepository;
use App\Services\AuditLogger;
use App\Services\DatabaseBackupService;
use App\Services\DatabaseCleanupService;
use App\Services\DatabaseMonitoringService;
use App\Services\DatabaseOptimizationService;
use App\Services\DatabaseRestoreService;
use App\Services\MaintenanceScheduler;

class DatabaseManagerController {
    private MySQLMetaRepository $meta;
    private DatabaseOptimizationService $optimization;
    private DatabaseBackupService $backup;
    private DatabaseRestoreService $restore;
    private DatabaseCleanupService $cleanup;
    private DatabaseMonitoringService $monitoring;
    private AuditLogRepository $auditLog;

    // کلیدهای وضعیت/متغیر سراسری که برای Dashboard/Monitoring لازم است — whitelist صریح تا هیچ‌وقت SHOW با ورودی کاربر ساخته نشود
    private const STATUS_KEYS = [
        'Threads_connected', 'Threads_running', 'Uptime', 'Connections',
        'Questions', 'Slow_queries', 'Aborted_connects', 'Bytes_received', 'Bytes_sent',
    ];
    private const VARIABLE_KEYS = ['version', 'max_connections', 'innodb_buffer_pool_size'];

    // اکشن‌ها/entity_typeهایی که در audit_log مخصوص همین پنل هستند — پیشوند مشترک action برای فیلتر صفحه‌ی Logs
    public const AUDIT_ACTION_PREFIX = 'mysql.';

    public function __construct() {
        $this->meta = new MySQLMetaRepository();
        $this->optimization = new DatabaseOptimizationService();
        $this->backup = new DatabaseBackupService();
        $this->restore = new DatabaseRestoreService();
        $this->cleanup = new DatabaseCleanupService();
        $this->monitoring = new DatabaseMonitoringService();
        $this->auditLog = new AuditLogRepository();
    }

    public function dashboard(): array {
        $tables = $this->meta->allTablesRaw();
        $status = $this->meta->globalStatus(self::STATUS_KEYS);
        $variables = $this->meta->globalVariables(self::VARIABLE_KEYS);
        $tablesWithErrors = $this->optimization->tablesWithErrorsCount();
        $lastBackup = $this->backup->latest();

        $tableCount = count($tables);
        $totalDataSize = 0;
        $totalIndexSize = 0;
        $needsOptimization = 0;

        foreach ($tables as $table) {
            $dataLength = (int)($table['data_length'] ?? 0);
            $indexLength = (int)($table['index_length'] ?? 0);
            $totalDataSize += $dataLength;
            $totalIndexSize += $indexLength;

            $fragmentation = $this->fragmentationPercent($table);
            if ($fragmentation >= MySQLMetaRepository::FRAGMENTATION_MEDIUM) {
                $needsOptimization++;
            }
        }

        return [
            'mysqlVersion' => (string)($variables['version'] ?? $this->meta->mysqlVersion()),
            'phpVersion' => PHP_VERSION,
            'uptimeSeconds' => (int)($status['Uptime'] ?? 0),
            'databaseName' => $this->meta->databaseName(),
            'tableCount' => $tableCount,
            'totalDataSize' => $totalDataSize,
            'totalIndexSize' => $totalIndexSize,
            'totalSize' => $totalDataSize + $totalIndexSize,
            'activeConnections' => (int)($status['Threads_connected'] ?? 0),
            'runningQueries' => (int)($status['Threads_running'] ?? 0),
            'tablesRequiringOptimization' => $needsOptimization,
            'tablesWithErrors' => $tablesWithErrors,
            'lastBackup' => $lastBackup === null ? null : [
                'createdAt' => $lastBackup['created_at'],
                'status' => $lastBackup['status'],
                'ageHours' => round((time() - strtotime($lastBackup['created_at'])) / 3600, 1),
            ],
            'health' => $this->healthScore($tables, $status, $tablesWithErrors, $lastBackup),
        ];
    }

    public function health(): array {
        $tables = $this->meta->allTablesRaw();
        $status = $this->meta->globalStatus(self::STATUS_KEYS);
        $tablesWithErrors = $this->optimization->tablesWithErrorsCount();
        return $this->healthScore($tables, $status, $tablesWithErrors, $this->backup->latest());
    }

    public function optimizationRecommendations(): array {
        return $this->optimization->recommendations();
    }

    public function optimizationHistory(int $limit, string $tableName): array {
        return $this->optimization->history($limit, $tableName);
    }

    public function optimizationPreview(string $tableName): array {
        return $this->optimization->preview($tableName);
    }

    public function runOptimize(string $tableName, string $actor): array {
        return $this->optimization->optimize($tableName, $actor);
    }

    public function runAnalyze(string $tableName, string $actor): array {
        return $this->optimization->analyze($tableName, $actor);
    }

    public function runCheck(string $tableName, string $actor): array {
        return $this->optimization->check($tableName, $actor);
    }

    public function runRepair(string $tableName, string $actor): array {
        return $this->optimization->repair($tableName, $actor);
    }

    // ---------- Backup ----------

    public function backupSettings(): array {
        return $this->backup->getSettings();
    }

    public function saveBackupSettings(array $input, string $actor): array {
        return $this->backup->saveSettings($input, $actor);
    }

    public function backups(int $page, int $perPage): array {
        return $this->backup->list($page, $perPage);
    }

    public function createBackup(string $type, ?array $tables, bool $compress, string $actor): array {
        return $this->backup->createBackup($type, $tables, $compress, $actor);
    }

    public function verifyBackup(int $id, string $actor): array {
        return $this->backup->verify($id, $actor);
    }

    public function deleteBackup(int $id, string $actor): void {
        $this->backup->delete($id, $actor);
    }

    // ---------- Restore ----------

    public function restoreBackup(int $id, string $confirmDbName, string $actor): array {
        return $this->restore->restore($id, $confirmDbName, $actor);
    }

    // ---------- Cleanup ----------

    public function cleanupRules(): array {
        return $this->cleanup->listRules();
    }

    public function saveCleanupRule(?int $id, string $tableName, string $dateColumn, int $retentionDays, bool $enabled, string $actor): array {
        return $this->cleanup->saveRule($id, $tableName, $dateColumn, $retentionDays, $enabled, $actor);
    }

    public function toggleCleanupRule(int $id, bool $enabled, string $actor): array {
        return $this->cleanup->toggleRule($id, $enabled, $actor);
    }

    public function deleteCleanupRule(int $id, string $actor): void {
        $this->cleanup->deleteRule($id, $actor);
    }

    public function cleanupPreview(int $ruleId): array {
        return $this->cleanup->preview($ruleId);
    }

    public function runCleanup(int $ruleId, bool $confirm, string $actor): array {
        return $this->cleanup->run($ruleId, $confirm, $actor);
    }

    // ---------- Monitoring ----------

    public function monitoringMetrics(): array {
        return $this->monitoring->metrics();
    }

    public function processList(): array {
        return $this->monitoring->processList();
    }

    public function killQuery(int $processId, string $actor): void {
        $this->monitoring->killQuery($processId, $actor);
    }

    public function slowQueries(int $limit): array {
        return $this->monitoring->slowQueries($limit);
    }

    // ---------- Scheduler ----------

    public function maintenanceSettings(): array {
        return $this->backup->getSettings();
    }

    public function runMaintenanceNow(string $actor): array {
        $scheduler = new MaintenanceScheduler();
        $result = $scheduler->run();
        AuditLogger::log($actor, 'mysql.scheduler.run_now', 'mysql_maintenance_settings', '1', $result);
        return $result;
    }

    // ---------- Logs / Audit ----------

    public function auditLogs(int $limit, ?int $beforeId, ?string $entityType): array {
        return $this->auditLog->listLogs($limit, $beforeId, null, $entityType, self::AUDIT_ACTION_PREFIX);
    }

    public function tables(int $page, int $perPage, string $search, string $sort, string $dir): array {
        $result = $this->meta->paginatedTables($page, $perPage, $search, $sort, $dir);
        $result['rows'] = array_map(function (array $table): array {
            return $this->decorateTable($table);
        }, $result['rows']);
        return $result;
    }

    public function tableDetail(string $tableName): ?array {
        $detail = $this->meta->tableDetail($tableName);
        if ($detail === null) {
            return null;
        }
        $detail = $this->decorateTable($detail);
        $detail['columns'] = $this->meta->tableColumns($tableName);
        $detail['indexes'] = $this->meta->indexes($tableName);
        return $detail;
    }

    public function indexes(string $tableName = ''): array {
        $rows = $this->meta->indexes($tableName);
        $issues = $this->detectIndexIssues($rows);
        return ['rows' => $rows, 'issues' => $issues];
    }

    public function storage(): array {
        $tables = $this->meta->allTablesRaw();
        usort($tables, function (array $a, array $b): int {
            $sizeA = (int)($a['data_length'] ?? 0) + (int)($a['index_length'] ?? 0);
            $sizeB = (int)($b['data_length'] ?? 0) + (int)($b['index_length'] ?? 0);
            return $sizeB <=> $sizeA;
        });

        $topTables = array_slice(array_map(fn (array $t) => $this->decorateTable($t), $tables), 0, 10);
        $topIndexes = $this->topIndexesBySize();

        $totalData = array_sum(array_column($tables, 'data_length'));
        $totalIndex = array_sum(array_column($tables, 'index_length'));
        $totalFree = array_sum(array_column($tables, 'data_free'));

        return [
            'totalDataSize' => (int)$totalData,
            'totalIndexSize' => (int)$totalIndex,
            'totalFreeSpace' => (int)$totalFree,
            'topTables' => $topTables,
            'topIndexes' => $topIndexes,
        ];
    }

    private function topIndexesBySize(): array {
        // MySQL آمار اندازه‌ی هر ایندکس را جداگانه در information_schema نمی‌دهد؛ index_length کل جدول را
        // به‌عنوان تقریب برای مرتب‌سازی «بزرگ‌ترین ایندکس‌ها بر اساس جدول» استفاده می‌کنیم و صادقانه به کاربر می‌گوییم.
        $tables = $this->meta->allTablesRaw();
        usort($tables, fn (array $a, array $b) => (int)($b['index_length'] ?? 0) <=> (int)($a['index_length'] ?? 0));
        return array_slice(array_map(function (array $t): array {
            return [
                'tableName' => $t['name'],
                'indexSize' => (int)($t['index_length'] ?? 0),
            ];
        }, $tables), 0, 10);
    }

    private function detectIndexIssues(array $rows): array {
        $issues = [];
        $byTable = [];
        foreach ($rows as $row) {
            $byTable[$row['table_name']][] = $row;
        }
        foreach ($byTable as $tableName => $tableIndexes) {
            $indexNames = [];
            foreach ($tableIndexes as $idx) {
                $indexNames[$idx['index_name']][] = $idx['column_name'];
            }
            // شناسایی ایندکس‌های تکراری: دو ایندکس با دقیقاً همان مجموعه/ترتیب ستون‌ها
            $seen = [];
            foreach ($indexNames as $name => $columns) {
                $signature = implode(',', $columns);
                if (isset($seen[$signature]) && $name !== 'PRIMARY') {
                    $issues[] = [
                        'type' => 'duplicate',
                        'tableName' => $tableName,
                        'message' => "ایندکس «{$name}» با ایندکس «{$seen[$signature]}» روی همان ستون‌ها تکراری به‌نظر می‌رسد.",
                    ];
                } else {
                    $seen[$signature] = $name;
                }
            }
        }
        return $issues;
    }

    private function decorateTable(array $table): array {
        $dataLength = (int)($table['data_length'] ?? 0);
        $indexLength = (int)($table['index_length'] ?? 0);
        $dataFree = (int)($table['data_free'] ?? 0);

        $table['data_length'] = $dataLength;
        $table['index_length'] = $indexLength;
        $table['data_free'] = $dataFree;
        $table['total_size'] = $dataLength + $indexLength;
        $table['fragmentation_percent'] = $this->fragmentationPercent($table);
        $table['fragmentation_level'] = $this->fragmentationLevel($table['fragmentation_percent']);
        $table['repair_supported'] = in_array($table['engine'] ?? '', ['MyISAM', 'ARCHIVE', 'CSV'], true);
        return $table;
    }

    private function fragmentationPercent(array $table): float {
        $dataLength = (int)($table['data_length'] ?? 0);
        $indexLength = (int)($table['index_length'] ?? 0);
        $dataFree = (int)($table['data_free'] ?? 0);
        $total = $dataLength + $indexLength;
        if ($total <= 0) {
            return 0.0;
        }
        return round(($dataFree / $total) * 100, 2);
    }

    private function fragmentationLevel(float $percent): string {
        if ($percent >= MySQLMetaRepository::FRAGMENTATION_HIGH) {
            return 'high';
        }
        if ($percent >= MySQLMetaRepository::FRAGMENTATION_MEDIUM) {
            return 'medium';
        }
        return 'low';
    }

    // امتیاز سلامت دیتابیس ۰-۱۰۰ — بند ۴ spec: ترکیب وضعیت fragmentation، خطاهای CHECK TABLE، وضعیت Backup و اتصال
    private function healthScore(array $tables, array $status, int $tablesWithErrors = 0, ?array $lastBackup = null): array {
        $score = 100;
        $reasons = [];

        if ($tablesWithErrors > 0) {
            $score -= min(30, $tablesWithErrors * 10);
            $reasons[] = "{$tablesWithErrors} جدول در آخرین بررسی (CHECK TABLE) دارای خطا بوده است.";
        }

        if ($lastBackup === null) {
            $score -= 15;
            $reasons[] = 'تاکنون هیچ پشتیبان موفقی ثبت نشده است.';
        } else {
            $ageHours = (time() - strtotime($lastBackup['created_at'])) / 3600;
            if ($ageHours > 48) {
                $score -= 15;
                $reasons[] = 'آخرین پشتیبان بیش از ۴۸ ساعت قدمت دارد.';
            } elseif ($ageHours > 24) {
                $score -= 7;
                $reasons[] = 'آخرین پشتیبان بیش از ۲۴ ساعت قدمت دارد.';
            }
        }

        $tableCount = count($tables);
        if ($tableCount > 0) {
            $fragSum = 0.0;
            $highFragCount = 0;
            foreach ($tables as $table) {
                $frag = $this->fragmentationPercent($table);
                $fragSum += $frag;
                if ($frag >= MySQLMetaRepository::FRAGMENTATION_HIGH) {
                    $highFragCount++;
                }
            }
            $avgFrag = $fragSum / $tableCount;
            if ($avgFrag >= MySQLMetaRepository::FRAGMENTATION_HIGH) {
                $score -= 25;
                $reasons[] = 'میانگین فضای هدررفته بالا است.';
            } elseif ($avgFrag >= MySQLMetaRepository::FRAGMENTATION_MEDIUM) {
                $score -= 10;
                $reasons[] = 'برخی جدول‌ها فضای هدررفته دارند.';
            }
            if ($highFragCount > 0) {
                $score -= min(15, $highFragCount * 3);
                $reasons[] = "{$highFragCount} جدول نیازمند بهینه‌سازی فوری است.";
            }
        }

        if (empty($status)) {
            $score -= 5;
            $reasons[] = 'دسترسی به آمار وضعیت سرور محدود است.';
        }

        $score = max(0, min(100, $score));
        $label = 'عالی';
        if ($score < 50) {
            $label = 'ضعیف';
        } elseif ($score < 75) {
            $label = 'متوسط';
        } elseif ($score < 90) {
            $label = 'خوب';
        }

        return ['score' => $score, 'label' => $label, 'reasons' => $reasons];
    }
}
