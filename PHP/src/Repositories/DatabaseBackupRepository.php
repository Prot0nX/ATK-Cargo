<?php
// PHP/src/Repositories/DatabaseBackupRepository.php — تنها منبع کوئری‌های db_backups/db_maintenance_settings
// برای پنل MySQL_Manager. فایل فیزیکی پشتیبان هرگز اینجا لمس نمی‌شود؛ فقط متادیتا.

declare(strict_types=1);

namespace App\Repositories;

use App\Core\Database;
use PDO;

class DatabaseBackupRepository {
    private PDO $db;

    public function __construct() {
        $this->db = Database::getInstance()->getPdoConnection();
    }

    public function createRunning(string $filename, string $backupType, string $compression, ?array $tables, string $actor, bool $isAutomatic = false): int {
        $stmt = $this->db->prepare(
            'INSERT INTO db_backups (filename, backup_type, compression, tables_included, status, is_automatic, created_by)
             VALUES (:filename, :backup_type, :compression, :tables, \'running\', :is_automatic, :created_by)'
        );
        $stmt->execute([
            ':filename' => $filename,
            ':backup_type' => $backupType,
            ':compression' => $compression,
            ':tables' => $tables === null ? null : json_encode($tables, JSON_UNESCAPED_UNICODE),
            ':is_automatic' => $isAutomatic ? 1 : 0,
            ':created_by' => $actor,
        ]);
        return (int)$this->db->lastInsertId();
    }

    public function markFinished(int $id, string $status, int $fileSizeBytes, ?string $checksum, int $durationMs, ?string $errorMessage = null): void {
        $stmt = $this->db->prepare(
            'UPDATE db_backups
             SET status = :status, file_size_bytes = :size, checksum_sha256 = :checksum,
                 duration_ms = :duration_ms, error_message = :error_message
             WHERE id = :id'
        );
        $stmt->execute([
            ':status' => $status,
            ':size' => $fileSizeBytes,
            ':checksum' => $checksum,
            ':duration_ms' => $durationMs,
            ':error_message' => $errorMessage,
            ':id' => $id,
        ]);
    }

    public function markStatus(int $id, string $status, ?string $errorMessage = null): void {
        $stmt = $this->db->prepare('UPDATE db_backups SET status = :status, error_message = :error_message WHERE id = :id');
        $stmt->execute([':status' => $status, ':error_message' => $errorMessage, ':id' => $id]);
    }

    public function find(int $id): ?array {
        $stmt = $this->db->prepare('SELECT * FROM db_backups WHERE id = :id');
        $stmt->execute([':id' => $id]);
        $row = $stmt->fetch();
        return $row === false ? null : $row;
    }

    public function latest(): ?array {
        $stmt = $this->db->prepare("SELECT * FROM db_backups WHERE status IN ('success','verified') ORDER BY created_at DESC, id DESC LIMIT 1");
        $stmt->execute();
        $row = $stmt->fetch();
        return $row === false ? null : $row;
    }

    public function latestAutomatic(): ?array {
        $stmt = $this->db->prepare("SELECT * FROM db_backups WHERE is_automatic = 1 AND status IN ('success','verified') ORDER BY created_at DESC, id DESC LIMIT 1");
        $stmt->execute();
        $row = $stmt->fetch();
        return $row === false ? null : $row;
    }

    public function paginated(int $page, int $perPage): array {
        $perPage = max(1, min($perPage, 100));
        $page = max(1, $page);
        $offset = ($page - 1) * $perPage;

        $countStmt = $this->db->prepare('SELECT COUNT(*) AS total FROM db_backups');
        $countStmt->execute();
        $total = (int)($countStmt->fetch()['total'] ?? 0);

        $stmt = $this->db->prepare('SELECT * FROM db_backups ORDER BY created_at DESC, id DESC LIMIT :limit OFFSET :offset');
        $stmt->bindValue(':limit', $perPage, PDO::PARAM_INT);
        $stmt->bindValue(':offset', $offset, PDO::PARAM_INT);
        $stmt->execute();

        return ['rows' => $stmt->fetchAll(), 'total' => $total, 'page' => $page, 'perPage' => $perPage];
    }

    // ردیف‌های خودکار موفق/تأییدشده که قدیمی‌تر از N اخیرترین هستند — برای اعمال Retention روی Backupهای خودکار (فاز ۴)
    public function findAutomaticOlderThanKeep(int $keep): array {
        $keep = max(0, $keep);
        $stmt = $this->db->prepare(
            "SELECT id, filename FROM db_backups
             WHERE is_automatic = 1 AND status IN ('success', 'verified')
             ORDER BY created_at DESC, id DESC
             LIMIT 100000 OFFSET :keep"
        );
        $stmt->bindValue(':keep', $keep, PDO::PARAM_INT);
        $stmt->execute();
        return $stmt->fetchAll();
    }

    public function delete(int $id): void {
        $stmt = $this->db->prepare('DELETE FROM db_backups WHERE id = :id');
        $stmt->execute([':id' => $id]);
    }

    public function getSettings(): array {
        $stmt = $this->db->prepare('SELECT * FROM db_maintenance_settings WHERE id = 1');
        $stmt->execute();
        $row = $stmt->fetch();
        return $row === false ? [] : $row;
    }

    public function saveSettings(array $settings): void {
        $stmt = $this->db->prepare(
            'UPDATE db_maintenance_settings SET
                auto_backup_enabled = :enabled,
                auto_backup_frequency = :frequency,
                auto_backup_time = :time,
                auto_backup_retention = :retention,
                auto_backup_compression = :compression,
                auto_backup_include_structure = :include_structure,
                auto_backup_include_data = :include_data,
                auto_analyze_weekly = :analyze_weekly,
                auto_optimize_weekly = :optimize_weekly,
                auto_cleanup_sessions = :cleanup_sessions
             WHERE id = 1'
        );
        $stmt->execute([
            ':enabled' => $settings['auto_backup_enabled'] ? 1 : 0,
            ':frequency' => $settings['auto_backup_frequency'],
            ':time' => $settings['auto_backup_time'],
            ':retention' => $settings['auto_backup_retention'],
            ':compression' => $settings['auto_backup_compression'] ? 1 : 0,
            ':include_structure' => $settings['auto_backup_include_structure'] ? 1 : 0,
            ':include_data' => $settings['auto_backup_include_data'] ? 1 : 0,
            ':analyze_weekly' => $settings['auto_analyze_weekly'] ? 1 : 0,
            ':optimize_weekly' => $settings['auto_optimize_weekly'] ? 1 : 0,
            ':cleanup_sessions' => $settings['auto_cleanup_sessions'] ? 1 : 0,
        ]);
    }
}
