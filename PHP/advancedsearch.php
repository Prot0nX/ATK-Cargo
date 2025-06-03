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
    echo json_encode($data, JSON_THROW_ON_ERROR | JSON_UNESCAPED_UNICODE);
    exit;
}

function validateAndSanitizeInput(?string $input): string
{
    $sanitized = filter_var(trim($input ?? ''), FILTER_SANITIZE_STRING);
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

    // برقراری اتصال به پایگاه داده
    $conn = getDbConnection();

    // بررسی و پاکسازی پارامتر جستجو
    $receiptNumber = validateAndSanitizeInput($_GET['receipt'] ?? null);

    // ساخت پرس و جو
    $query = "
        SELECT 
            c.trackingNumber, c.entryTime, c.netWeight, c.scaleReceiptNumber,
            c.shortageWeight, c.excessWeight, c.exitTime, c.exitDate, c.status,
            i.shipName, i.loadingWarehouse, i.cargoType, i.shippingCompany,
            i.loadingQuotaNumber
        FROM 
            CargoInfo c
        JOIN 
            InitialInfo i ON c.loadingQuotaNumber = i.loadingQuotaNumber
        WHERE 
            c.scaleReceiptNumber = ?
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

    if ($cargoInfo) {
        // فرمت‌بندی داده‌ها برای خروجی
        $formattedCargoInfo = array_map('htmlspecialchars', [
            'trackingNumber' => $cargoInfo['trackingNumber'],
            'entryTime' => $cargoInfo['entryTime'],
            'netWeight' => $cargoInfo['netWeight'],
            'scaleReceiptNumber' => $cargoInfo['scaleReceiptNumber'],
            'shortageWeight' => $cargoInfo['shortageWeight'],
            'excessWeight' => $cargoInfo['excessWeight'],
            'exitTime' => $cargoInfo['exitTime'],
            'exitDate' => $cargoInfo['exitDate'],
            'status' => $cargoInfo['status'],
            'shipName' => $cargoInfo['shipName'],
            'loadingWarehouse' => $cargoInfo['loadingWarehouse'],
            'cargoType' => $cargoInfo['cargoType'],
            'shippingCompany' => $cargoInfo['shippingCompany'],
            'loadingQuotaNumber' => $cargoInfo['loadingQuotaNumber']
        ]);
        sendJsonResponse(['cargoInfo' => $formattedCargoInfo]);
    } else {
        sendJsonResponse(['error' => 'هیچ نتیجه‌ای یافت نشد.'], 404);
    }
} catch (InvalidArgumentException $e) {
    error_log("Validation Error: " . $e->getMessage());
    sendJsonResponse(['error' => $e->getMessage()], 400);
} catch (Exception $e) {
    error_log("API Error: " . $e->getMessage());
    sendJsonResponse(['error' => 'خطای داخلی سرور رخ داده است.'], 500);
} finally {
    if (isset($stmt)) {
        $stmt->close();
    }
    if (isset($conn)) {
        $conn->close();
    }
}
?>