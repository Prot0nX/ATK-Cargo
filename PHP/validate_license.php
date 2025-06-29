<?php
ini_set('display_errors', 0);
date_default_timezone_set('Asia/Tehran');

error_reporting(0);

header('Content-Type: application/json');
header('X-Content-Type-Options: nosniff');
header('X-Frame-Options: DENY');
header('Content-Security-Policy: default-src \'self\'');

require_once __DIR__ . '/config/config.php';

$rawData = file_get_contents('php://input');
error_log("Received request data: " . $rawData);

if (!$rawData) {
    echo json_encode([
        'success' => false,
        'message' => 'داده‌های ورودی نامعتبر است'
    ]);
    exit;
}

$data = json_decode($rawData, true);
error_log("Decoded JSON data: " . print_r($data, true));

if (json_last_error() !== JSON_ERROR_NONE) {
    error_log("JSON decode error: " . json_last_error_msg());
    echo json_encode([
        'success' => false,
        'message' => 'فرمت داده‌های ورودی نامعتبر است'
    ]);
    exit;
}

$licenseKey = trim($data['licenseKey'] ?? '');
$updateLastCheck = $data['update_last_check'] ?? true;

error_log("Processing license key: " . $licenseKey . ", update_last_check: " . ($updateLastCheck ? "true" : "false"));

if (empty($licenseKey) || strlen($licenseKey) !== 32) {
    echo json_encode([
        'success' => false,
        'message' => 'کلید لایسنس نامعتبر است'
    ]);
    exit;
}

try {
    $conn = getDbConnection();
    $stmt = $conn->prepare("SELECT * FROM licenses WHERE license_key = ?");
    $stmt->bind_param("s", $licenseKey);
    
    if (!$stmt->execute()) {
        throw new Exception("خطا در اجرای کوئری");
    }
    
    $result = $stmt->get_result();
    
    if ($result->num_rows > 0) {
        $license = $result->fetch_assoc();
        
        if ($updateLastCheck) {
            error_log("Updating last_check for license: " . $licenseKey);
            $updateStmt = $conn->prepare("UPDATE licenses SET last_check = CURRENT_TIMESTAMP WHERE license_key = ?");
            $updateStmt->bind_param("s", $licenseKey);
            $success = $updateStmt->execute();
            error_log("Update result: " . ($success ? "success" : "failed"));
            $updateStmt->close();
        }

        if ($license['is_active']) {
            $response = [
                'success' => true,
                'message' => 'لایسنس معتبر است',
                'license' => [
                    'licenseKey' => htmlspecialchars($license['license_key']),
                    'companyName' => htmlspecialchars($license['company_name']),
                    'activationDate' => $license['activation_date'],
                    'isActive' => (bool)$license['is_active'],
                    'lastCheck' => $updateLastCheck ? date('Y-m-d H:i:s') : $license['last_check']
                ]
            ];
            error_log("Sending response: " . json_encode($response));
            echo json_encode($response);
        } else {
            echo json_encode([
                'success' => false,
                'message' => 'لایسنس غیرفعال شده است',
                'license' => [
                    'licenseKey' => htmlspecialchars($license['license_key']),
                    'lastCheck' => $updateLastCheck ? date('Y-m-d H:i:s') : $license['last_check']
                ]
            ]);
        }
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'لایسنس نامعتبر است'
        ]);
    }
} catch (Exception $e) {
    error_log("خطا در اعتبارسنجی لایسنس: " . $e->getMessage());
    echo json_encode([
        'success' => false,
        'message' => 'خطای سیستمی رخ داده است'
    ]);
}

$stmt->close();
$conn->close(); 