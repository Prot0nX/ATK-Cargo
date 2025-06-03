<?php
header('Content-Type: application/json; charset=UTF-8');
error_reporting(E_ALL);
ini_set('display_errors', 0);
require_once __DIR__ . '/config/config.php';

function sanitize_input($input) {
    return htmlspecialchars(trim($input), ENT_QUOTES, 'UTF-8');
}

function send_json_response($data, $status_code = 200) {
    http_response_code($status_code);
    echo json_encode($data, JSON_UNESCAPED_UNICODE);
    exit();
}

$required_fields = ['shipName', 'loadingWarehouse', 'cargoType', 'shippingCompany', 'cargoWeight', 'loadingQuotaNumber', 'remainingWeight', 'totalNetWeight', 'averageNetWeight', 'remainingServices', 'cargoOwner'];

try {
    $data = json_decode(file_get_contents('php://input'), true);
    if (!$data) {
        throw new Exception("داده‌های ورودی نامعتبر هستند.");
    }

    foreach ($required_fields as $field) {
        if (!isset($data[$field]) || trim($data[$field]) === '') {
            throw new Exception("فیلد $field الزامی است.");
        }
    }

    $conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
    if ($conn->connect_error) {
        throw new Exception("خطا در اتصال به پایگاه داده: " . $conn->connect_error);
    }
    $conn->set_charset("utf8mb4");

    $stmt = $conn->prepare("INSERT INTO InitialInfo (shipName, loadingWarehouse, cargoType, shippingCompany, cargoWeight, loadingQuotaNumber, remainingWeight, totalNetWeight, averageNetWeight, remainingServices, cargoOwner, isActive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)");
    if (!$stmt) {
        throw new Exception("خطا در آماده‌سازی دستور SQL: " . $conn->error);
    }

    $stmt->bind_param("ssssdddddds", 
        $data['shipName'],
        $data['loadingWarehouse'],
        $data['cargoType'],
        $data['shippingCompany'],
        $data['cargoWeight'],
        $data['loadingQuotaNumber'],
        $data['remainingWeight'],
        $data['totalNetWeight'],
        $data['averageNetWeight'],
        $data['remainingServices'],
        $data['cargoOwner']
    );

    if (!$stmt->execute()) {
        throw new Exception("خطا در اجرای دستور SQL: " . $stmt->error);
    }

    send_json_response(["status" => "success", "message" => "اطلاعات با موفقیت ثبت شد."]);
} catch (Exception $e) {
    error_log("Error: " . $e->getMessage());
    send_json_response(["status" => "error", "message" => $e->getMessage()], 400);
} finally {
    if (isset($stmt)) $stmt->close();
    if (isset($conn) && $conn instanceof mysqli) $conn->close();
}
?>