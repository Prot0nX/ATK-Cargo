<?php
function getFileSize($filePath): string {
    if (!file_exists($filePath)) {
        return '0';
    }

    $bytes = filesize($filePath);
    $mb = round($bytes / (1024 * 1024), 1);
    return (string)$mb;
}

/**
 * هش SHA-256 فایل APK برای اعتبارسنجی سمت کلاینت قبل از نصب (C-3).
 * محاسبه‌ی این هش برای هر درخواست هزینه‌بر است (خواندن کامل فایل چندین‌ده‌مگابایتی)،
 * پس نتیجه در یک فایل کنار APK کش می‌شود و فقط با تغییر mtime دوباره محاسبه می‌شود.
 */
function getFileSha256(string $filePath): string {
    if (!file_exists($filePath)) {
        return '';
    }

    $cacheFile = $filePath . '.sha256';
    $mtime = (string)filemtime($filePath);

    if (file_exists($cacheFile)) {
        $cached = trim((string)file_get_contents($cacheFile));
        [$cachedMtime, $cachedHash] = array_pad(explode('|', $cached, 2), 2, '');
        if ($cachedMtime === $mtime && ctype_xdigit($cachedHash) && strlen($cachedHash) === 64) {
            return $cachedHash;
        }
    }

    $hash = hash_file('sha256', $filePath);
    if ($hash !== false) {
        @file_put_contents($cacheFile, $mtime . '|' . $hash);
    }
    return (string)$hash;
}

return [
    'latest_version' => '3.0.30',
    'download_url' => 'https://atk-nk.ir/Cargo/downloads/app-release.apk',
    'min_required_version' => '1.9',
    'min_allowed_version' => '3.0.29',
    'update_priority' => 'normal',
    'update_message' => 'نسخه جدید با امکانات جدید در دسترس است',
    'force_update' => false,
    'update_size' => getFileSize(__DIR__ . '/downloads/app-release.apk'),
    'sha256' => getFileSha256(__DIR__ . '/downloads/app-release.apk'),
    'release_date' => '1405/02/30',

    'version_constraints' => [
        'min_android_version' => 21,
        'min_app_version' => '1.9',
        'excluded_versions' => []
    ]
];
?>
 