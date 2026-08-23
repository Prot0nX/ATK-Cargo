<?php
// PHP/scripts/health_monitor.php مانیتورینگ خودکار health-check.

declare(strict_types=1);

if (PHP_SAPI !== 'cli') {
    http_response_code(403);
    exit('Forbidden: این اسکریپت فقط از طریق CLI/cron قابل اجراست.');
}

require_once __DIR__ . '/../src/bootstrap.php';

use App\Controllers\DiagnosticsController;
use App\Services\SecurityAlerter;

$controller = new DiagnosticsController();
$result = $controller->evaluateHealth();

if ($result['healthy']) {
    echo "OK: سیستم سالم است (" . date('Y-m-d H:i:s') . ")\n";
    exit(0);
}

$details = [];
if (!$result['status']['database']) {
    $details[] = 'اتصال دیتابیس برقرار نشد.';
} elseif (!empty($result['missingTables'])) {
    $details[] = 'جداول ناموجود: ' . implode('، ', $result['missingTables']);
}

$message = implode(' ', $details);

// dedupeKey ثابت است تا در بازه‌ی cooldown داخلی SecurityAlerter (۵ دقیقه) خرابی مستمر هر ۵ دقیقه دوباره هشدار تلگرام نفرستد، فقط یک‌بار در هر cooldown.
SecurityAlerter::getInstance()->alert('HEALTH_CHECK_FAILED', $message, 'HEALTH_CHECK_FAILED');

fwrite(STDERR, "UNHEALTHY: $message\n");
exit(1);