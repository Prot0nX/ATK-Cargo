<?php
// PHP/scripts/crash_report_summary.php
//
// گزارش روزانه‌ی تعداد کرش‌های جدید (DEEP_CODE_REVIEW.md Phase3 #28).
// logs/crash_reports.log یک فایل متنی است که تا پیش از این کسی باید دستی
// می‌خواند تا خرابی‌های کلاینت کشف شوند. این اسکریپت تعداد خطوط جدید نوشته‌شده
// از آخرین اجرا را می‌شمارد و در صورت غیرصفر بودن، از همان کانال Telegram
// موجود (SecurityAlerter) هشدار می‌فرستد.
//
// نکته‌ی rotate_logs.php: چون آن اسکریپت فایل را truncate می‌کند (نه حذف)،
// شمارنده‌ی خط اینجا اگر بین دو اجرا rotate رخ داده باشد (فایل کوچک‌تر از
// آخرین offset ثبت‌شده) از صفر شروع می‌کند تا شمارش منفی/اشتباه ندهد.
//
// نصب: یک cron job کاربر (نه root) هر روز این را اجرا کند، مثلاً:
//   0 8 * * * php /path/to/PHP/scripts/crash_report_summary.php >> /path/to/PHP/logs/crash_summary.out 2>&1
//
// این اسکریپت فقط CLI است — مثل rotate_logs.php از طریق HTTP اجرا نمی‌شود.

declare(strict_types=1);

if (PHP_SAPI !== 'cli') {
    http_response_code(403);
    exit('Forbidden: این اسکریپت فقط از طریق CLI/cron قابل اجراست.');
}

require_once __DIR__ . '/../src/bootstrap.php';

use App\Services\SecurityAlerter;

$logFile = APP_ROOT . '/logs/crash_reports.log';
$stateFile = APP_ROOT . '/logs/.crash_summary_state';

$currentLineCount = 0;
if (file_exists($logFile)) {
    $currentLineCount = count(file($logFile, FILE_SKIP_EMPTY_LINES));
}

$lastLineCount = 0;
if (file_exists($stateFile)) {
    $lastLineCount = (int)trim((string)file_get_contents($stateFile));
}

// اگر فایل کوچک‌تر از آخرین شمارش ثبت‌شده باشد یعنی rotate_logs.php آن را
// truncate کرده — شمارش جدید از صفر است، نه یک عدد منفی.
$newCrashes = $currentLineCount >= $lastLineCount
    ? $currentLineCount - $lastLineCount
    : $currentLineCount;

file_put_contents($stateFile, (string)$currentLineCount, LOCK_EX);

if ($newCrashes === 0) {
    echo "OK: هیچ کرش جدیدی از آخرین بررسی ثبت نشده (" . date('Y-m-d H:i:s') . ")\n";
    exit(0);
}

$message = "$newCrashes گزارش کرش جدید در logs/crash_reports.log از آخرین بررسی ثبت شده است.";

// dedupeKey شامل تاریخ است تا هر روز حداکثر یک هشدار بفرستد، نه اینکه اجرای
// مکرر دستی همان روز را cooldown داخلی (۵ دقیقه) به‌اشتباه سرکوب کند و بعد
// هم روز بعد چیزی نفرستد.
SecurityAlerter::getInstance()->alert('DAILY_CRASH_SUMMARY', $message, 'DAILY_CRASH_SUMMARY_' . date('Y-m-d'));

echo "$message\n";
