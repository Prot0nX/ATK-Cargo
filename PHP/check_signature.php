<?php

declare(strict_types=1);

// تنظیم هدرهای امنیتی
header('Content-Type: application/json; charset=UTF-8');
header('X-Content-Type-Options: nosniff');
header('X-Frame-Options: DENY');
header('X-XSS-Protection: 1; mode=block');
header('Referrer-Policy: strict-origin-when-cross-origin');
header('Cache-Control: no-cache, no-store, must-revalidate');
header('Pragma: no-cache');
header('Expires: 0');

// تابع برای ارسال پاسخ JSON و خروج
function sendJsonResponse(array $data, int $httpCode = 200): void {
    http_response_code($httpCode);
    echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
    exit;
}

// تابع برای اعتبارسنجی امضا
function validateSignature(string $signature): bool {
    // بررسی طول امضا (SHA-256 باید 64 کاراکتر باشد)
    if (strlen($signature) !== 64) {
        return false;
    }
    
    // بررسی اینکه فقط شامل کاراکترهای هگزادسیمال باشد
    return ctype_xdigit($signature);
}

// تابع برای اعتبارسنجی نام بسته
function validatePackageName(?string $packageName): bool {
    if (empty($packageName)) {
        return false;
    }
    
    // بررسی فرمت نام بسته Android
    return preg_match('/^[a-zA-Z][a-zA-Z0-9_]*(?:\.[a-zA-Z][a-zA-Z0-9_]*)*$/', $packageName) === 1;
}

// بررسی روش درخواست
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendJsonResponse(['error' => 'روش درخواست غیرمجاز'], 405);
}

// بررسی Content-Type
$contentType = $_SERVER['CONTENT_TYPE'] ?? '';
if (strpos($contentType, 'application/json') === false) {
    sendJsonResponse(['error' => 'نوع محتوای نامعتبر'], 400);
}

// دریافت و اعتبارسنجی داده‌های JSON
try {
    $rawInput = file_get_contents('php://input');
    
    if (empty($rawInput)) {
        sendJsonResponse(['error' => 'بدنه درخواست خالی است'], 400);
    }
    
    $input = json_decode($rawInput, true, 512, JSON_THROW_ON_ERROR);
} catch (JsonException $e) {
    error_log('JSON decode error: ' . $e->getMessage());
    sendJsonResponse(['error' => 'فرمت JSON نامعتبر'], 400);
}

// بررسی وجود فیلدهای ضروری
if (!isset($input['app_signature']) || !is_string($input['app_signature'])) {
    sendJsonResponse(['error' => 'امضای برنامه ارسال نشده است'], 400);
}

$receivedSignature = trim($input['app_signature']);

// اعتبارسنجی امضا
if (!validateSignature($receivedSignature)) {
    error_log('Invalid signature format received: ' . $receivedSignature);
    sendJsonResponse(['error' => 'فرمت امضای نامعتبر'], 400);
}

// اعتبارسنجی اختیاری نام بسته و نسخه (برای سازگاری با کلاینت Kotlin)
$packageName = $input['app_package'] ?? null;
$appVersion = $input['app_version'] ?? null;

if ($packageName !== null && !validatePackageName($packageName)) {
    error_log('Invalid package name received: ' . $packageName);
    sendJsonResponse(['error' => 'نام بسته نامعتبر'], 400);
}

// بارگذاری تنظیمات پایگاه داده
require_once 'config/config.php';

$conn = null;
$stmt = null;

try {
    // اتصال به پایگاه داده
    $conn = getDbConnection();

    if ($conn->connect_error) {
        throw new Exception('Database connection failed: ' . $conn->connect_error);
    }

    // تنظیم charset برای جلوگیری از حملات تزریق
    $conn->set_charset('utf8mb4');

    // آماده‌سازی کوئری با بهینه‌سازی
    $query = "SELECT 1 FROM SignChecker WHERE app_signature = ? LIMIT 1";
    $stmt = $conn->prepare($query);
    
    if (!$stmt) {
        throw new Exception('Statement preparation failed: ' . $conn->error);
    }

    // اجرای کوئری
    $stmt->bind_param("s", $receivedSignature);
    
    if (!$stmt->execute()) {
        throw new Exception('Query execution failed: ' . $stmt->error);
    }

    $result = $stmt->get_result();
    $isValid = $result->num_rows > 0;

    // ثبت لاگ برای درخواست‌های معتبر (اختیاری)
    if ($isValid && $packageName !== null) {
        error_log("Valid signature check for package: {$packageName}, version: {$appVersion}");
    }

    // ارسال پاسخ
    sendJsonResponse(['is_valid' => $isValid]);

} catch (Exception $e) {
    // ثبت جزئیات خطا در لاگ
    error_log('Signature verification error: ' . $e->getMessage() . ' | IP: ' . ($_SERVER['REMOTE_ADDR'] ?? 'unknown'));
    
    // ارسال پاسخ خطای عمومی
    sendJsonResponse(['error' => 'خطای سرور رخ داده است'], 500);
    
} finally {
    // تمیز کردن منابع
    if ($stmt !== null) {
        $stmt->close();
    }
    if ($conn !== null) {
        $conn->close();
    }
}
?>
