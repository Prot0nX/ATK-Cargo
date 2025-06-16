<?php
// تنظیم منطقه زمانی تهران
date_default_timezone_set('Asia/Tehran');

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

require_once __DIR__ . '/SessionManager.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    exit(0);
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'فقط درخواست POST مجاز است']);
    exit;
}

try {
    // دریافت داده‌های ورودی
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!isset($input['username']) || empty($input['username'])) {
        echo json_encode(['success' => false, 'message' => 'نام کاربری الزامی است']);
        exit;
    }
    
    $user = trim($input['username']);
    $deviceId = isset($input['deviceId']) ? trim($input['deviceId']) : null;
    
    // استفاده از SessionManager برای مدیریت خروج
    $sessionManager = new SessionManager();
    
    // غیرفعال کردن جلسه کاربر
    $result = $sessionManager->deactivateSession($user, $deviceId);
    
    echo json_encode($result);
    
} catch (PDOException $e) {
    error_log("Database error in logout: " . $e->getMessage());
    echo json_encode([
        'success' => false,
        'message' => 'خطا در اتصال به دیتابیس'
    ]);
} catch (Exception $e) {
    error_log("General error in logout: " . $e->getMessage());
    echo json_encode([
        'success' => false,
        'message' => 'خطای سرور'
    ]);
}
?>