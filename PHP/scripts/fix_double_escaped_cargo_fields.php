<?php
// PHP/scripts/fix_double_escaped_cargo_fields.php
//
// یک‌بارمصرف: رفع مقادیر escape‌شده‌ای که قبل از رفع DEEP_CODE_AUDIT.md #۱۳
// از طریق CargoController::updateCargoInfo (PATCH cargo/update) با
// htmlspecialchars ذخیره شده بودند. آن endpoint دیگر escape نمی‌کند، اما
// ردیف‌هایی که قبلاً از این مسیر ویرایش شده‌اند ممکن است هنوز &amp; / &lt; /
// &gt; / &quot; / &#039; داشته باشند.
//
// این اسکریپت فقط ستون‌هایی از CargoInfo را بررسی می‌کند که واقعاً از طریق
// validateStringField (تنها منبع htmlspecialchars در مسیر نوشتن) عبور
// می‌کردند: trackingNumber, numberOfPeople, entryTime, scaleReceiptNumber,
// exitTime, exitDate, status, confirm.
//
// پیش‌فرض: dry-run — فقط تعداد و نمونه‌ی ردیف‌های مشکوک را چاپ می‌کند،
// هیچ UPDATE ای اجرا نمی‌شود. برای اعمال واقعی، پرچم --apply لازم است.
//
// نصب: یک‌بار دستی روی سرور اجرا شود، نه از طریق cron:
//   php /path/to/PHP/scripts/fix_double_escaped_cargo_fields.php            (بررسی، بدون تغییر)
//   php /path/to/PHP/scripts/fix_double_escaped_cargo_fields.php --apply    (اعمال واقعی)
//
// هشدار: قبل از --apply حتماً از جدول CargoInfo بکاپ بگیرید. html_entity_decode
// روی مقادیری که کاربر واقعاً و عمداً رشته‌ی «&amp;» را تایپ کرده باشد هم اثر
// می‌کند (این حالت در داده‌ی این جدول بسیار بعید است چون این ستون‌ها شماره/
// تاریخ/وضعیت هستند، نه متن آزاد)، پس خروجی dry-run را قبل از اعمال دستی
// مرور کنید.

declare(strict_types=1);

if (PHP_SAPI !== 'cli') {
    http_response_code(403);
    exit('Forbidden: این اسکریپت فقط از طریق CLI قابل اجراست.');
}

require_once __DIR__ . '/../src/bootstrap.php';

use App\Core\Database;

// همان ستون‌هایی که validateStringField روی آن‌ها اجرا می‌شد
const AFFECTED_COLUMNS = [
    'trackingNumber', 'numberOfPeople', 'entryTime',
    'scaleReceiptNumber', 'exitTime', 'exitDate', 'status', 'confirm',
];

const HTML_ENTITY_MARKERS = ['&amp;', '&lt;', '&gt;', '&quot;', '&#039;'];

$apply = in_array('--apply', $argv, true);

$pdo = Database::getInstance()->getPdoConnection();

$whereClauses = array_map(
    static fn(string $col): string => "$col LIKE '%&amp;%' OR $col LIKE '%&lt;%' OR $col LIKE '%&gt;%' OR $col LIKE '%&quot;%' OR $col LIKE '%&#039;%'",
    AFFECTED_COLUMNS
);
$sql = "SELECT id, " . implode(', ', AFFECTED_COLUMNS) . "\n"
     . "FROM CargoInfo\n"
     . "WHERE (" . implode(') OR (', $whereClauses) . ")";

$stmt = $pdo->query($sql);
$suspectRows = $stmt->fetchAll();

$total = count($suspectRows);
if ($total === 0) {
    echo "OK: هیچ مقدار escape‌شده‌ای در CargoInfo یافت نشد؛ چیزی برای اصلاح نیست.\n";
    exit(0);
}

echo "$total ردیف با مقدار escape‌شده در CargoInfo یافت شد.\n";
echo $apply ? "حالت: --apply (تغییرات واقعاً اعمال می‌شوند)\n\n" : "حالت: dry-run (فقط نمایش، بدون تغییر — برای اعمال از --apply استفاده کنید)\n\n";

$updateStmt = $apply
    ? $pdo->prepare("UPDATE CargoInfo SET " . implode(', ', array_map(static fn(string $c) => "$c = :$c", AFFECTED_COLUMNS)) . " WHERE id = :id")
    : null;

$fixed = 0;
foreach ($suspectRows as $row) {
    $id = (int)$row['id'];
    $decoded = [];
    $changed = false;

    foreach (AFFECTED_COLUMNS as $col) {
        $original = (string)($row[$col] ?? '');
        $decodedValue = html_entity_decode($original, ENT_QUOTES, 'UTF-8');
        $decoded[$col] = $decodedValue;
        if ($decodedValue !== $original) {
            $changed = true;
        }
    }

    if (!$changed) {
        continue;
    }

    echo "id=$id:\n";
    foreach (AFFECTED_COLUMNS as $col) {
        if ($decoded[$col] !== (string)($row[$col] ?? '')) {
            echo "  $col: \"{$row[$col]}\" -> \"{$decoded[$col]}\"\n";
        }
    }

    if ($apply && $updateStmt !== null) {
        $params = $decoded;
        $params['id'] = $id;
        $updateStmt->execute($params);
    }

    $fixed++;
}

echo "\n$fixed از $total ردیف نیاز به اصلاح واقعی داشتند (بقیه فقط الگوی مشکوک داشتند ولی html_entity_decode چیزی تغییر نداد).\n";

if (!$apply) {
    echo "این یک dry-run بود — هیچ تغییری در دیتابیس اعمال نشد. برای اعمال واقعی: php " . basename(__FILE__) . " --apply\n";
} else {
    echo "تغییرات اعمال شد.\n";
}
