<?php
// PHP/MySQL_Manager/download.php — دانلود امن فایل پشتیبان. طبق بند ۴۲ spec، فایل‌های پشتیبان هرگز
// مستقیم از طریق URL قابل دسترسی نیستند (پوشه‌ی storage پشت .htaccess است)؛ این فایل تنها راه مجاز دانلود است
// و صرفاً بعد از احراز هویت کامل صفحه (نه فقط JSON API) اجرا می‌شود، چون از طریق یک لینک <a href> باز می‌شود.

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

use App\Core\Logger;
use App\Services\AuditLogger;
use App\Services\DatabaseBackupService;

mys_require_auth_page();

$id = (int)($_GET['id'] ?? 0);
if ($id <= 0) {
    http_response_code(422);
    exit('شناسه‌ی پشتیبان نامعتبر است.');
}

try {
    $service = new DatabaseBackupService();
    $row = $service->find($id);
} catch (\Throwable $e) {
    http_response_code(404);
    exit('پشتیبان مورد نظر یافت نشد.');
}

if (!in_array($row['status'], ['success', 'verified'], true)) {
    http_response_code(409);
    exit('این پشتیبان هنوز آماده‌ی دانلود نیست.');
}

$path = $service->filePath($row);
if (!file_exists($path)) {
    http_response_code(404);
    exit('فایل پشتیبان روی دیسک یافت نشد.');
}

AuditLogger::log(MYS_ACTOR, 'mysql.backup.download', 'mysql_backup', (string)$id, []);
Logger::getInstance()->security('MySQL Manager backup downloaded: id=' . $id);

$filename = basename($row['filename']);
$mime = $row['compression'] === 'gzip' ? 'application/gzip' : 'application/sql';

header('Content-Type: ' . $mime);
header('Content-Disposition: attachment; filename="' . $filename . '"');
header('Content-Length: ' . (string)filesize($path));
header('X-Content-Type-Options: nosniff');
header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');

readfile($path);
exit;
