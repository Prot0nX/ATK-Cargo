<?php
// PHP/src/Services/AuditLogger.php

declare(strict_types=1);

namespace App\Services;

use App\Core\DatabaseManager;

/**
 * ثبت best-effort عملیات حساس در جدول audit_log (I-03؛ ساختار در
 * schema.sql و migrations/ مستند است). عمداً هرگز عملیات اصلی را با شکست
 * خودش fail نمی‌کند (مثلاً روی نصب‌های جدیدی که هنوز migration مربوطه را
 * اجرا نکرده‌اند) — فقط با error_log ثبت و بی‌صدا نادیده گرفته می‌شود.
 */
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
            $stmt->bind_param("sssss", $actorUsername, $action, $entityType, $entityId, $detailsJson);
            $stmt->execute();
        } catch (\Throwable $e) {
            error_log('AuditLogger: failed to write audit log entry - ' . $e->getMessage());
        }
    }
}
