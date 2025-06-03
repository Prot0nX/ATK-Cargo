<?php

declare(strict_types=1);

header('Content-Type: application/json; charset=UTF-8');
header('X-Content-Type-Options: nosniff');
header('X-Frame-Options: DENY');
header('X-XSS-Protection: 1; mode=block');

// مجاز کردن فقط درخواست‌های POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405); // Method Not Allowed
    echo json_encode(['error' => 'روش درخواست غیرمجاز']);
    exit;
}

// دریافت داده‌های JSON از بدنه درخواست
$input = json_decode(file_get_contents('php://input'), true);

if (!isset($input['app_signature'])) {
    http_response_code(400); // Bad Request
    echo json_encode(['error' => 'امضای برنامه ارسال نشده است']);
    exit;
}

$receivedSignature = $input['app_signature'];

require_once 'config/config.php';

try {
    $conn = getDbConnection();

    if ($conn->connect_error) {
        throw new Exception('خطا در اتصال به پایگاه داده');
    }

    // تهیه کوئری برای بررسی امضا
    $stmt = $conn->prepare("SELECT COUNT(*) as count FROM SignChecker WHERE app_signature = ?");
    
    if (!$stmt) {
        throw new Exception('خطا در آماده‌سازی کوئری');
    }

    $stmt->bind_param("s", $receivedSignature);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($row = $result->fetch_assoc()) {
        $isValid = $row['count'] > 0;
        echo json_encode(['is_valid' => $isValid]);
    } else {
        echo json_encode(['is_valid' => false]);
    }

    $stmt->close();
    $conn->close();
} catch (Exception $e) {
    // بازگرداندن پیام خطای کلی به کاربر
    http_response_code(500); // Internal Server Error
    echo json_encode(['error' => 'خطای سرور رخ داده است']);

    // ثبت اطلاعات دقیق خطا در لاگ (log) برای مدیر سیستم
    error_log('Error: ' . $e->getMessage());
}
?>
