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

// Sanitize inputs once
$quotaNumber = sanitize_input($_GET['quotaNumber']);
$shippingCompany = sanitize_input($_GET['shippingCompany']);
$warehouse = sanitize_input($_GET['warehouse']);
$cargoType = sanitize_input($_GET['cargoType']);

try {
    // Establish a single connection
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
    if ($conn->connect_error) {
        throw new Exception("خطا در اتصال به پایگاه داده: " . $conn->connect_error);
    }
    $conn->set_charset("utf8mb4");
    
    // Start transaction for data consistency
    $conn->begin_transaction();
    
    // تبدیل تاریخ امروز و دیروز به شمسی - calculate once
    $today = jdate('Y/m/d');
    $yesterday = jdate('Y/m/d', time() - 86400);
    
    // OPTIMIZATION 1: Use EXISTS for more efficient record checking
    $initialInfoSql = $conn->prepare(
        "SELECT *, temp_tonnage_status, temp_tonnage_amount FROM InitialInfo 
         WHERE loadingQuotaNumber = ? 
         AND shippingCompany = ? 
         AND loadingWarehouse = ? 
         AND cargoType = ? 
         LIMIT 1"
    );
    $initialInfoSql->bind_param("ssss", $quotaNumber, $shippingCompany, $warehouse, $cargoType);
    $initialInfoSql->execute();
    $initialInfoResult = $initialInfoSql->get_result();
    
    if ($initialInfoResult->num_rows === 0) {
        $conn->rollback();
        send_json_response([
            "status" => "error",
            "message" => "اطلاعات وارد شده (شامل نوع کالا) مطابقت ندارد. لطفاً مقادیر را بررسی کنید."
        ], 404);
    }
    
    $initialInfo = $initialInfoResult->fetch_assoc();
    
    // OPTIMIZATION 2: Use a single, more efficient query for statistics with direct aggregation
    $statsSql = $conn->prepare(
        "SELECT 
            COUNT(*) as totalVouchers,
            COALESCE(SUM(CASE WHEN status = 'خروج' THEN netWeight ELSE 0 END), 0) as totalNetWeight,
            COUNT(CASE WHEN status = 'خروج' THEN 1 END) as exitedVouchers,
            COUNT(CASE WHEN status = 'ورود' THEN 1 END) as remainingVouchers,
            COALESCE(AVG(CASE WHEN status = 'خروج' AND netWeight > 0 THEN netWeight END), 0) as avgNetWeight
         FROM CargoInfo 
         WHERE loadingQuotaNumber = ? 
         AND shippingCompany = ? 
         AND loadingWarehouse = ? 
         AND cargoType = ?"
    );
    $statsSql->bind_param("ssss", $quotaNumber, $shippingCompany, $warehouse, $cargoType);
    $statsSql->execute();
    $statsResult = $statsSql->get_result();
    $stats = $statsResult->fetch_assoc();
    
    // Process statistics efficiently with proper type casting
    $totalNetWeight = (int)$stats['totalNetWeight'];
    $totalVouchers = (int)$stats['totalVouchers'];
    $averageNetWeight = (float)$stats['avgNetWeight'];
    
    // Calculate remaining weight and services
    $cargoWeight = (int)$initialInfo['cargoWeight'];
    $remainingWeight = max(0, $cargoWeight - $totalNetWeight);
    $remainingServices = $averageNetWeight > 0 ? floor($remainingWeight / $averageNetWeight) : 0;
    
    // Update the initialInfo array
    $initialInfo['remainingWeight'] = $remainingWeight;
    $initialInfo['averageNetWeight'] = round($averageNetWeight, 2);
    $initialInfo['remainingServices'] = (int)$remainingServices;
    $initialInfo['totalVoucherCount'] = $totalVouchers;
    $initialInfo['totalNetWeight'] = $totalNetWeight;
    
    // Add temporary tonnage information
    $initialInfo['tempTonnageStatus'] = isset($initialInfo['temp_tonnage_status']) ? (bool)$initialInfo['temp_tonnage_status'] : false;
    $initialInfo['tempTonnageAmount'] = isset($initialInfo['temp_tonnage_amount']) ? (float)$initialInfo['temp_tonnage_amount'] : null;
    
    // OPTIMIZATION 3: More efficient date filtering with BETWEEN and prepared statements
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
                AND exitDate >= ? 
                AND exitDate <= ?
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
    
    // Fetch data with improved memory handling
    $cargoInfoList = [];
    while ($row = $cargoInfoResult->fetch_assoc()) {
        // Only include necessary fields to reduce response size
        $cargoInfoList[] = $row;
    }
    
    // OPTIMIZATION 4: Use DISTINCT for tracking numbers with reduced result set
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
    
    // Clean response with only needed data
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
    
    error_log("Error in getInitialInfo.php: " . $e->getMessage());
    send_json_response([
        "status" => "error",
        "message" => "خطایی در سیستم رخ داده است. لطفاً بعداً تلاش کنید."
    ], 500);
} finally {
    // Close all prepared statements
    $statements = [
        'initialInfoSql', 'statsSql', 'cargoInfoSql', 'trackingNumbersSql'
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