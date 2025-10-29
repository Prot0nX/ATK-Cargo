<?php
declare(strict_types=1);

// تنظیم نوع محتوا به JSON و تنظیمات مربوط به خطاها
header('Content-Type: application/json; charset=UTF-8');
header('X-Content-Type-Options: nosniff');
header('X-Frame-Options: DENY');
header('X-XSS-Protection: 1; mode=block');
error_reporting(E_ALL);
ini_set('display_errors', '0');

// وارد کردن تنظیمات پایگاه داده
require_once __DIR__ . '/config/config.php';

function sendJsonResponse($data, int $statusCode = 200): never
{
    http_response_code($statusCode);
    $json = json_encode($data, JSON_UNESCAPED_UNICODE);
    if ($json === false) {
        error_log("JSON encode error: " . json_last_error_msg());
        http_response_code(500);
        echo json_encode(['error' => 'JSON encoding failed: ' . json_last_error_msg()]);
        exit;
    }
    echo $json;
    exit;
}

function validateAndSanitizeInput(?string $input): string
{
    $sanitized = htmlspecialchars(trim($input ?? ''), ENT_QUOTES, 'UTF-8');
    if (empty($sanitized)) {
        throw new InvalidArgumentException('لطفاً شماره قبض باسکول را وارد کنید.');
    }
    return $sanitized;
}

try {
    // بررسی روش درخواست
    if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
        throw new Exception('روش درخواست نامعتبر است');
    }

    // لاگ درخواست
    error_log("🔍 Search Receipt Request - Receipt: " . ($_GET['receipt'] ?? 'NULL'));

    // برقراری اتصال به پایگاه داده
    $conn = getDbConnection();
    error_log("✅ Database connection established");

    // بررسی و پاکسازی پارامتر جستجو
    $receiptNumber = validateAndSanitizeInput($_GET['receipt'] ?? null);
    error_log("📝 Sanitized receipt number: $receiptNumber");

    // ساخت پرس و جو
    $query = "
        SELECT 
            id, trackingNumber, numberOfPeople, username, userType,
            entryTime, netWeight, scaleReceiptNumber,
            shortageWeight, excessWeight, exitTime, exitDate, status,
            confirm, confirmation,
            shipName, loadingWarehouse, cargoType, shippingCompany,
            loadingQuotaNumber
        FROM 
            CargoInfo
        WHERE 
            scaleReceiptNumber = ?
        LIMIT 1
    ";

    // آماده‌سازی و اجرای پرس و جو
    $stmt = $conn->prepare($query);
    if (!$stmt) {
        throw new Exception("آماده‌سازی پرس و جو ناموفق بود: " . $conn->error);
    }

    $stmt->bind_param("s", $receiptNumber);
    if (!$stmt->execute()) {
        throw new Exception("اجرای پرس و جو ناموفق بود: " . $stmt->error);
    }

    $result = $stmt->get_result();
    $cargoInfo = $result->fetch_assoc();
    
    error_log("📊 Query result count: " . ($cargoInfo ? "1" : "0"));

    if ($cargoInfo) {
        error_log("✅ Found cargo info with ID: " . $cargoInfo['id']);
        // فرمت‌بندی داده‌ها برای خروجی
        $formattedCargoInfo = [
            'id' => (int)$cargoInfo['id'],
            'trackingNumber' => htmlspecialchars((string)$cargoInfo['trackingNumber']),
            'numberOfPeople' => htmlspecialchars((string)($cargoInfo['numberOfPeople'] ?? '')),
            'username' => htmlspecialchars((string)($cargoInfo['username'] ?? '')),
            'userType' => htmlspecialchars((string)($cargoInfo['userType'] ?? '')),
            'entryTime' => htmlspecialchars((string)$cargoInfo['entryTime']),
            'netWeight' => htmlspecialchars((string)$cargoInfo['netWeight']),
            'scaleReceiptNumber' => htmlspecialchars((string)$cargoInfo['scaleReceiptNumber']),
            'shortageWeight' => htmlspecialchars((string)$cargoInfo['shortageWeight']),
            'excessWeight' => htmlspecialchars((string)$cargoInfo['excessWeight']),
            'exitTime' => htmlspecialchars((string)($cargoInfo['exitTime'] ?? '')),
            'exitDate' => htmlspecialchars((string)($cargoInfo['exitDate'] ?? '')),
            'status' => htmlspecialchars((string)$cargoInfo['status']),
            'shipName' => htmlspecialchars((string)$cargoInfo['shipName']),
            'loadingWarehouse' => htmlspecialchars((string)$cargoInfo['loadingWarehouse']),
            'cargoType' => htmlspecialchars((string)$cargoInfo['cargoType']),
            'shippingCompany' => htmlspecialchars((string)$cargoInfo['shippingCompany']),
            'loadingQuotaNumber' => htmlspecialchars((string)$cargoInfo['loadingQuotaNumber']),
            'confirm' => htmlspecialchars((string)($cargoInfo['confirm'] ?? '')),
            'confirmation' => htmlspecialchars((string)($cargoInfo['confirmation'] ?? 'no'))
        ];
        error_log("📤 Sending success response");
        sendJsonResponse(['cargoInfo' => $formattedCargoInfo]);
    } else {
        error_log("❌ No results found for receipt: $receiptNumber");
        sendJsonResponse(['error' => 'هیچ نتیجه‌ای یافت نشد.'], 404);
    }
} catch (InvalidArgumentException $e) {
    error_log("Validation Error: " . $e->getMessage());
    sendJsonResponse(['error' => $e->getMessage()], 400);
} catch (Exception $e) {
    error_log("API Error: " . $e->getMessage());
    error_log("Stack trace: " . $e->getTraceAsString());
    sendJsonResponse([
        'error' => 'خطای داخلی سرور رخ داده است.',
        'message' => $e->getMessage(),
        'file' => basename($e->getFile()),
        'line' => $e->getLine()
    ], 500);
} finally {
    if (isset($stmt)) {
        $stmt->close();
    }
    if (isset($conn)) {
        $conn->close();
    }
}
?>
