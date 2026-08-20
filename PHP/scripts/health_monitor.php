<?php
// PHP/scripts/health_monitor.php
//
// مانیتورینگ خودکار health-check (DEEP_CODE_REVIEW.md Phase3 #28). قبلاً
// endpoint سلامت (`GET /api/v2/health`) وجود داشت اما هیچ مصرف‌کننده‌ای
// نداشت — خرابی دیتابیس/جدول تا وقتی کاربر گزارش می‌داد کشف نمی‌شد.
//
// این اسکریپت مستقیماً DiagnosticsController::evaluateHealth() را صدا
// می‌زند (نه از طریق HTTP) — یعنی نیازی به حذف فیلتر User-Agent در
// config/.htaccess (که curl/wget را مسدود می‌کند) نیست و یک درخواست HTTP
// اضافه روی خودِ سرور صرفه‌جویی می‌شود. کانال هشدار همان SecurityAlerter
// (Telegram) است که از قبل برای رویدادهای امنیتی استفاده می‌شود — بدون
// SECURITY_ALERT_TELEGRAM_BOT_TOKEN/CHAT_ID در .env کاملاً no-op می‌ماند.
//
// نصب: یک cron job کاربر (نه root) هر ۵ دقیقه این را اجرا کند، مثلاً:
//   */5 * * * * php /path/to/PHP/scripts/health_monitor.php >> /path/to/PHP/logs/health_monitor.out 2>&1
//
// این اسکریپت فقط CLI است — مثل export_schema.php/rotate_logs.php از طریق HTTP اجرا نمی‌شود.

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

// dedupeKey ثابت است تا در بازه‌ی cooldown داخلی SecurityAlerter (۵ دقیقه)
// خرابی مستمر هر ۵ دقیقه دوباره هشدار تلگرام نفرستد، فقط یک‌بار در هر cooldown.
SecurityAlerter::getInstance()->alert('HEALTH_CHECK_FAILED', $message, 'HEALTH_CHECK_FAILED');

fwrite(STDERR, "UNHEALTHY: $message\n");
exit(1);
