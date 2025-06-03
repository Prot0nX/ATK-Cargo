<?php
require_once __DIR__ . '/config/config.php';

function sendResponse($status, $message) {
    echo json_encode(array("status" => $status, "message" => $message));
    exit;
}

try {
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
    
    if ($conn->connect_error) {
        throw new Exception("Connection failed: " . $conn->connect_error);
    }
    
    $conn->set_charset("utf8mb4");
    
    $input = file_get_contents('php://input');
    $data = json_decode($input, true);
    
    if (!$data || !isset($data['loadingQuotaNumber']) || !is_int($data['loadingQuotaNumber']) ||
        !isset($data['shipName']) || !isset($data['loadingWarehouse']) || 
        !isset($data['cargoType']) || !isset($data['shippingCompany'])) {
        sendResponse("error", "داده‌های ورودی نامعتبر است");
    }
    
    $loadingQuotaNumber = $data['loadingQuotaNumber'];
    $shipName = $data['shipName'];
    $loadingWarehouse = $data['loadingWarehouse'];
    $cargoType = $data['cargoType'];
    $shippingCompany = $data['shippingCompany'];
    
    // بررسی وجود دقیق رکورد
    $stmt = $conn->prepare("SELECT id FROM InitialInfo WHERE 
                            loadingQuotaNumber = ? AND 
                            shipName = ? AND 
                            loadingWarehouse = ? AND 
                            cargoType = ? AND 
                            shippingCompany = ?");
    
    if (!$stmt) {
        throw new Exception("Prepare failed: " . $conn->error);
    }
    
    $stmt->bind_param("issss", $loadingQuotaNumber, $shipName, $loadingWarehouse, $cargoType, $shippingCompany);
    
    if (!$stmt->execute()) {
        throw new Exception("Execute failed: " . $stmt->error);
    }
    
    $result = $stmt->get_result();
    
    if ($result->num_rows > 0) {
        sendResponse("exists", "اطلاعات وارد شده قبلاً ثبت شده است.");
    } else {
        // بررسی وجود کوتاژ با شرایط متفاوت
        $stmtPartial = $conn->prepare("SELECT id FROM InitialInfo WHERE loadingQuotaNumber = ?");
        
        if (!$stmtPartial) {
            throw new Exception("Prepare failed: " . $conn->error);
        }
        
        $stmtPartial->bind_param("i", $loadingQuotaNumber);
        
        if (!$stmtPartial->execute()) {
            throw new Exception("Execute failed: " . $stmtPartial->error);
        }
        
        $resultPartial = $stmtPartial->get_result();
        
        if ($resultPartial->num_rows > 0) {
            sendResponse("partial_match", "شماره کوتاژ قبلاً ثبت شده، اما با مشخصات متفاوت. ثبت اطلاعات جدید مجاز است.");
        } else {
            sendResponse("not_exists", "اطلاعات وارد شده قابل ثبت است.");
        }
        
        $stmtPartial->close();
    }
    
    $stmt->close();
} catch (Exception $e) {
    error_log($e->getMessage());
    sendResponse("error", $e->getMessage());
} finally {
    if (isset($conn) && $conn instanceof mysqli) {
        $conn->close();
    }
}
?>