<?php
// PHP/src/Services/MaintenanceScheduler.php — منطق مشترک نگهداری خودکار (Backup/Analyze/Optimize/Cleanup) برای
// پنل MySQL_Manager (فاز ۴). دو راه اجرا دارد (طبق بند ۲۳ spec): از طریق CLI (PHP/cron/mysql_manager_cron.php،
// برای سرورهایی که به cron دسترسی دارند) یا از طریق endpoint وب امن با احراز هویت کامل (اکشن runMaintenanceNow
// در api.php، برای اجرای دستی/تست بدون نیاز به دسترسی shell).
//
// این کلاس خودش هیچ Daemon/Timerی ندارد — صرفاً هر بار که صدا زده شود، بررسی می‌کند «آیا الان نوبت هرکدام از
// وظایف فعال‌شده رسیده؟» و در صورت رسیدن نوبت، همان‌جا اجرا می‌کند. این یعنی باید طبق راهنمای بالای همین فایل،
// یا از cron سیستم (هر ۱۵-۳۰ دقیقه) یا با کلیک دستی «اجرای اکنون» در پنل صدا زده شود.

declare(strict_types=1);

namespace App\Services;

use App\Repositories\DatabaseBackupRepository;
use App\Repositories\MySQLMetaRepository;
use DateTimeImmutable;
use Throwable;

class MaintenanceScheduler {
    // نامی که در audit_log و db_optimization_history/db_backups.created_by به‌عنوان عامل خودکار ثبت می‌شود
    public const SCHEDULER_ACTOR = 'mysql_manager_scheduler';

    private DatabaseBackupService $backupService;
    private DatabaseBackupRepository $backupRepo;
    private DatabaseOptimizationService $optimizationService;
    private DatabaseCleanupService $cleanupService;
    private MySQLMetaRepository $meta;

    public function __construct() {
        $this->backupService = new DatabaseBackupService();
        $this->backupRepo = new DatabaseBackupRepository();
        $this->optimizationService = new DatabaseOptimizationService();
        $this->cleanupService = new DatabaseCleanupService();
        $this->meta = new MySQLMetaRepository();
    }

    public function run(): array {
        $settings = $this->backupRepo->getSettings();
        $result = [
            'checkedAt' => date('Y-m-d H:i:s'),
            'backup' => null,
            'analyze' => null,
            'optimize' => null,
            'cleanup' => null,
        ];

        if (empty($settings)) {
            $result['error'] = 'تنظیمات نگهداری یافت نشد.';
            return $result;
        }

        if (!empty($settings['auto_backup_enabled']) && $this->isBackupDue($settings)) {
            $result['backup'] = $this->runAutomaticBackup($settings);
        }

        if (!empty($settings['auto_analyze_weekly']) && $this->isWeeklyTaskDue('analyze')) {
            $result['analyze'] = $this->runOnAllTables('analyze');
        }

        if (!empty($settings['auto_optimize_weekly']) && $this->isWeeklyTaskDue('optimize')) {
            $result['optimize'] = $this->runOnAllTables('optimize');
        }

        if (!empty($settings['auto_cleanup_sessions'])) {
            $result['cleanup'] = $this->runAllEnabledCleanupRules();
        }

        return $result;
    }

    private function isBackupDue(array $settings): bool {
        $now = new DateTimeImmutable();
        $timeOfDay = (string)$settings['auto_backup_time'];
        $todayScheduled = new DateTimeImmutable($now->format('Y-m-d') . ' ' . $timeOfDay);
        // پیش از رسیدن ساعت مقرر امروز، هنوز نوبت نیست (حتی اگر از نظر فاصله‌ی روز/هفته/ماه موعد رسیده باشد)
        if ($now < $todayScheduled) {
            return false;
        }

        $latest = $this->backupRepo->latestAutomatic();
        if ($latest === null) {
            return true;
        }
        $lastRun = new DateTimeImmutable($latest['created_at']);

        switch ($settings['auto_backup_frequency']) {
            case 'daily':
                return $lastRun->format('Y-m-d') < $now->format('Y-m-d');
            case 'weekly':
                return $lastRun <= $now->modify('-7 days');
            case 'monthly':
                return $lastRun <= $now->modify('-1 month');
            default:
                return false;
        }
    }

    private function runAutomaticBackup(array $settings): array {
        $type = $settings['auto_backup_include_structure'] && $settings['auto_backup_include_data']
            ? 'full'
            : ($settings['auto_backup_include_structure'] ? 'structure' : 'data');

        try {
            $backup = $this->backupService->createBackup($type, null, (bool)$settings['auto_backup_compression'], self::SCHEDULER_ACTOR, true);
            $deletedByRetention = $this->backupService->applyRetention((int)$settings['auto_backup_retention'], self::SCHEDULER_ACTOR);
            return ['status' => 'ok', 'backupId' => $backup['id'], 'deletedByRetention' => $deletedByRetention];
        } catch (Throwable $e) {
            error_log('MaintenanceScheduler::runAutomaticBackup failed: ' . $e->getMessage());
            return ['status' => 'error', 'message' => 'پشتیبان‌گیری خودکار با خطا مواجه شد.'];
        }
    }

    private function isWeeklyTaskDue(string $operation): bool {
        $lastRunAt = $this->optimizationService->lastRunAt($operation, self::SCHEDULER_ACTOR);
        if ($lastRunAt === null) {
            return true;
        }
        return (new DateTimeImmutable($lastRunAt)) <= (new DateTimeImmutable())->modify('-7 days');
    }

    private function runOnAllTables(string $operation): array {
        $tables = array_column($this->meta->allTablesRaw(), 'name');
        $succeeded = 0;
        $failed = 0;
        foreach ($tables as $tableName) {
            try {
                if ($operation === 'analyze') {
                    $this->optimizationService->analyze($tableName, self::SCHEDULER_ACTOR);
                } else {
                    $this->optimizationService->optimize($tableName, self::SCHEDULER_ACTOR);
                }
                $succeeded++;
            } catch (Throwable $e) {
                $failed++;
                error_log("MaintenanceScheduler::runOnAllTables({$operation}) failed for {$tableName}: " . $e->getMessage());
            }
        }
        return ['status' => 'ok', 'tableCount' => count($tables), 'succeeded' => $succeeded, 'failed' => $failed];
    }

    // «Cleanup Expired Sessions» در بند ۴۶ spec — به‌جای فرض کردن ساختار جدول‌های اصلی برنامه (که این سرویس
    // نمی‌شناسد)، این گزینه یعنی «همه‌ی Ruleهای پاکسازی فعال را اجرا کن»؛ رفتار کاملاً داده‌محور و امن است.
    private function runAllEnabledCleanupRules(): array {
        $rules = array_filter($this->cleanupService->listRules(), fn (array $r) => !empty($r['is_enabled']));
        $totalDeleted = 0;
        $ruleResults = [];
        foreach ($rules as $rule) {
            try {
                $result = $this->cleanupService->run((int)$rule['id'], true, self::SCHEDULER_ACTOR);
                $totalDeleted += $result['deletedCount'];
                $ruleResults[] = ['tableName' => $rule['table_name'], 'deletedCount' => $result['deletedCount']];
            } catch (Throwable $e) {
                error_log('MaintenanceScheduler::runAllEnabledCleanupRules failed for rule #' . $rule['id'] . ': ' . $e->getMessage());
            }
        }
        return ['status' => 'ok', 'ruleCount' => count($rules), 'totalDeleted' => $totalDeleted, 'rules' => $ruleResults];
    }
}
