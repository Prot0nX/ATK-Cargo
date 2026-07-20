<?php
header('Content-Type: application/json; charset=utf-8');
error_reporting(E_ALL);
ini_set('display_errors', 0);

require_once __DIR__ . '/config/config.php';

// تنظیم مسیر فایل لاگ
$logFile = __DIR__ . '/logs/scale_receipt_check.log';

function writeLog($message) {
    global $logFile;
    $timestamp = date('Y-m-d H:i:s');
    $logMessage = "[$timestamp] $message" . PHP_EOL;
    file_put_contents($logFile, $logMessage, FILE_APPEND);
}

function sanitize_input($input) {
    return htmlspecialchars(trim($input), ENT_QUOTES, 'UTF-8');
}

function send_json_response($data, $status_code = 200) {
    http_response_code($status_code);
    echo json_encode($data, JSON_UNESCAPED_UNICODE);
    exit();
}

function is_valid_scale_receipt($scaleReceiptNumber) {
    // بررسی عددی بودن
    if (!ctype_digit($scaleReceiptNumber)) {
        return false;
    }

    // بررسی طول (8 رقمی بودن)
    if (strlen($scaleReceiptNumber) !== 8) {
        return false;
    }

    // بررسی دو رقم اول
    $firstTwoDigits = substr($scaleReceiptNumber, 0, 2);
    if ($firstTwoDigits < '44' || $firstTwoDigits > '55') {
        return false;
    }

    return true;
}


$scaleReceiptNumber = sanitize_input($_GET['scaleReceiptNumber'] ?? '');

if (empty($scaleReceiptNumber)) {
    send_json_response(['error' => 'شماره قبض باسکول الزامی است.'], 400);
}

if (!is_valid_scale_receipt($scaleReceiptNumber)) {
    send_json_response(['error' => 'شماره قبض باسکول معتبر نیست. لطفاً دوباره اسکن کنید.'], 400);
}

try {
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
    if ($conn->connect_error) {
        throw new Exception("خطا در اتصال به پایگاه داده");
    }

    $conn->set_charset("utf8mb4");

    $stmt = $conn->prepare("SELECT trackingNumber, netWeight, loadingQuotaNumber FROM CargoInfo WHERE scaleReceiptNumber = ? LIMIT 1");
    if (!$stmt) {
        throw new Exception("خطا در آماده‌سازی دستور SQL");
    }

    $stmt->bind_param("s", $scaleReceiptNumber);
    if (!$stmt->execute()) {
        throw new Exception("خطا در اجرای دستور SQL");
    }

    $result = $stmt->get_result();
    if ($row = $result->fetch_assoc()) {
        send_json_response([
            'exists' => true,
            'message' => 'شماره قبض باسکول تکراری است.',
            'trackingNumber' => $row['trackingNumber'],
            'netWeight' => $row['netWeight'],
            'loadingQuotaNumber' => $row['loadingQuotaNumber']
        ]);
    } else {
        send_json_response(['exists' => false, 'message' => 'شماره قبض باسکول معتبر است.']);
    }

} catch (Exception $e) {
    send_json_response(['error' => $e->getMessage()], 500);
} finally {
    if (isset($stmt)) $stmt->close();
    if (isset($conn) && $conn instanceof mysqli) {
        $conn->close();
    }
}
?>