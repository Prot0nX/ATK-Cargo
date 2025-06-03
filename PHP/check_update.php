<?php
header('Content-Type: application/json');

$api_key = $_GET['api_key'] ?? '';
if ($api_key !== 'atk_nk_9290VV42-38XQ02DI-F2WY4L2K-EJA7V682') {
    http_response_code(403);
    echo json_encode(['error' => 'دسترسی غیرمجاز']);
    exit;
}

$config = include 'update_config.php';
$currentVersion = $_GET['current_version'] ?? '';
if (empty($currentVersion)) {
    http_response_code(400);
    echo json_encode(['error' => 'نسخه فعلی مشخص نشده است.']);
    exit;
}

// Check version compatibility
$hasUpdate = version_compare($currentVersion, $config['latest_version'], '<');
$isCompatible = version_compare($currentVersion, $config['min_required_version'], '>=');

if ($hasUpdate && !$isCompatible) {
    http_response_code(426);
    echo json_encode([
        'error' => 'نسخه برنامه شما قدیمی است. لطفاً برنامه را از طریق مارکت به‌روزرسانی کنید.',
        'forceUpdate' => true
    ]);
    exit;
}

// Build response with additional update info
$response = [
    'hasUpdate' => $hasUpdate,
    'latestVersion' => $config['latest_version'],
    'downloadUrl' => $hasUpdate ? $config['download_url'] : '',
    'changeLog' => $hasUpdate ? $config['change_log'] : [],
    'minRequiredVersion' => $config['min_required_version'],
    'updateInfo' => $hasUpdate ? [
        'priority' => $config['update_priority'] ?? 'normal',
        'message' => $config['update_message'] ?? '',
        'forceUpdate' => $config['force_update'] ?? false,
        'size' => $config['update_size'] ?? '0',
        'releaseDate' => $config['release_date'] ?? '',
        'minAndroidVersion' => $config['version_constraints']['min_android_version'] ?? 21,
    ] : null
];

// Log update check
$log_message = sprintf(
    "%s - Check for update: Current Version: %s, Latest Version: %s, Has Update: %s\n",
    date('Y-m-d H:i:s'),
    $currentVersion,
    $config['latest_version'],
    $hasUpdate ? 'Yes' : 'No'
);
file_put_contents('update_checks.log', $log_message, FILE_APPEND);

echo json_encode($response);
?>