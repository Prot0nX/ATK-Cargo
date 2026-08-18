<?php
// PHP/scripts/rotate_logs.php
//
// جایگزین PHP/deploy/logrotate.d/atk-cargo برای هاست‌های اشتراکی که دسترسی
// root/logrotate ندارند (DEEP_CODE_AUDIT.md #Phase4.11). Logger.php هرگز
// خودش فایل‌های logs/*.log را نمی‌چرخاند یا پاک نمی‌کند — بدون این اسکریپت
// (یا logrotate واقعی)، این فایل‌ها تا ابد رشد می‌کنند.
//
// نصب: یک cron job کاربر (نه root) هر روز این را اجرا کند، مثلاً:
//   0 3 * * * php /path/to/PHP/scripts/rotate_logs.php >> /path/to/PHP/logs/rotate.out 2>&1
//
// این اسکریپت فقط CLI است — مثل export_schema.php از طریق HTTP اجرا نمی‌شود.

declare(strict_types=1);

if (PHP_SAPI !== 'cli') {
    http_response_code(403);
    exit('Forbidden: این اسکریپت فقط از طریق CLI/cron قابل اجراست.');
}

const MAX_SIZE_BYTES = 50 * 1024 * 1024; // ۵۰ مگابایت
const KEEP_ARCHIVES_DAYS = 30;

$logDir = dirname(__DIR__) . '/logs';

if (!is_dir($logDir)) {
    fwrite(STDERR, "پوشه‌ی logs یافت نشد: $logDir\n");
    exit(1);
}

$rotated = 0;
$deleted = 0;

foreach (glob($logDir . '/*.log') as $logFile) {
    $baseName = basename($logFile, '.log');

    // فایل‌های آرشیوشده (که خودشان .log هستند اما با پسوند تاریخ) دوباره
    // چرخانده نشوند — فقط فایل‌های لاگ فعال (بدون تاریخ در نام) هدف‌اند.
    if (preg_match('/-\d{4}-\d{2}-\d{2}$/', $baseName)) {
        continue;
    }

    if (filesize($logFile) < MAX_SIZE_BYTES) {
        continue;
    }

    $archiveName = sprintf('%s/%s-%s.log.gz', $logDir, $baseName, date('Y-m-d_His'));

    $content = file_get_contents($logFile);
    if ($content === false) {
        fwrite(STDERR, "خواندن ناموفق: $logFile\n");
        continue;
    }

    $compressed = gzencode($content, 9);
    if ($compressed === false || file_put_contents($archiveName, $compressed) === false) {
        fwrite(STDERR, "آرشیو ناموفق: $logFile\n");
        continue;
    }

    // خالی‌کردن فایل اصلی (نه حذف) — Logger.php به همان مسیر می‌نویسد و اگر
    // فایل حذف شود، تا اولین باز شدن پراسس PHP بعدی همچنان به fd قدیمی
    // (که دیگر در سیستم‌فایل قابل مشاهده نیست) می‌نویسد؛ truncate امن‌تر است.
    if (file_put_contents($logFile, '') === false) {
        fwrite(STDERR, "خالی‌کردن ناموفق بعد از آرشیو: $logFile\n");
        continue;
    }

    $rotated++;
    echo "چرخانده شد: $logFile -> $archiveName\n";
}

// حذف آرشیوهای قدیمی‌تر از KEEP_ARCHIVES_DAYS روز
$cutoff = time() - (KEEP_ARCHIVES_DAYS * 86400);
foreach (glob($logDir . '/*.log.gz') as $archiveFile) {
    if (filemtime($archiveFile) < $cutoff) {
        if (unlink($archiveFile)) {
            $deleted++;
            echo "حذف شد (قدیمی‌تر از " . KEEP_ARCHIVES_DAYS . " روز): $archiveFile\n";
        }
    }
}

echo "پایان: $rotated فایل چرخانده شد، $deleted آرشیو قدیمی حذف شد.\n";
