<?php
// PHP/Quota Reports/export.php — خروجی واقعی Excel/HTML گزارش یک کوتاژ (نه CSV دستی).
// خروجی PDF عمداً حذف شد: dompdf شکل‌دهی صحیح حروف فارسی (به‌خصوص در متن‌های ترکیبی فارسی/لاتین) را
// به‌طور قابل‌اعتماد پشتیبانی نمی‌کند و حتی با فونت جاسازی‌شده هم رندر شکسته/آینه‌ای می‌داد.

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

use App\Core\Logger;
use App\Core\Request;
use App\Repositories\CargoRepository;
use App\Services\QuotaService;
use PhpOffice\PhpSpreadsheet\Cell\DataType;
use PhpOffice\PhpSpreadsheet\Spreadsheet;
use PhpOffice\PhpSpreadsheet\Style\Alignment;
use PhpOffice\PhpSpreadsheet\Style\Fill;
use PhpOffice\PhpSpreadsheet\Writer\Xlsx;

qr_require_auth_page();

// خروجی خطا به‌صورت متن ساده‌ی خوانا (نه یک صفحه‌ی 500 خالی) — همیشه لاگ کامل هم ثبت می‌شود.
function qr_export_fail(string $message, int $httpCode = 500): never {
    if (!headers_sent()) {
        http_response_code($httpCode);
        header('Content-Type: text/plain; charset=UTF-8');
    }
    echo $message;
    exit;
}

$request = new Request();
$format = (string)$request->get('format', '');
$kotazh = trim((string)$request->get('kotazh', ''));

if (!preg_match('/^\d{8}$/', $kotazh)) {
    qr_export_fail('فرمت کوتاژ نامعتبر است.', 422);
}

// اگر composer install روی این سرور برای وابستگی تازه‌ی phpoffice/phpspreadsheet اجرا نشده باشد،
// به‌جای یک خطای فاتال بی‌صدا (چون vendor/ در گیت کامیت نمی‌شود) پیام روشنی نمایش داده می‌شود.
if ($format === 'excel' && !class_exists(Spreadsheet::class)) {
    qr_export_fail('کتابخانه‌ی مورد نیاز این خروجی نصب نشده است. روی این سرور دستور «composer install» را در پوشه‌ی PHP اجرا کنید.');
}

try {
    $kotazhInfo = (new QuotaService())->getQuotaDetails($kotazh);
    if ($kotazhInfo === null) {
        qr_export_fail('کوتاژ مورد نظر یافت نشد.', 404);
    }

    $cargoInfo = (new CargoRepository())->findByQuotaNumber($kotazh);
} catch (\Throwable $e) {
    Logger::getInstance()->error(sprintf(
        'Quota Reports export.php (data load): [%s] %s in %s:%d',
        get_class($e), $e->getMessage(), $e->getFile(), $e->getLine()
    ));
    qr_export_fail('خطا در بارگذاری اطلاعات کوتاژ رخ داد.');
}

// مقادیر آزاد (نام کاربری) قبل از قرارگیری در سلول اکسل/HTML در برابر تزریق فرمول (CSV/Excel injection) خنثی می‌شوند.
function qr_safe(?string $value): string {
    $value = (string)$value;
    if ($value !== '' && strpbrk($value[0], "=+-@\t\r") !== false) {
        return "'" . $value;
    }
    return $value;
}

$statusLabel = static fn(?string $status): string => $status === 'خروج' ? 'خارج شده' : ($status === 'ورود' ? 'در انبار' : (string)$status);

$summaryRows = [
    ['کشتی', $kotazhInfo['shipName']],
    ['انبار', $kotazhInfo['warehouseName']],
    ['نوع بار', $kotazhInfo['cargoType']],
    ['شرکت حمل', $kotazhInfo['shippingCompany']],
    ['مالک بار', $kotazhInfo['cargoOwner']],
    ['شماره کوتاژ', $kotazhInfo['number']],
    ['تناژ کل (تن)', number_format($kotazhInfo['totalTonnage'], 2)],
    ['تناژ بارگیری‌شده (تن)', number_format($kotazhInfo['loadedTonnage'], 2)],
    ['تناژ باقی‌مانده (تن)', number_format($kotazhInfo['remainingTonnage'], 2)],
    ['درصد بارگیری', number_format($kotazhInfo['percentageLoaded'], 2) . '٪'],
    ['تعداد حواله خروج', (string)$kotazhInfo['exitVoucherCount']],
    ['میانگین وزن هر حواله (تن)', number_format($kotazhInfo['avgVoucherWeight'], 2)],
    ['وضعیت کوتاژ', $kotazhInfo['isActive'] ? 'فعال' : 'غیرفعال'],
];

$tableHeaders = ['شماره حواله', 'ساعت ورود', 'وزن خالص', 'قبض باسکول', 'کسری بار', 'اضافه بار', 'ساعت خروج', 'تاریخ خروج', 'وضعیت'];
$tableRows = array_map(static fn(array $c) => [
    $c['trackingNumber'] ?? '',
    $c['entryTime'] ?? '',
    $c['netWeight'] ?? '',
    $c['scaleReceiptNumber'] ?? '',
    $c['shortageWeight'] ?? '',
    $c['excessWeight'] ?? '',
    $c['exitTime'] ?? '',
    $c['exitDate'] ?? '',
    $statusLabel($c['status'] ?? ''),
], $cargoInfo);

$baseFilename = 'kotazh-' . $kotazh . '-' . date('Y-m-d-His');

try {
    qr_generate_export($format, $kotazh, $baseFilename, $summaryRows, $tableHeaders, $tableRows);
} catch (\Throwable $e) {
    Logger::getInstance()->error(sprintf(
        'Quota Reports export.php (format=%s): [%s] %s in %s:%d',
        $format, get_class($e), $e->getMessage(), $e->getFile(), $e->getLine()
    ));
    qr_export_fail('تولید خروجی با خطا مواجه شد. جزئیات در لاگ سرور ثبت شد.');
}

/**
 * @param array<int, array{0: string, 1: string}> $summaryRows
 * @param array<int, string> $tableHeaders
 * @param array<int, array<int, string>> $tableRows
 */
function qr_generate_export(string $format, string $kotazh, string $baseFilename, array $summaryRows, array $tableHeaders, array $tableRows): void {
    switch ($format) {
        case 'excel':
            $spreadsheet = new Spreadsheet();
            $sheet = $spreadsheet->getActiveSheet();
            $sheet->setRightToLeft(true);
            $sheet->setTitle('گزارش کوتاژ');

            $sheet->setCellValue('A1', 'گزارش آماری کوتاژ ' . $kotazh);
            $sheet->mergeCells('A1:B1');
            $sheet->getStyle('A1')->getFont()->setBold(true)->setSize(14);

            $row = 3;
            foreach ($summaryRows as [$label, $value]) {
                $sheet->setCellValue("A{$row}", $label);
                $sheet->setCellValueExplicit("B{$row}", qr_safe((string)$value), DataType::TYPE_STRING);
                $sheet->getStyle("A{$row}")->getFont()->setBold(true);
                $row++;
            }

            $row += 1;
            $headerRow = $row;
            foreach ($tableHeaders as $col => $label) {
                $cellCoord = $sheet->getColumnDimensionByColumn($col + 1)->getColumnIndex() . $headerRow;
                $sheet->setCellValue($cellCoord, $label);
            }
            $sheet->getStyle("A{$headerRow}:I{$headerRow}")->getFont()->setBold(true);
            $sheet->getStyle("A{$headerRow}:I{$headerRow}")->getFill()
                ->setFillType(Fill::FILL_SOLID)->getStartColor()->setRGB('F2F4F7');
            $row++;

            foreach ($tableRows as $dataRow) {
                $col = 'A';
                foreach ($dataRow as $value) {
                    $sheet->setCellValueExplicit("{$col}{$row}", qr_safe((string)$value), DataType::TYPE_STRING);
                    $col++;
                }
                $row++;
            }

            foreach (range('A', 'I') as $col) {
                $sheet->getColumnDimension($col)->setAutoSize(true);
            }
            $sheet->getStyle('A1:I' . ($row - 1))->getAlignment()->setHorizontal(Alignment::HORIZONTAL_RIGHT);

            header('Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
            header('Content-Disposition: attachment; filename="' . $baseFilename . '.xlsx"');
            header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
            $writer = new Xlsx($spreadsheet);
            $writer->save('php://output');
            exit;

        case 'html':
            header('Content-Type: text/html; charset=UTF-8');
            header('Content-Disposition: attachment; filename="' . $baseFilename . '.html"');
            header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
            require __DIR__ . '/_export_report_template.php';
            exit;

        default:
            http_response_code(400);
            header('Content-Type: text/plain; charset=UTF-8');
            echo 'فرمت خروجی نامعتبر است.';
            exit;
    }
}
