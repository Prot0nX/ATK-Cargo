<?php
// این فایل با include ساده (نه include_once) هم از MinVersionGate::enforce
// (سطح Router v2) و هم از AuthenticatesRequests::enforceMinAppVersion (سطح
// کنترلرهای قدیمی) صدا زده می‌شود؛ برای مسیرهایی که هر دو گیت در یک درخواست
// اجرا می‌شوند (اکثر route های v2 که مستقیم یک متد کنترلر قدیمی را صدا
// می‌زنند)، این فایل دوبار include می‌شد و بدون این گارد PHP Fatal error:
// Cannot redeclare می‌داد (کشف‌شده حین تست واقعی مرحله ۳.۱ — DEEP_CODE_AUDIT.md).
if (!function_exists('getFileSize')) {
    function getFileSize($filePath): string {
        if (!file_exists($filePath)) {
            return '0';
        }

        $bytes = filesize($filePath);
        $mb = round($bytes / (1024 * 1024), 1);
        return (string)$mb;
    }
}

if (!function_exists('getFileSha256')) {
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
}

return [
    'latest_version' => '4.0.1',
    'download_url' => 'https://atk-nk.ir/Cargo/test_api/downloads/app-release.apk',
    'min_required_version' => '1.9',
    'min_allowed_version' => '3.0.29',
    'update_priority' => 'normal',
    'update_message' => 'نسخه جدید با امکانات جدید در دسترس است',
    'force_update' => false,
    'update_size' => getFileSize(__DIR__ . '/downloads/app-release.apk'),
    'sha256' => getFileSha256(__DIR__ . '/downloads/app-release.apk'),
    'release_date' => '1405/05/24',

    'version_constraints' => [
        'min_android_version' => 21,
        'min_app_version' => '1.9',
        'excluded_versions' => []
    ]
];
?>
 