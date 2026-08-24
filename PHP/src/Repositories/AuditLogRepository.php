<?php
// PHP/src/Repositories/AuditLogRepository.php

declare(strict_types=1);

namespace App\Repositories;

use App\Core\Database;
use PDO;

// تنها منبع حقیقت کوئری‌های جدول audit_log؛ فقط خواندن — نوشتن از طریق App\Services\AuditLogger انجام می‌شود
class AuditLogRepository {
 // سقف سخت‌گیرانه به‌جای صفحه‌بندی کامل، مشابه MonitoringRepository::MAX_ROWS
    private const MAX_ROWS = 200;

    private PDO $db;

    public function __construct() {
        $this->db = Database::getInstance()->getPdoConnection();
    }

 /** @return array<int,array<string,mixed>> */
    public function listLogs(int $limit, ?int $beforeId = null, ?string $username = null, ?string $entityType = null): array {
        $limit = max(1, min($limit, self::MAX_ROWS));
        $where = [];
        $params = [];

        if ($beforeId !== null) {
            $where[] = 'id < :before_id';
            $params[':before_id'] = $beforeId;
        }

        if ($username !== null && $username !== '') {
            $where[] = 'username = :username';
            $params[':username'] = $username;
        }

        if ($entityType !== null && $entityType !== '') {
            $where[] = 'entity_type = :entity_type';
            $params[':entity_type'] = $entityType;
        }

        $sql = 'SELECT * FROM audit_log';
        if ($where !== []) {
            $sql .= ' WHERE ' . implode(' AND ', $where);
        }
        $sql .= ' ORDER BY id DESC LIMIT ' . $limit;

        $stmt = $this->db->prepare($sql);
        $stmt->execute($params);
        return $stmt->fetchAll();
    }

 // حذف دائمی یک ردیف لاگ تغییرات؛ rowCount===0 یعنی از قبل وجود نداشته
    public function delete(int $id): bool {
        $stmt = $this->db->prepare('DELETE FROM audit_log WHERE id = :id');
        $stmt->execute([':id' => $id]);
        return $stmt->rowCount() > 0;
    }
}
