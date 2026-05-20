<?php
function getFileSize($filePath): string {
    if (!file_exists($filePath)) {
        return '0';
    }
    
    $bytes = filesize($filePath);
    $mb = round($bytes / (1024 * 1024), 1);
    return (string)$mb;
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
    'release_date' => '1405/02/30', 
    
    'version_constraints' => [
        'min_android_version' => 21,
        'min_app_version' => '1.9',
        'excluded_versions' => []
    ]
];
?>
 