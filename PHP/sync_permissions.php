<?php
/**
 * ATK-Cargo Permission Sync Endpoint
 * به‌روزرسانی زنده سطح دسترسی کاربر بدون نیاز به Logout/Login
 */

date_default_timezone_set('Asia/Tehran');
header('Content-Type: application/json; charset=UTF-8');
error_reporting(E_ALL);
ini_set('display_errors', 0);
require_once __DIR__ . '/config/config.php';
require_once __DIR__ . '/SessionManager.php';

// Security Headers
header('X-Content-Type-Options: nosniff');
header('X-Frame-Options: DENY');
header('Cache-Control: no-store, no-cache, must-revalidate');

function send_response(bool $success, string $message, array $extra = [], int $code = 200): void {
    http_response_code($code);
    echo json_encode(array_merge(['success' => $success, 'message' => $message], $extra), JSON_UNESCAPED_UNICODE);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    send_response(false, 'Only POST method is allowed.', [], 405);
}

$input  = json_decode(file_get_contents('php://input'), true);
if (json_last_error() !== JSON_ERROR_NONE) {
    send_response(false, 'Invalid JSON payload.', [], 400);
}

$username     = isset($input['username'])      ? trim($input['username'])      : '';
$deviceId     = isset($input['deviceId'])      ? trim($input['deviceId'])      : '';
$sessionToken = isset($input['session_token']) ? trim($input['session_token']) : '';

if (empty($username)) {
    send_response(false, 'نام کاربری الزامی است.', [], 400);
}

try {
    $sessionManager = new SessionManager();

    // ۱. اعتبارسنجی نشست
    if (!$sessionManager->isSessionActive($username, $deviceId)) {
        send_response(false, 'نشست کاربر منقضی شده است.', [], 401);
    }

    // ۲. دریافت نوع کاربر از دیتابیس
    $conn = getDbConnection();
    $stmt = $conn->prepare("SELECT userType FROM Users WHERE username = ? LIMIT 1");
    $stmt->bind_param('s', $username);
    $stmt->execute();
    $row = $stmt->get_result()->fetch_assoc();

    if (!$row) {
        send_response(false, 'کاربر یافت نشد.', [], 404);
    }

    $userType = $row['userType'];

    // ۳. بارگذاری لایه‌ای permissions (کاربر اختصاصی → نقش → خالی)
    $permissions_file = __DIR__ . '/config/permissions.json';
    $userPermissions  = [];

    if (file_exists($permissions_file)) {
        $allData = json_decode(file_get_contents($permissions_file), true);

        if (isset($allData['roles'])) {
            // ساختار جدید: اولویت با تنظیمات اختصاصی کاربر
            if (isset($allData['users'][$username])) {
                $userPermissions = $allData['users'][$username];
            } elseif (isset($allData['roles'][$userType])) {
                $userPermissions = $allData['roles'][$userType];
            }
        } else {
            // ساختار قدیمی (سازگاری با نسخه‌های قبلی)
            $userPermissions = $allData[$userType] ?? [];
        }
    }

    // ۴. ارسال پاسخ
    send_response(true, 'Permissions synced successfully.', [
        'userType'    => $userType,
        'permissions' => $userPermissions,
    ]);

} catch (Exception $e) {
    error_log('[sync_permissions] Error: ' . $e->getMessage());
    send_response(false, 'خطای داخلی سرور.', [], 500);
}
?>
