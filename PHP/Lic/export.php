<?php
// PHP/Lic/export.php
//
// خروجی CSV از لیست لایسنس‌ها. جایگزین دکمه‌ی «خروجی Excel» پنل قدیمی که
// تابع تعریف‌نشده‌ی exportData() را صدا می‌زد و عملاً هیچ کاری نمی‌کرد.
//
// خروجی همان فیلتر و جستجوی جاری صفحه را رعایت می‌کند تا کاربر بتواند
// «فقط منقضی‌شده‌ها» را بگیرد، نه همیشه کل جدول.

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

use App\Core\Request;
use App\Services\LicenseAdminService;

lic_require_auth_page();

$request = new Request();
$search = $request->get('search');
$status = $request->get('status');

$rows = (new LicenseAdminService())->list(
    is_string($search) ? trim($search) : null,
    is_string($status) ? $status : null
);

$statusLabels = [
    'active'   => 'فعال',
    'expired'  => 'منقضی',
    'inactive' => 'غیرفعال',
];

$filename = 'licenses-' . date('Y-m-d-His') . '.csv';

header('Content-Type: text/csv; charset=UTF-8');
header('Content-Disposition: attachment; filename="' . $filename . '"');
header('X-Content-Type-Options: nosniff');
header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');

$out = fopen('php://output', 'wb');

// BOM لازم است وگرنه Excel فایل را با کدپیج محلی باز می‌کند و متن فارسی
// به‌صورت کاراکترهای درهم نمایش داده می‌شود.
fwrite($out, "\xEF\xBB\xBF");

fputcsv($out, [
    'کلید لایسنس',
    'نام شرکت',
    'پلن',
    'وضعیت',
    'تاریخ انقضا',
    'نام رابط',
    'شماره تماس',
    'ایمیل',
    'تاریخ ایجاد',
    'آخرین بررسی',
    'یادداشت',
]);

foreach ($rows as $row) {
    fputcsv($out, [
        // پیشوند خنثی‌سازی فرمول: مقادیری که با = + - @ شروع می‌شوند در
        // Excel به‌عنوان فرمول اجرا می‌شوند (CSV injection). نام شرکت و
        // یادداشت را کاربر وارد می‌کند، پس هر دو باید خنثی شوند.
        lic_csv_safe($row['license_key']),
        lic_csv_safe($row['company_name']),
        lic_csv_safe($row['plan_label']),
        $statusLabels[$row['status']] ?? $row['status'],
        $row['expires_at'] ?? 'نامحدود',
        lic_csv_safe($row['contact_name']),
        lic_csv_safe($row['contact_phone']),
        lic_csv_safe($row['contact_email']),
        $row['created_at'],
        $row['last_check'] ?? 'هرگز',
        lic_csv_safe($row['notes']),
    ]);
}

fclose($out);
exit;

function lic_csv_safe(?string $value): string {
    $value = (string)$value;
    if ($value !== '' && strpbrk($value[0], "=+-@\t\r") !== false) {
        return "'" . $value;
    }
    return $value;
}
