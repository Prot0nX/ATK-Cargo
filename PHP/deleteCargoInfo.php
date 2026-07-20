<?php
header('Content-Type: application/json; charset=UTF-8');
error_reporting(E_ALL);
ini_set('display_errors', 0);
require_once __DIR__ . '/config/config.php';

function log_debug($message) {
    error_log(date('[Y-m-d H:i:s] ') . $message . "\n", 3, "delete_cargo_debug.log");
}

function send_json_response($status, $message, $http_code = 200) {
    http_response_code($http_code);
    echo json_encode(["status" => $status, "message" => $message], JSON_UNESCAPED_UNICODE);
    exit();
}

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    send_json_response("error", "روش درخواست مجاز نیست. لطفاً از روش POST استفاده کنید.", 405);
}

log_debug("Received POST request");

$data = json_decode(file_get_contents("php://input"), true);
if (json_last_error() !== JSON_ERROR_NONE) {
    log_debug("JSON Error: " . json_last_error_msg());
    send_json_response("error", "فرمت JSON نامعتبر است", 400);
}

log_debug("Received data: " . json_encode($data, JSON_UNESCAPED_UNICODE));

// بررسی فیلد id
if (!isset($data['id']) || empty($data['id'])) {
    log_debug("Missing required field: id");
    send_json_response("error", "فیلد ضروری وجود ندارد: id", 400);
}

$cargoId = intval($data['id']);

if ($cargoId <= 0) {
    log_debug("Invalid cargo id: " . $data['id']);
    send_json_response("error", "شناسه حواله نامعتبر است", 400);
}

log_debug("Cargo ID: " . $cargoId);

try {
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
    if ($conn->connect_error) {
        throw new Exception("خطا در اتصال به پایگاه داده: " . $conn->connect_error);
    }
    $conn->set_charset("utf8mb4");
    log_debug("Database connection successful");

    // ابتدا اطلاعات حواله را برای بازگردانی تناژ موقت دریافت می‌کنیم
    $selectStmt = $conn->prepare("SELECT netWeight, status, shipName, loadingWarehouse, cargoType, shippingCompany, loadingQuotaNumber FROM CargoInfo WHERE id = ?");
    if (!$selectStmt) {
        throw new Exception("خطا در آماده‌سازی دستور انتخاب: " . $conn->error);
    }
    
    $selectStmt->bind_param("i", $cargoId);
    $selectStmt->execute();
    $selectResult = $selectStmt->get_result();
    $cargoData = $selectResult->fetch_assoc();
    $selectStmt->close();
    
    if (!$cargoData) {
        log_debug("Cargo not found for deletion");
        send_json_response("error", "حواله یافت نشد یا قبلاً حذف شده است", 404);
    }

    $stmt = $conn->prepare("DELETE FROM CargoInfo WHERE id = ?");
    if (!$stmt) {
        throw new Exception("خطا در آماده‌سازی دستور SQL: " . $conn->error);
    }
    log_debug("SQL statement prepared successfully");

    $stmt->bind_param("i", $cargoId);
    
    if (!$stmt->execute()) {
        throw new Exception("خطا در اجرای دستور SQL: " . $stmt->error);
    }
    
    $affectedRows = $stmt->affected_rows;
    log_debug("SQL statement executed successfully. Affected rows: $affectedRows");
    
    // اگر حواله خروج شده بود، تناژ آن را به تناژ موقت اضافه می‌کنیم
    if ($affectedRows > 0 && $cargoData['status'] === 'خروج' && !empty($cargoData['netWeight'])) {
        $netWeightValue = floatval($cargoData['netWeight']);
        $tempTonnageQuery = "SELECT temp_tonnage_status, temp_tonnage_amount FROM InitialInfo 
                           WHERE shipName = ? AND loadingWarehouse = ? AND cargoType = ? AND 
                                 shippingCompany = ? AND loadingQuotaNumber = ? LIMIT 1";
        $tempTonnageStmt = $conn->prepare($tempTonnageQuery);
        $tempTonnageStmt->bind_param("sssss", 
            $cargoData['shipName'], 
            $cargoData['loadingWarehouse'], 
            $cargoData['cargoType'], 
            $cargoData['shippingCompany'], 
            $cargoData['loadingQuotaNumber']
        );
        $tempTonnageStmt->execute();
        $tempTonnageResult = $tempTonnageStmt->get_result();
        $tempTonnageData = $tempTonnageResult->fetch_assoc();
        $tempTonnageStmt->close();
        
        if ($tempTonnageData && $tempTonnageData['temp_tonnage_status'] == 1) {
            $currentTempTonnage = floatval($tempTonnageData['temp_tonnage_amount']);
            $newTempTonnage = $currentTempTonnage + $netWeightValue;
            $updateTempTonnageQuery = "UPDATE InitialInfo SET temp_tonnage_amount = ? 
                                     WHERE shipName = ? AND loadingWarehouse = ? AND cargoType = ? AND 
                                           shippingCompany = ? AND loadingQuotaNumber = ?";
            $updateTempTonnageStmt = $conn->prepare($updateTempTonnageQuery);
            $updateTempTonnageStmt->bind_param("dsssss", 
                $newTempTonnage, 
                $cargoData['shipName'], 
                $cargoData['loadingWarehouse'], 
                $cargoData['cargoType'], 
                $cargoData['shippingCompany'], 
                $cargoData['loadingQuotaNumber']
            );
            $updateTempTonnageStmt->execute();
            $updateTempTonnageStmt->close();
            log_debug("Temporary tonnage restored: $newTempTonnage");
        }
    }

    if ($affectedRows === 0) {
        log_debug("No rows affected. Cargo not found.");
        send_json_response("error", "حواله یافت نشد یا قبلاً حذف شده است", 404);
    } else {
        log_debug("Cargo deleted successfully");
        send_json_response("success", "حواله با موفقیت حذف شد");
    }
} catch (Exception $e) {
    log_debug("Error: " . $e->getMessage());
    send_json_response("error", "خطا در حذف حواله: " . $e->getMessage(), 500);
} finally {
    if (isset($stmt)) $stmt->close();
    if (isset($conn)) $conn->close();
}
?>