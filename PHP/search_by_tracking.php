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
        throw new InvalidArgumentException('لطفاً شماره حواله را وارد کنید.');
    }
    return $sanitized;
}

try {
    // بررسی روش درخواست
    if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
        throw new Exception('روش درخواست نامعتبر است');
    }

    // لاگ درخواست
    error_log("🔍 Search Tracking Request - Tracking: " . ($_GET['tracking'] ?? 'NULL'));

    // برقراری اتصال به پایگاه داده
    $conn = getDbConnection();
    error_log("✅ Database connection established");

    // بررسی و پاکسازی پارامتر جستجو
    $trackingNumber = validateAndSanitizeInput($_GET['tracking'] ?? null);
    error_log("📝 Sanitized tracking number: $trackingNumber");

    // ساخت پرس و جو برای جستجوی دقیق شماره حواله
    $query = "
        SELECT 
            id, trackingNumber, numberOfPeople, username, userType, entryTime, 
            netWeight, scaleReceiptNumber, shortageWeight, excessWeight, 
            exitTime, exitDate, status, confirm, confirmation,
            shipName, loadingWarehouse, cargoType, shippingCompany,
            loadingQuotaNumber
        FROM 
            CargoInfo
        WHERE 
            trackingNumber = ?
        ORDER BY entryTime DESC
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
    
    error_log("📊 Query executed, fetching results...");

    while ($row = $result->fetch_assoc()) {
        error_log("✅ Found cargo info with ID: " . $row['id']);
        // فرمت‌بندی داده‌ها برای خروجی با ساختار مورد انتظار Android
        $cargoInfo = [
            'id' => (int)$row['id'],
            'trackingNumber' => htmlspecialchars((string)$row['trackingNumber']),
            'numberOfPeople' => htmlspecialchars((string)($row['numberOfPeople'] ?? '')),
            'username' => htmlspecialchars((string)($row['username'] ?? '')),
            'userType' => htmlspecialchars((string)($row['userType'] ?? '')),
            'entryTime' => htmlspecialchars((string)$row['entryTime']),
            'netWeight' => htmlspecialchars((string)$row['netWeight']),
            'scaleReceiptNumber' => htmlspecialchars((string)$row['scaleReceiptNumber']),
            'shortageWeight' => htmlspecialchars((string)$row['shortageWeight']),
            'excessWeight' => htmlspecialchars((string)$row['excessWeight']),
            'exitTime' => htmlspecialchars((string)($row['exitTime'] ?? '')),
            'exitDate' => htmlspecialchars((string)($row['exitDate'] ?? '')),
            'status' => htmlspecialchars((string)$row['status']),
            'shipName' => htmlspecialchars((string)$row['shipName']),
            'loadingWarehouse' => htmlspecialchars((string)$row['loadingWarehouse']),
            'cargoType' => htmlspecialchars((string)$row['cargoType']),
            'shippingCompany' => htmlspecialchars((string)$row['shippingCompany']),
            'loadingQuotaNumber' => htmlspecialchars((string)$row['loadingQuotaNumber']),
            'confirm' => htmlspecialchars((string)($row['confirm'] ?? '')),
            'confirmation' => htmlspecialchars((string)($row['confirmation'] ?? 'no'))
        ];
        
        // ایجاد ساختار مطابق با مدل Android CargoInfoSearch
        $cargoInfoList[] = [
            'cargoInfo' => $cargoInfo
        ];
    }

    error_log("📋 Total results found: " . count($cargoInfoList));
    
    if (!empty($cargoInfoList)) {
        error_log("📤 Sending success response with " . count($cargoInfoList) . " items");
        sendJsonResponse([
            'cargoInfoList' => $cargoInfoList,
            'totalCount' => count($cargoInfoList)
        ]);
    } else {
        error_log("❌ No results found for tracking: $trackingNumber");
        sendJsonResponse(['error' => 'هیچ نتیجه‌ای برای این شماره حواله یافت نشد.'], 404);
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
