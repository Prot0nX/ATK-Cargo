<?php
// PHP/cron/mysql_manager_cron.php — نقطه‌ی ورود CLI برای نگهداری خودکار MySQL_Manager (بند ۲۳ spec).
// فقط از خط فرمان قابل اجراست؛ یک درخواست HTTP مستقیم به این فایل (حتی اگر .htaccess به هر دلیل حذف شده باشد)
// بلافاصله با ۴۰۳ رد می‌شود چون هیچ نشست/CSRFای در کار نیست.
//
// نمونه‌ی ورودی crontab (هر ۱۵ دقیقه، هم‌راستا با پنجره‌ی تشخیص «نوبت رسیده» در MaintenanceScheduler):
//   */15 * * * * php /path/to/PHP/cron/mysql_manager_cron.php >> /path/to/PHP/logs/mysql_manager_cron.log 2>&1

declare(strict_types=1);

if (PHP_SAPI !== 'cli') {
    http_response_code(403);
    exit('Forbidden: this script can only be run from the command line.');
}

require_once __DIR__ . '/../src/bootstrap.php';

use App\Core\Logger;
use App\Services\MaintenanceScheduler;

try {
    $scheduler = new MaintenanceScheduler();
    $result = $scheduler->run();
    echo json_encode($result, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT) . PHP_EOL;
    Logger::getInstance()->info('mysql_manager_cron run: ' . json_encode($result, JSON_UNESCAPED_UNICODE));
} catch (\Throwable $e) {
    fwrite(STDERR, 'mysql_manager_cron failed: ' . $e->getMessage() . PHP_EOL);
    Logger::getInstance()->error('mysql_manager_cron failed: ' . $e->getMessage());
    exit(1);
}
