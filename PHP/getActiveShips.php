<?php
header('Content-Type: application/json; charset=UTF-8');
error_reporting(E_ALL);
ini_set('display_errors', 0);
require_once __DIR__ . '/config/config.php';

function send_json_response($data, $status_code = 200) {
    http_response_code($status_code);
    echo json_encode($data, JSON_UNESCAPED_UNICODE);
    exit();
}

try {
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
    if ($conn->connect_error) {
        throw new Exception("خطا در اتصال به پایگاه داده: " . $conn->connect_error);
    }
    $conn->set_charset("utf8mb4");

    $stmt = $conn->prepare("SELECT shipName, loadingWarehouse, cargoType, shippingCompany, loadingQuotaNumber FROM InitialInfo WHERE isActive = 1");
    if (!$stmt) {
        throw new Exception("خطا در آماده‌سازی دستور SQL: " . $conn->error);
    }

    if (!$stmt->execute()) {
        throw new Exception("خطا در اجرای دستور SQL: " . $stmt->error);
    }

    $result = $stmt->get_result();
    $activeShips = [];

    while ($row = $result->fetch_assoc()) {
        $activeShips[] = [
            'shipName' => $row['shipName'],
            'loadingWarehouse' => $row['loadingWarehouse'],
            'cargoType' => $row['cargoType'],
            'shippingCompany' => $row['shippingCompany'],
            'loadingQuotaNumber' => $row['loadingQuotaNumber']
        ];
    }

    send_json_response($activeShips);
} catch (Exception $e) {
    error_log("Error: " . $e->getMessage());
    send_json_response(["status" => "error", "message" => $e->getMessage()], 400);
} finally {
    if (isset($stmt)) $stmt->close();
    if (isset($conn) && $conn instanceof mysqli) $conn->close();
}
?>