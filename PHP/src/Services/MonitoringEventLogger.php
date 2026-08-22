<?php
// PHP/src/Services/MonitoringEventLogger.php

declare(strict_types=1);

namespace App\Services;

use App\Core\DatabaseManager;

// ثبت best-effort رویدادهای مانیتورینگ در monitoring_events؛ مطابق الگوی
// AuditLogger — خطا هرگز مسیر فراخوان (SecurityAlerter::alert در مسیرهای
// امنیتی حساس مثل login/refresh) را نمی‌شکند
final class MonitoringEventLogger {
    public static function record(
        string $eventType,
        string $severity,
        string $message,
        string $source,
        ?string $dedupeKey = null
    ): void {
        try {
            $db = new DatabaseManager();
            $query = "INSERT INTO monitoring_events (event_type, severity, message, source, dedupe_key) VALUES (?, ?, ?, ?, ?)";
            $stmt = $db->prepare($query);
            $stmt->bind_param("sssss", $eventType, $severity, $message, $source, $dedupeKey);
            $stmt->execute();
        } catch (\Throwable $e) {
            error_log('MonitoringEventLogger: failed to write monitoring event - ' . $e->getMessage());
        }
    }
}
