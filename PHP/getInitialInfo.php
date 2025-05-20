<?php
header('Content-Type: application/json; charset=UTF-8');
error_reporting(E_ALL);
ini_set('display_errors', 0);
require_once __DIR__ . '/config/config.php';
require_once __DIR__ . '/jdf.php';

function sanitize_input($input) {
    return htmlspecialchars(trim($input), ENT_QUOTES, 'UTF-8');
}

function send_json_response($data, $status_code = 200) {
    http_response_code($status_code);
    echo json_encode($data, JSON_UNESCAPED_UNICODE);
    exit();
}

// Validate required parameters
$required_params = ['quotaNumber', 'shippingCompany', 'warehouse', 'cargoType'];
$missing_params = array_filter($required_params, function($param) {
    return !isset($_GET[$param]) || trim($_GET[$param]) === '';
});

if (!empty($missing_params)) {
    send_json_response([
        "status" => "error",
        "message" => "پارامترهای زیر الزامی هستند: " . implode(', ', $missing_params)
    ], 400);
}

// Sanitize inputs
$quotaNumber = sanitize_input($_GET['quotaNumber']);
$shippingCompany = sanitize_input($_GET['shippingCompany']);
$warehouse = sanitize_input($_GET['warehouse']);
$cargoType = sanitize_input($_GET['cargoType']);

try {
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
    if ($conn->connect_error) {
        throw new Exception("خطا در اتصال به پایگاه داده: " . $conn->connect_error);
    }
    $conn->set_charset("utf8mb4");
    
    // Start transaction for data consistency
    $conn->begin_transaction();
    
    // تبدیل تاریخ امروز و دیروز به شمسی - calculate once
    $today = jdate('Y-m-d');
    $yesterday = jdate('Y-m-d', time() - 86400);
    
    // Combined query to verify and get initial info in one operation
    $initialInfoSql = $conn->prepare(
        "SELECT *, 
            (SELECT COUNT(*) FROM InitialInfo WHERE loadingQuotaNumber = ? 
             AND shippingCompany = ? AND loadingWarehouse = ? AND cargoType = ?) as record_exists 
         FROM InitialInfo 
         WHERE loadingQuotaNumber = ? 
         AND shippingCompany = ? 
         AND loadingWarehouse = ? 
         AND cargoType = ?"
    );
    $initialInfoSql->bind_param("ssssssss", 
        $quotaNumber, $shippingCompany, $warehouse, $cargoType,
        $quotaNumber, $shippingCompany, $warehouse, $cargoType
    );
    $initialInfoSql->execute();
    $initialInfoResult = $initialInfoSql->get_result();
    $initialInfo = $initialInfoResult->fetch_assoc();
    
    // Verify the record exists
    if (!$initialInfo || $initialInfo['record_exists'] == 0) {
        $conn->rollback();
        send_json_response([
            "status" => "error",
            "message" => "اطلاعات وارد شده (شامل نوع کالا) مطابقت ندارد. لطفاً مقادیر را بررسی کنید."
        ], 404);
    }
    
    // Remove the extra field we added
    unset($initialInfo['record_exists']);
    
    // Convert numeric values in InitialInfo - more efficient type casting
    $numericFields = ['loadingQuotaNumber', 'cargoWeight', 'remainingWeight', 
                     'totalNetWeight', 'remainingServices'];
    foreach ($numericFields as $field) {
        if (isset($initialInfo[$field])) {
            $initialInfo[$field] = (int)$initialInfo[$field];
        }
    }
    
    if (isset($initialInfo['averageNetWeight'])) {
        $initialInfo['averageNetWeight'] = (float)$initialInfo['averageNetWeight'];
    }
    
    // Combined query for both CargoInfo and statistics
    $combinedSql = $conn->prepare(
        "SELECT 
            (SELECT COUNT(*) FROM CargoInfo 
             WHERE loadingQuotaNumber = ? 
             AND shippingCompany = ? 
             AND loadingWarehouse = ? 
             AND cargoType = ?) as totalVouchers,
             
            (SELECT COALESCE(SUM(CASE WHEN status = 'خروج' THEN netWeight ELSE 0 END), 0) 
             FROM CargoInfo 
             WHERE loadingQuotaNumber = ? 
             AND shippingCompany = ? 
             AND loadingWarehouse = ? 
             AND cargoType = ?) as totalNetWeight,
             
            (SELECT COUNT(*) FROM CargoInfo 
             WHERE loadingQuotaNumber = ? 
             AND shippingCompany = ? 
             AND loadingWarehouse = ? 
             AND cargoType = ? 
             AND status = 'خروج') as exitedVouchers,
             
            (SELECT COUNT(*) FROM CargoInfo 
             WHERE loadingQuotaNumber = ? 
             AND shippingCompany = ? 
             AND loadingWarehouse = ? 
             AND cargoType = ? 
             AND status = 'ورود') as remainingVouchers,
             
            (SELECT COALESCE(AVG(CASE WHEN status = 'خروج' AND netWeight > 0 THEN netWeight END), 0) 
             FROM CargoInfo 
             WHERE loadingQuotaNumber = ? 
             AND shippingCompany = ? 
             AND loadingWarehouse = ? 
             AND cargoType = ?) as avgNetWeight"
    );
    
    $combinedSql->bind_param("ssssssssssssssssssss", 
        $quotaNumber, $shippingCompany, $warehouse, $cargoType,
        $quotaNumber, $shippingCompany, $warehouse, $cargoType,
        $quotaNumber, $shippingCompany, $warehouse, $cargoType,
        $quotaNumber, $shippingCompany, $warehouse, $cargoType,
        $quotaNumber, $shippingCompany, $warehouse, $cargoType
    );
    $combinedSql->execute();
    $statsResult = $combinedSql->get_result()->fetch_assoc();
    
    // Calculate statistics using query results
    $totalNetWeight = (int)$statsResult['totalNetWeight'];
    $exitedVouchers = (int)$statsResult['exitedVouchers'];
    $remainingVouchers = (int)$statsResult['remainingVouchers'];
    $totalVouchers = (int)$statsResult['totalVouchers'];
    $averageNetWeight = (float)$statsResult['avgNetWeight'];
    
    // Update values in initialInfo
    $remainingWeight = $initialInfo['cargoWeight'] - $totalNetWeight;
    $remainingServices = $averageNetWeight > 0 ? floor($remainingWeight / $averageNetWeight) : 0;
    
    $initialInfo['remainingWeight'] = max(0, $remainingWeight);
    $initialInfo['averageNetWeight'] = round($averageNetWeight, 2);
    $initialInfo['remainingServices'] = max(0, (int)$remainingServices);
    $initialInfo['totalVoucherCount'] = $totalVouchers;
    $initialInfo['totalNetWeight'] = $totalNetWeight;
    
    // Get CargoInfo with date filter - filtered by recent entries only
    $cargoInfoSql = $conn->prepare(
        "SELECT * 
        FROM CargoInfo 
        WHERE loadingQuotaNumber = ? 
        AND shippingCompany = ? 
        AND loadingWarehouse = ? 
        AND cargoType = ? 
        AND (
            status = 'ورود' 
            OR (
                status = 'خروج' 
                AND DATE(exitDate) >= DATE(?) 
                AND DATE(exitDate) <= DATE(?)
            )
        )
        ORDER BY 
            CASE WHEN status = 'ورود' THEN 1 ELSE 2 END,
            entryTime DESC"
    );
    $cargoInfoSql->bind_param("ssssss", 
        $quotaNumber, $shippingCompany, $warehouse, $cargoType, 
        $yesterday, $today
    );
    $cargoInfoSql->execute();
    $cargoInfoResult = $cargoInfoSql->get_result();
    
    $cargoInfoList = [];
    while ($row = $cargoInfoResult->fetch_assoc()) {
        $cargoInfoList[] = $row;
    }
    
    // Get only tracking numbers instead of full data
    $trackingNumbersSql = $conn->prepare(
        "SELECT DISTINCT trackingNumber 
        FROM CargoInfo 
        WHERE shippingCompany = ? 
        AND loadingWarehouse = ? 
        AND cargoType = ?"
    );
    $trackingNumbersSql->bind_param("sss", $shippingCompany, $warehouse, $cargoType);
    $trackingNumbersSql->execute();
    $allTrackingNumbersResult = $trackingNumbersSql->get_result();
    
    $allTrackingNumbers = [];
    while ($row = $allTrackingNumbersResult->fetch_assoc()) {
        $allTrackingNumbers[] = $row['trackingNumber'];
    }
    
    // Commit the transaction
    $conn->commit();
    
    send_json_response([
        "status" => "success",
        "initialInfo" => $initialInfo,
        "cargoInfoList" => $cargoInfoList,
        "allTrackingNumbers" => $allTrackingNumbers
    ]);

} catch (Exception $e) {
    // Rollback transaction on error
    if (isset($conn) && $conn->ping()) {
        $conn->rollback();
    }
    
    error_log("Error: " . $e->getMessage());
    send_json_response([
        "status" => "error",
        "message" => "خطایی در سیستم رخ داده است. لطفاً بعداً تلاش کنید."
    ], 500);
} finally {
    // Close all prepared statements
    $statements = [
        'initialInfoSql', 'combinedSql', 'cargoInfoSql', 'trackingNumbersSql'
    ];
    
    foreach ($statements as $stmt) {
        if (isset($$stmt)) {
            $$stmt->close();
        }
    }
    
    // Close database connection
    if (isset($conn)) {
        $conn->close();
    }
}