<?php
// PHP/src/Services/AuditLogger.php

declare(strict_types=1);

namespace App\Services;

use App\Core\DatabaseManager;

// ثبت best-effort عملیات حساس در جدول audit_log؛ خطا هرگز عملیات اصلی را نمی‌شکند و فقط لاگ می‌شود
final class AuditLogger {
    public static function log(
        string $actorUsername,
        string $action,
        string $entityType,
        string $entityId,
        array $details = []
    ): void {
        try {
            $db = new DatabaseManager();
            $detailsJson = empty($details) ? null : json_encode($details, JSON_UNESCAPED_UNICODE);

            $query = "INSERT INTO audit_log (username, action, entity_type, entity_id, details) VALUES (?, ?, ?, ?, ?)";
            $stmt = $db->prepare($query);
            $stmt->execute([$actorUsername, $action, $entityType, $entityId, $detailsJson]);
        } catch (\Throwable $e) {
            error_log('AuditLogger: failed to write audit log entry - ' . $e->getMessage());
        }
    }
}
