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
        throw new InvalidArgumentException('لطفاً شماره حواله را وارد کنید.');
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
    $trackingNumber = validateAndSanitizeInput($_GET['tracking'] ?? null);

    // ساخت پرس و جو برای جستجوی دقیق شماره حواله
    $query = "
        SELECT 
            c.trackingNumber, c.numberOfPeople, c.username, c.userType, c.entryTime, 
            c.netWeight, c.scaleReceiptNumber, c.shortageWeight, c.excessWeight, 
            c.exitTime, c.exitDate, c.status, c.confirm, c.confirmation,
            i.shipName, i.loadingWarehouse, i.cargoType, i.shippingCompany,
            i.loadingQuotaNumber
        FROM 
            CargoInfo c
        JOIN 
            InitialInfo i ON c.loadingQuotaNumber = i.loadingQuotaNumber
        WHERE 
            c.trackingNumber = ?
        ORDER BY c.entryTime DESC
    ";

    // آماده‌سازی و اجرای پرس و جو
    $stmt = $conn->prepare($query);
    if (!$stmt) {
        throw new Exception("آماده‌سازی پرس و جو ناموفق بود: " . $conn->error);
    }

    $stmt->bind_param("s", $trackingNumber);
    if (!$stmt->execute()) {
        throw new Exception("اجرای پرس و جو ناموفق بود: " . $stmt->error);
    }

    $result = $stmt->get_result();
    $cargoInfoList = [];

    while ($row = $result->fetch_assoc()) {
        // فرمت‌بندی داده‌ها برای خروجی با ساختار مورد انتظار Android
        $cargoInfo = array_map('htmlspecialchars', [
            'trackingNumber' => $row['trackingNumber'],
            'numberOfPeople' => $row['numberOfPeople'] ?? '',
            'username' => $row['username'] ?? '',
            'userType' => $row['userType'] ?? '',
            'entryTime' => $row['entryTime'],
            'netWeight' => $row['netWeight'],
            'scaleReceiptNumber' => $row['scaleReceiptNumber'],
            'shortageWeight' => $row['shortageWeight'],
            'excessWeight' => $row['excessWeight'],
            'exitTime' => $row['exitTime'],
            'exitDate' => $row['exitDate'],
            'status' => $row['status'],
            'shipName' => $row['shipName'],
            'loadingWarehouse' => $row['loadingWarehouse'],
            'cargoType' => $row['cargoType'],
            'shippingCompany' => $row['shippingCompany'],
            'loadingQuotaNumber' => $row['loadingQuotaNumber'],
            'confirm' => $row['confirm'] ?? '',
            'confirmation' => $row['confirmation'] ?? 'no'
        ]);
        
        // ایجاد ساختار مطابق با مدل Android CargoInfoSearch
        $cargoInfoList[] = [
            'cargoInfo' => $cargoInfo
        ];
    }

    if (!empty($cargoInfoList)) {
        sendJsonResponse([
            'cargoInfoList' => $cargoInfoList,
            'totalCount' => count($cargoInfoList)
        ]);
    } else {
        sendJsonResponse(['error' => 'هیچ نتیجه‌ای برای این شماره حواله یافت نشد.'], 404);
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