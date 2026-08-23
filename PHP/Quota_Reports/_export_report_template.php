<?php
// PHP/Quota Reports/_export_report_template.php — قالب خروجی HTML؛ فقط از export.php require می‌شود، نه مستقیم.
declare(strict_types=1);
?>
<!DOCTYPE html>
<html lang="fa" dir="rtl">
<head>
<meta charset="UTF-8">
<title>گزارش کوتاژ <?= e($kotazh) ?></title>
<style>
    body { font-family: 'Vazirmatn', Tahoma, Arial, sans-serif; direction: rtl; color: #14171c; font-size: 12px; }
    h1 { font-size: 16px; margin: 0 0 4px; }
    p.muted { color: #656d79; margin: 0 0 16px; font-size: 11px; }
    table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
    td, th { padding: 5px 8px; border: 1px solid #c8cdd6; text-align: right; }
    .summary td:first-child { font-weight: bold; background: #f1f3f6; width: 40%; }
    thead th { background: #eaf1fe; font-weight: bold; }
    tbody tr:nth-child(even) { background: #f6f7f9; }
</style>
</head>
<body>
    <h1>گزارش آماری کوتاژ <?= e($kotazh) ?></h1>
    <p class="muted">تاریخ تولید گزارش: <?= e(date('Y-m-d H:i')) ?></p>

    <table class="summary">
        <?php foreach ($summaryRows as [$label, $value]): ?>
        <tr>
            <td><?= e((string)$label) ?></td>
            <td><?= e((string)$value) ?></td>
        </tr>
        <?php endforeach; ?>
    </table>

    <table>
        <thead>
            <tr>
                <?php foreach ($tableHeaders as $header): ?>
                <th><?= e($header) ?></th>
                <?php endforeach; ?>
            </tr>
        </thead>
        <tbody>
            <?php if (empty($tableRows)): ?>
            <tr><td colspan="<?= count($tableHeaders) ?>" style="text-align:center; color:#656d79;">حواله‌ای برای این کوتاژ ثبت نشده است.</td></tr>
            <?php else: ?>
                <?php foreach ($tableRows as $dataRow): ?>
                <tr>
                    <?php foreach ($dataRow as $value): ?>
                    <td><?= e((string)$value) ?></td>
                    <?php endforeach; ?>
                </tr>
                <?php endforeach; ?>
            <?php endif; ?>
        </tbody>
    </table>
</body>
</html>
