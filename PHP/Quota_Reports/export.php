<?php
// PHP/Quota Reports/export.php — خروجی واقعی Excel/HTML گزارش یک کوتاژ یا خلاصه‌ی همه‌ی کوتاژها.

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

use App\Core\Logger;
use App\Core\Request;
use App\Repositories\CargoRepository;
use App\Services\QuotaService;
use PhpOffice\PhpSpreadsheet\Cell\Coordinate;
use PhpOffice\PhpSpreadsheet\Cell\DataType;
use PhpOffice\PhpSpreadsheet\Spreadsheet;
use PhpOffice\PhpSpreadsheet\Style\Alignment;
use PhpOffice\PhpSpreadsheet\Style\Fill;
use PhpOffice\PhpSpreadsheet\Writer\Xlsx;

qr_require_auth_page();

function qr_export_fail(string $message, int $httpCode = 500): never {
    if (!headers_sent()) {
        http_response_code($httpCode);
        header('Content-Type: text/plain; charset=UTF-8');
    }
    echo $message;
    exit;
}

function qr_safe(?string $value): string {
    $value = (string)$value;
    if ($value !== '' && strpbrk($value[0], "=+-@\t\r") !== false) {
        return "'" . $value;
    }
    return $value;
}

$request = new Request();
$format = (string)$request->get('format', '');
$scope = (string)$request->get('scope', 'quota');

if ($format === 'excel' && !class_exists(Spreadsheet::class)) {
    qr_export_fail('کتابخانه‌ی مورد نیاز این خروجی نصب نشده است. روی این سرور دستور «composer install» را در پوشه‌ی PHP اجرا کنید.');
}

if ($scope === 'summary') {
    if ($format !== 'excel') {
        qr_export_fail('خروجی کامل داشبورد فقط برای Excel پشتیبانی می‌شود.', 400);
    }

    try {
        $quotas = (new QuotaService())->getQuotasSummary();
    } catch (\Throwable $e) {
        Logger::getInstance()->error(sprintf(
            'Quota Reports export.php (summary): [%s] %s in %s:%d',
            get_class($e), $e->getMessage(), $e->getFile(), $e->getLine()
        ));
        qr_export_fail('خطا در بارگذاری خلاصه‌ی کوتاژها رخ داد.');
    }

    $tableHeaders = ['شماره کوتاژ', 'کشتی', 'انبار', 'شرکت حمل', 'کالا', 'تناژ کل (تن)', 'بارگیری‌شده (تن)', 'باقی‌مانده (تن)', 'درصد بارگیری', 'تعداد حواله خروج', 'وضعیت'];
    $tableRows = array_map(static fn(array $q) => [
        $q['number'],
        $q['shipName'],
        $q['warehouse'],
        $q['shippingCompany'],
        $q['cargoType'],
        number_format($q['totalTonnage'], 2),
        number_format($q['loadedTonnage'], 2),
        number_format($q['remainingTonnage'], 2),
        number_format($q['percentageLoaded'], 2) . '٪',
        (string)$q['exitVoucherCount'],
        $q['isActive'] ? 'فعال' : 'غیرفعال',
    ], $quotas);

    try {
        qr_stream_excel('گزارش خلاصه‌ی همه‌ی کوتاژها', [], $tableHeaders, $tableRows, 'quotas-summary-' . date('Y-m-d-His') . '.xlsx');
    } catch (\Throwable $e) {
        Logger::getInstance()->error(sprintf(
            'Quota Reports export.php (summary excel): [%s] %s in %s:%d',
            get_class($e), $e->getMessage(), $e->getFile(), $e->getLine()
        ));
        qr_export_fail('تولید خروجی با خطا مواجه شد. جزئیات در لاگ سرور ثبت شد.');
    }
    exit;
}

$kotazh = trim((string)$request->get('kotazh', ''));

if (!preg_match('/^\d{8}$/', $kotazh)) {
    qr_export_fail('فرمت کوتاژ نامعتبر است.', 422);
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

/** @param array<int, array{0: string, 1: string}> $summaryRows @param array<int, string> $tableHeaders @param array<int, array<int, string>> $tableRows */
function qr_generate_export(string $format, string $kotazh, string $baseFilename, array $summaryRows, array $tableHeaders, array $tableRows): void {
    switch ($format) {
        case 'excel':
            qr_stream_excel('گزارش آماری کوتاژ ' . $kotazh, $summaryRows, $tableHeaders, $tableRows, $baseFilename . '.xlsx');
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

/** @param array<int, array{0: string, 1: string}> $summaryRows @param array<int, string> $tableHeaders @param array<int, array<int, string>> $tableRows */
function qr_stream_excel(string $title, array $summaryRows, array $tableHeaders, array $tableRows, string $filename): void {
    $lastCol = Coordinate::stringFromColumnIndex(count($tableHeaders));

    $spreadsheet = new Spreadsheet();
    $sheet = $spreadsheet->getActiveSheet();
    $sheet->setRightToLeft(true);
    $sheet->setTitle('گزارش کوتاژ');

    $sheet->setCellValue('A1', $title);
    $sheet->mergeCells("A1:{$lastCol}1");
    $sheet->getStyle('A1')->getFont()->setBold(true)->setSize(14);

    $row = 3;
    foreach ($summaryRows as [$label, $value]) {
        $sheet->setCellValue("A{$row}", $label);
        $sheet->setCellValueExplicit("B{$row}", qr_safe((string)$value), DataType::TYPE_STRING);
        $sheet->getStyle("A{$row}")->getFont()->setBold(true);
        $row++;
    }
    if (!empty($summaryRows)) {
        $row++;
    }

    $headerRow = $row;
    foreach ($tableHeaders as $col => $label) {
        $sheet->setCellValue(Coordinate::stringFromColumnIndex($col + 1) . $headerRow, $label);
    }
    $sheet->getStyle("A{$headerRow}:{$lastCol}{$headerRow}")->getFont()->setBold(true);
    $sheet->getStyle("A{$headerRow}:{$lastCol}{$headerRow}")->getFill()
        ->setFillType(Fill::FILL_SOLID)->getStartColor()->setRGB('F2F4F7');
    $row++;

    foreach ($tableRows as $dataRow) {
        $col = 1;
        foreach ($dataRow as $value) {
            $sheet->setCellValueExplicit(Coordinate::stringFromColumnIndex($col) . $row, qr_safe((string)$value), DataType::TYPE_STRING);
            $col++;
        }
        $row++;
    }

    foreach (range(1, count($tableHeaders)) as $colIndex) {
        $sheet->getColumnDimension(Coordinate::stringFromColumnIndex($colIndex))->setAutoSize(true);
    }
    $sheet->getStyle("A1:{$lastCol}" . ($row - 1))->getAlignment()->setHorizontal(Alignment::HORIZONTAL_RIGHT);

    header('Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
    header('Content-Disposition: attachment; filename="' . $filename . '"');
    header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
    (new Xlsx($spreadsheet))->save('php://output');
}
