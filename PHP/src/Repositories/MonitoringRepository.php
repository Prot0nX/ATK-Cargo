<?php
// PHP/src/Repositories/MonitoringRepository.php

declare(strict_types=1);

namespace App\Repositories;

use App\Core\Database;
use PDO;

// تنها منبع حقیقت کوئری‌های جدول monitoring_events؛ برای خواندن/تایید توسط MonitoringController
class MonitoringRepository {
    // سقف سخت‌گیرانه به‌جای صفحه‌بندی کامل، مشابه LicenseRepository::MAX_ROWS
    private const MAX_ROWS = 200;

    private PDO $db;

    public function __construct() {
        $this->db = Database::getInstance()->getPdoConnection();
    }

    /**
     * @param string $status یکی از open، acknowledged یا all
     * @return array<int,array<string,mixed>>
     */
    public function listEvents(string $status, int $limit, ?int $beforeId = null): array {
        $limit = max(1, min($limit, self::MAX_ROWS));
        $where = [];
        $params = [];

        if ($status === 'open') {
            $where[] = 'acknowledged_at IS NULL';
        } elseif ($status === 'acknowledged') {
            $where[] = 'acknowledged_at IS NOT NULL';
        }

        if ($beforeId !== null) {
            $where[] = 'id < :before_id';
            $params[':before_id'] = $beforeId;
        }

        $sql = 'SELECT * FROM monitoring_events';
        if ($where !== []) {
            $sql .= ' WHERE ' . implode(' AND ', $where);
        }
        $sql .= ' ORDER BY id DESC LIMIT ' . $limit;

        $stmt = $this->db->prepare($sql);
        $stmt->execute($params);
        return $stmt->fetchAll();
    }

    /** @return array<string,mixed>|null */
    public function findById(int $id): ?array {
        $stmt = $this->db->prepare('SELECT * FROM monitoring_events WHERE id = :id LIMIT 1');
        $stmt->execute([':id' => $id]);
        $row = $stmt->fetch();
        return $row ?: null;
    }

    // فقط اگر هنوز تایید نشده باشد اثر می‌کند؛ rowCount()===0 یعنی «قبلاً تایید شده»
    public function acknowledge(int $id, string $username): bool {
        $stmt = $this->db->prepare(
            'UPDATE monitoring_events SET acknowledged_at = NOW(), acknowledged_by = :username WHERE id = :id AND acknowledged_at IS NULL'
        );
        $stmt->execute([':username' => $username, ':id' => $id]);
        return $stmt->rowCount() > 0;
    }

    // حذف دائمی یک رویداد؛ rowCount()===0 یعنی رویداد از قبل وجود نداشته
    public function delete(int $id): bool {
        $stmt = $this->db->prepare('DELETE FROM monitoring_events WHERE id = :id');
        $stmt->execute([':id' => $id]);
        return $stmt->rowCount() > 0;
    }

    /** @return array{open_total:int, open_critical:int, open_warning:int, open_info:int} */
    public function openCounts(): array {
        $sql = "SELECT
                COUNT(*) AS open_total,
                SUM(severity = 'critical') AS open_critical,
                SUM(severity = 'warning') AS open_warning,
                SUM(severity = 'info') AS open_info
            FROM monitoring_events
            WHERE acknowledged_at IS NULL";
        $stmt = $this->db->query($sql);
        $row = $stmt !== false ? ($stmt->fetch() ?: []) : [];

        // SUM() روی جدول خالی NULL برمی‌گرداند نه صفر
        return [
            'open_total'    => (int)($row['open_total'] ?? 0),
            'open_critical' => (int)($row['open_critical'] ?? 0),
            'open_warning'  => (int)($row['open_warning'] ?? 0),
            'open_info'     => (int)($row['open_info'] ?? 0),
        ];
    }
}