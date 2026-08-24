<?php
// PHP/src/Services/DatabaseRestoreService.php — بازیابی دیتابیس، صرفاً از فایل‌های پشتیبانی که خودِ
// DatabaseBackupService تولید کرده (هیچ Import فایل دلخواه/SQL Console در این پنل وجود ندارد).
// چون فایل با «یک statement کامل در هر خط» تولید می‌شود، خواندن/اجرا خط‌به‌خط است — بدون نیاز به SQL parser.

declare(strict_types=1);

namespace App\Services;

use App\Core\Database;
use App\Exceptions\ApiException;
use App\Repositories\DatabaseBackupRepository;
use App\Repositories\MySQLMetaRepository;
use PDO;
use PDOException;
use Throwable;

class DatabaseRestoreService {
    private PDO $pdo;
    private MySQLMetaRepository $meta;
    private DatabaseBackupRepository $backupRepo;
    private DatabaseBackupService $backupService;

    public function __construct() {
        $this->pdo = Database::getInstance()->getPdoConnection();
        $this->meta = new MySQLMetaRepository();
        $this->backupRepo = new DatabaseBackupRepository();
        $this->backupService = new DatabaseBackupService();
    }

    // بازیابی نیازمند تایپ دقیق نام دیتابیس توسط کاربر است — تأیید دومرحله‌ای واقعی، نه فقط یک دکمه‌ی «تأیید»
    public function restore(int $backupId, string $confirmDbName, string $actor): array {
        $currentDbName = $this->meta->databaseName();
        if (!hash_equals($currentDbName, $confirmDbName)) {
            throw new ApiException('نام دیتابیس واردشده مطابقت ندارد. برای تأیید، دقیقاً نام دیتابیس را تایپ کنید.', 422);
        }

        $row = $this->backupRepo->find($backupId);
        if ($row === null) {
            throw new ApiException('پشتیبان مورد نظر یافت نشد.', 404);
        }
        if (!in_array($row['status'], ['success', 'verified'], true)) {
            throw new ApiException('فقط پشتیبان‌های سالم (موفق یا تأییدشده) قابل بازیابی هستند.', 422);
        }

        $path = $this->backupService->filePath($row);
        if (!file_exists($path)) {
            throw new ApiException('فایل پشتیبان روی دیسک یافت نشد.', 404);
        }

        $gzip = $row['compression'] === 'gzip';
        $handle = $gzip ? @gzopen($path, 'rb') : @fopen($path, 'rb');
        if ($handle === false) {
            throw new ApiException('باز کردن فایل پشتیبان ممکن نشد.', 500);
        }

        $startedAt = microtime(true);
        $totalStatements = 0;
        $executedStatements = 0;
        $failedStatement = null;
        $errorMessage = null;

        try {
            while (($gzip ? !gzeof($handle) : !feof($handle))) {
                $line = $gzip ? gzgets($handle) : fgets($handle);
                if ($line === false) {
                    break;
                }
                $statement = trim($line);
                if ($statement === '' || str_starts_with($statement, '--')) {
                    continue;
                }

                $totalStatements++;
                try {
                    // DDL در MySQL باعث commit ضمنی می‌شود؛ به همین دلیل کل بازیابی در یک Transaction واحد قابل
                    // wrap کردن نیست — این محدودیت به‌صراحت در UI (پیش از تأیید) به کاربر اعلام می‌شود.
                    $this->pdo->exec($statement);
                    $executedStatements++;
                } catch (PDOException $e) {
                    $failedStatement = $totalStatements;
                    $errorMessage = $e->getMessage();
                    break;
                }
            }
        } finally {
            $gzip ? @gzclose($handle) : @fclose($handle);
        }

        $durationMs = (int)round((microtime(true) - $startedAt) * 1000);
        $success = $failedStatement === null;

        AuditLogger::log($actor, 'mysql.restore.execute', 'mysql_backup', (string)$backupId, [
            'status' => $success ? 'success' : 'partial_failure',
            'totalStatements' => $totalStatements,
            'executedStatements' => $executedStatements,
            'failedAtStatement' => $failedStatement,
            'durationMs' => $durationMs,
        ]);

        if (!$success) {
            error_log("DatabaseRestoreService::restore partial failure at statement {$failedStatement}: {$errorMessage}");
        }

        return [
            'backupId' => $backupId,
            'success' => $success,
            'totalStatements' => $totalStatements,
            'executedStatements' => $executedStatements,
            'failedAtStatement' => $failedStatement,
            'errorMessage' => $success ? null : 'اجرای دستورات در میانه‌ی راه متوقف شد؛ دیتابیس ممکن است در وضعیت ناقص باشد. جزئیات فنی در لاگ سرور ثبت شد.',
            'durationMs' => $durationMs,
        ];
    }
}
