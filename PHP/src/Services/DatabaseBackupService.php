<?php
// PHP/src/Services/DatabaseBackupService.php — تولید/تأیید/حذف پشتیبان دیتابیس با PHP+PDO خالص (بدون exec/shell_exec/
// mysqldump، چون وضعیت disable_functions وب‌سرور نامعلوم است). فرمت خروجی dump عمداً «یک statement کامل در هر خط»
// است — این یعنی Restore هرگز نیازی به SQL parser ندارد و فقط با خواندن خط‌به‌خط کار می‌کند (بدون خطر تفسیر غلط
// semicolonِ داخل داده). هیچ SQL Console/Import فایل دلخواه در این پنل وجود ندارد؛ فقط فایل‌هایی که خودِ همین سرویس
// تولید کرده باشد قابل Restore هستند.

declare(strict_types=1);

namespace App\Services;

use App\Core\Database;
use App\Exceptions\ApiException;
use App\Repositories\DatabaseBackupRepository;
use App\Repositories\MySQLMetaRepository;
use PDO;
use Throwable;

class DatabaseBackupService {
    private const ROW_BATCH_SIZE = 500;
    private const ALLOWED_TYPES = ['full', 'tables', 'structure', 'data'];
    private const ALLOWED_RETENTION = [1, 3, 7, 14, 30, 90];
    private const ALLOWED_FREQUENCY = ['daily', 'weekly', 'monthly'];

    // db_backups در حالت «همه‌ی جدول‌ها» (full/structure/data) عمداً کنار گذاشته می‌شود: ردیف خودِ این پشتیبان
    // در لحظه‌ی dump شدن هنوز status='running' دارد (چون هنوز کامل نشده)، پس اگر بعداً همین فایل Restore شود،
    // تاریخچه‌ی واقعی پشتیبان‌ها را با یک لحظه‌ی ناقص از خودش بازنویسی می‌کند. اگر Administrator صریحاً از طریق
    // نوع «جدول‌های منتخب» این جدول را انتخاب کند، همچنان به‌طور کامل پشتیبانی/بازیابی می‌شود.
    private const DEFAULT_EXCLUDED_TABLES = ['db_backups'];

    private PDO $pdo;
    private MySQLMetaRepository $meta;
    private DatabaseBackupRepository $repo;

    public function __construct() {
        $this->pdo = Database::getInstance()->getPdoConnection();
        $this->meta = new MySQLMetaRepository();
        $this->repo = new DatabaseBackupRepository();
    }

    public function storageDir(): string {
        $dir = __DIR__ . '/../../storage/mysql_backups';
        if (!is_dir($dir)) {
            mkdir($dir, 0750, true);
        }
        return $dir;
    }

    public function list(int $page, int $perPage): array {
        return $this->repo->paginated($page, $perPage);
    }

    public function latest(): ?array {
        return $this->repo->latest();
    }

    public function latestAutomatic(): ?array {
        return $this->repo->latestAutomatic();
    }

    // Retention Policy — بند ۴۸ spec: فقط Backupهای خودکارِ موفق/تأییدشده که قدیمی‌تر از N اخیرترین هستند حذف می‌شوند
    public function applyRetention(int $keep, string $actor): int {
        $stale = $this->repo->findAutomaticOlderThanKeep($keep);
        $deleted = 0;
        foreach ($stale as $row) {
            try {
                $this->delete((int)$row['id'], $actor);
                $deleted++;
            } catch (Throwable $e) {
                error_log('DatabaseBackupService::applyRetention failed to delete backup #' . $row['id'] . ': ' . $e->getMessage());
            }
        }
        return $deleted;
    }

    public function find(int $id): array {
        $row = $this->repo->find($id);
        if ($row === null) {
            throw new ApiException('پشتیبان مورد نظر یافت نشد.', 404);
        }
        return $row;
    }

    public function getSettings(): array {
        $row = $this->repo->getSettings();
        if (empty($row)) {
            throw new ApiException('تنظیمات نگهداری یافت نشد. لطفاً migration مربوطه را اجرا کنید.', 500);
        }
        return $row;
    }

    public function saveSettings(array $input, string $actor): array {
        $frequency = (string)($input['auto_backup_frequency'] ?? 'daily');
        if (!in_array($frequency, self::ALLOWED_FREQUENCY, true)) {
            throw new ApiException('بازه‌ی زمان‌بندی نامعتبر است.', 422);
        }
        $retention = (int)($input['auto_backup_retention'] ?? 7);
        if (!in_array($retention, self::ALLOWED_RETENTION, true)) {
            throw new ApiException('مقدار Retention نامعتبر است.', 422);
        }
        $time = (string)($input['auto_backup_time'] ?? '02:00');
        if (!preg_match('/^([01]\d|2[0-3]):([0-5]\d)(:[0-5]\d)?$/', $time)) {
            throw new ApiException('فرمت زمان نامعتبر است.', 422);
        }

        $settings = [
            'auto_backup_enabled' => !empty($input['auto_backup_enabled']),
            'auto_backup_frequency' => $frequency,
            'auto_backup_time' => strlen($time) === 5 ? $time . ':00' : $time,
            'auto_backup_retention' => $retention,
            'auto_backup_compression' => !empty($input['auto_backup_compression']),
            'auto_backup_include_structure' => !empty($input['auto_backup_include_structure']),
            'auto_backup_include_data' => !empty($input['auto_backup_include_data']),
            'auto_analyze_weekly' => !empty($input['auto_analyze_weekly']),
            'auto_optimize_weekly' => !empty($input['auto_optimize_weekly']),
            'auto_cleanup_sessions' => !empty($input['auto_cleanup_sessions']),
        ];

        $this->repo->saveSettings($settings);
        AuditLogger::log($actor, 'mysql.settings.update', 'mysql_maintenance_settings', '1', $settings);

        return $this->getSettings();
    }

    // ایجاد پشتیبان دستی. $tables=null یعنی تمام جدول‌های دیتابیس.
    public function createBackup(string $type, ?array $tables, bool $compress, string $actor, bool $isAutomatic = false): array {
        if (!in_array($type, self::ALLOWED_TYPES, true)) {
            throw new ApiException('نوع پشتیبان نامعتبر است.', 422);
        }
        if ($type === 'tables' && empty($tables)) {
            throw new ApiException('برای نوع «جدول‌های منتخب» باید حداقل یک جدول انتخاب شود.', 422);
        }

        $targetTables = $this->resolveAndValidateTables($tables);
        $includeStructure = in_array($type, ['full', 'tables', 'structure'], true);
        $includeData = in_array($type, ['full', 'tables', 'data'], true);

        $extension = $compress ? '.sql.gz' : '.sql';
        $filename = sprintf('atk_cargo_%s_%s%s', $type, date('Y_m_d_His'), $extension);
        $path = $this->storageDir() . '/' . $filename;

        $id = $this->repo->createRunning($filename, $type, $compress ? 'gzip' : 'none', $tables, $actor, $isAutomatic);
        $startedAt = microtime(true);

        try {
            $handle = $compress ? gzopen($path, 'wb9') : fopen($path, 'wb');
            if ($handle === false) {
                throw new ApiException('امکان ایجاد فایل پشتیبان روی دیسک وجود ندارد.', 500);
            }

            $this->writeLine($handle, $compress, '-- ATK-Cargo MySQL Manager backup');
            $this->writeLine($handle, $compress, '-- Database: ' . $this->meta->databaseName());
            $this->writeLine($handle, $compress, '-- Type: ' . $type);
            $this->writeLine($handle, $compress, '-- Generated: ' . date('Y-m-d H:i:s'));
            $this->writeLine($handle, $compress, 'SET NAMES utf8mb4;');
            $this->writeLine($handle, $compress, 'SET FOREIGN_KEY_CHECKS = 0;');

            foreach ($targetTables as $tableName) {
                if ($includeStructure) {
                    $this->writeStructure($handle, $compress, $tableName);
                }
                if ($includeData) {
                    $this->writeData($handle, $compress, $tableName);
                }
            }

            $this->writeLine($handle, $compress, 'SET FOREIGN_KEY_CHECKS = 1;');
            $compress ? gzclose($handle) : fclose($handle);
        } catch (Throwable $e) {
            if (isset($handle) && $handle !== false) {
                $compress ? @gzclose($handle) : @fclose($handle);
            }
            if (file_exists($path)) {
                @unlink($path);
            }
            $message = $e instanceof ApiException ? $e->getMessage() : 'خطا در تولید فایل پشتیبان.';
            $this->repo->markStatus($id, 'failed', $message);
            error_log('DatabaseBackupService::createBackup failed: ' . $e->getMessage());
            AuditLogger::log($actor, 'mysql.backup.create', 'mysql_backup', (string)$id, ['status' => 'failed']);
            throw new ApiException($message, $e instanceof ApiException ? $e->getStatusCode() : 500);
        }

        $durationMs = (int)round((microtime(true) - $startedAt) * 1000);
        $fileSize = filesize($path) ?: 0;
        $checksum = hash_file('sha256', $path) ?: null;

        [$verifyStatus, $verifyMessage] = $this->verifyFile($path, $compress);
        $finalStatus = $verifyStatus ? 'verified' : 'success';

        $this->repo->markFinished($id, $finalStatus, $fileSize, $checksum, $durationMs, $verifyStatus ? null : $verifyMessage);
        AuditLogger::log($actor, 'mysql.backup.create', 'mysql_backup', (string)$id, [
            'type' => $type,
            'status' => $finalStatus,
            'sizeBytes' => $fileSize,
            'durationMs' => $durationMs,
        ]);

        return [
            'id' => $id,
            'filename' => $filename,
            'status' => $finalStatus,
            'sizeBytes' => $fileSize,
            'checksum' => $checksum,
            'durationMs' => $durationMs,
            'verifyMessage' => $verifyMessage,
        ];
    }

    public function verify(int $id, string $actor): array {
        $row = $this->find($id);
        if ($row['status'] === 'running') {
            throw new ApiException('این پشتیبان هنوز در حال تولید است.', 409);
        }

        $path = $this->filePath($row);
        if (!file_exists($path)) {
            $this->repo->markStatus($id, 'failed', 'فایل پشتیبان روی دیسک یافت نشد.');
            AuditLogger::log($actor, 'mysql.backup.verify', 'mysql_backup', (string)$id, ['status' => 'failed']);
            throw new ApiException('فایل پشتیبان روی دیسک یافت نشد.', 404);
        }

        [$ok, $message] = $this->verifyFile($path, $row['compression'] === 'gzip');
        $status = $ok ? 'verified' : 'failed';
        $this->repo->markStatus($id, $status, $ok ? null : $message);
        AuditLogger::log($actor, 'mysql.backup.verify', 'mysql_backup', (string)$id, ['status' => $status]);

        return ['id' => $id, 'status' => $status, 'message' => $message];
    }

    public function delete(int $id, string $actor): void {
        $row = $this->find($id);
        if ($row['status'] === 'running') {
            throw new ApiException('پشتیبانی که هنوز در حال تولید است قابل حذف نیست.', 409);
        }

        $path = $this->filePath($row);
        if (file_exists($path)) {
            @unlink($path);
        }
        $this->repo->delete($id);
        AuditLogger::log($actor, 'mysql.backup.delete', 'mysql_backup', (string)$id, ['filename' => $row['filename']]);
    }

    // مسیر فیزیکی فایل — basename() به‌عنوان دفاع در عمق، هرچند filename همیشه توسط خودِ این سرویس تولید می‌شود نه ورودی کاربر
    public function filePath(array $backupRow): string {
        return $this->storageDir() . '/' . basename($backupRow['filename']);
    }

    private function resolveAndValidateTables(?array $tables): array {
        if ($tables === null || empty($tables)) {
            $allNames = array_column($this->meta->allTablesRaw(), 'name');
            return array_values(array_diff($allNames, self::DEFAULT_EXCLUDED_TABLES));
        }
        $validated = [];
        foreach ($tables as $tableName) {
            $tableName = (string)$tableName;
            if (!$this->meta->tableExists($tableName)) {
                throw new ApiException("جدول «{$tableName}» در این دیتابیس یافت نشد.", 422);
            }
            $validated[] = $tableName;
        }
        return $validated;
    }

    private function writeStructure($handle, bool $gzip, string $tableName): void {
        $escaped = $this->escapeIdentifier($tableName);
        $stmt = $this->pdo->query("SHOW CREATE TABLE {$escaped}");
        $row = $stmt->fetch();
        if ($row === false || !isset($row['Create Table'])) {
            throw new ApiException("امکان خواندن ساختار جدول «{$tableName}» وجود ندارد.", 500);
        }

        $this->writeLine($handle, $gzip, "DROP TABLE IF EXISTS {$escaped};");
        // خروجی SHOW CREATE TABLE چندخطی است؛ برای رعایت قرارداد «یک statement در هر خط» فایل، به یک خط فشرده می‌شود.
        // نکته‌ی حیاتی: حتماً با modifier «u» — بدون آن، \R در PCRE بایت 0x85 (NEL) را هم خط‌جدید تلقی می‌کند که
        // دقیقاً بایت دوم رمزگذاری UTF-8 برخی حروف فارسی (مثل «م» = 0xD9 0x85) است و بدون /u آن را قطع/خراب می‌کند.
        $createSql = preg_replace('/\s*\R\s*/u', ' ', trim($row['Create Table']));
        $this->writeLine($handle, $gzip, $createSql . ';');
    }

    private function writeData($handle, bool $gzip, string $tableName): void {
        $escaped = $this->escapeIdentifier($tableName);
        $columns = array_column($this->meta->tableColumns($tableName), 'name');
        if (empty($columns)) {
            return;
        }
        $columnList = implode(',', array_map(fn ($c) => $this->escapeIdentifier($c), $columns));

        $offset = 0;
        while (true) {
            $stmt = $this->pdo->prepare("SELECT {$columnList} FROM {$escaped} LIMIT :limit OFFSET :offset");
            $stmt->bindValue(':limit', self::ROW_BATCH_SIZE, PDO::PARAM_INT);
            $stmt->bindValue(':offset', $offset, PDO::PARAM_INT);
            $stmt->execute();
            $rows = $stmt->fetchAll(PDO::FETCH_NUM);
            if (empty($rows)) {
                break;
            }

            $valueGroups = [];
            foreach ($rows as $row) {
                $values = array_map(function ($value) {
                    return $value === null ? 'NULL' : $this->pdo->quote((string)$value);
                }, $row);
                $valueGroups[] = '(' . implode(',', $values) . ')';
            }

            $this->writeLine($handle, $gzip, "INSERT INTO {$escaped} ({$columnList}) VALUES " . implode(',', $valueGroups) . ';');

            if (count($rows) < self::ROW_BATCH_SIZE) {
                break;
            }
            $offset += self::ROW_BATCH_SIZE;
        }
    }

    private function escapeIdentifier(string $name): string {
        return '`' . str_replace('`', '``', $name) . '`';
    }

    private function writeLine($handle, bool $gzip, string $line): void {
        $data = $line . "\n";
        if ($gzip) {
            gzwrite($handle, $data);
        } else {
            fwrite($handle, $data);
        }
    }

    // بررسی سبک صحت فایل — بازکردن کامل (gzip بودن واقعاً معتبر است) + وجود حداقل یک statement اجرایی
    private function verifyFile(string $path, bool $gzip): array {
        $handle = $gzip ? @gzopen($path, 'rb') : @fopen($path, 'rb');
        if ($handle === false) {
            return [false, 'باز کردن فایل پشتیبان ممکن نشد (فایل خراب است؟).'];
        }

        $statementCount = 0;
        try {
            while (($gzip ? !gzeof($handle) : !feof($handle))) {
                $line = $gzip ? gzgets($handle) : fgets($handle);
                if ($line === false) {
                    break;
                }
                $trimmed = trim($line);
                if ($trimmed === '' || str_starts_with($trimmed, '--')) {
                    continue;
                }
                $statementCount++;
            }
        } catch (Throwable $e) {
            $gzip ? @gzclose($handle) : @fclose($handle);
            return [false, 'خواندن فایل پشتیبان با خطا مواجه شد (فایل خراب است؟).'];
        }

        $gzip ? gzclose($handle) : fclose($handle);

        if ($statementCount === 0) {
            return [false, 'فایل پشتیبان هیچ دستور SQL معتبری ندارد.'];
        }

        return [true, "بررسی ساختار موفق بود ({$statementCount} دستور SQL)."];
    }
}
