<?php
// تنظیمات اولیه و هدرهای امنیتی
ini_set('display_errors', 0);
date_default_timezone_set('Asia/Tehran');
error_reporting(0);

// بررسی متد درخواست
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'روش درخواست معتبر نیست']);
    exit;
}

// تنظیم هدرهای امنیتی
header('Content-Type: application/json');
header('X-Content-Type-Options: nosniff');
header('X-Frame-Options: DENY');
header('Content-Security-Policy: default-src \'self\'');

require_once __DIR__ . '/config/config.php';

// دریافت کلید لایسنس از پارامترهای GET
$licenseKey = trim($_GET['licenseKey'] ?? '');

// اعتبارسنجی کلید لایسنس
if (empty($licenseKey) || strlen($licenseKey) !== 32) {
    echo json_encode([
        'success' => false,
        'message' => 'کلید لایسنس نامعتبر است'
    ]);
    exit;
}

try {
    $conn = getDbConnection();
    $stmt = $conn->prepare("SELECT * FROM licenses WHERE license_key = ?");
    $stmt->bind_param("s", $licenseKey);
    
    if (!$stmt->execute()) {
        throw new Exception("خطا در اجرای کوئری");
    }
    
    $result = $stmt->get_result();
    
    if ($result->num_rows > 0) {
        $license = $result->fetch_assoc();
        echo json_encode([
            'success' => true,
            'message' => 'اطلاعات لایسنس با موفقیت دریافت شد',
            'license' => [
                'licenseKey' => htmlspecialchars($license['license_key']),
                'companyName' => htmlspecialchars($license['company_name']),
                'activationDate' => $license['activation_date'],
                'isActive' => (bool)$license['is_active'],
                'createdAt' => $license['created_at'],
                'lastCheck' => $license['last_check']
            ]
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'لایسنس مورد نظر یافت نشد'
        ]);
    }
} catch (Exception $e) {
    error_log("خطا در بررسی لایسنس: " . $e->getMessage());
    echo json_encode([
        'success' => false,
        'message' => 'خطای سیستمی رخ داده است'
    ]);
}

$stmt->close();
$conn->close(); 