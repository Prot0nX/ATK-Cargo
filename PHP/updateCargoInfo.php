<?php
declare(strict_types=1);

// تنظیم نوع محتوا به JSON و تنظیمات امنیتی
header('Content-Type: application/json; charset=UTF-8');
header('X-Content-Type-Options: nosniff');
header('X-Frame-Options: DENY');
header('X-XSS-Protection: 1; mode=block');
error_reporting(E_ALL);
ini_set('display_errors', '0');

// وارد کردن تنظیمات پایگاه داده و مدیریت session
require_once __DIR__ . '/config/config.php';
require_once __DIR__ . '/SessionManager.php';

function sendJsonResponse($data, int $statusCode = 200)
{
    http_response_code($statusCode);
    $json = json_encode($data, JSON_UNESCAPED_UNICODE);
    if ($json === false) {
        error_log("JSON encode error: " . json_last_error_msg());
        http_response_code(500);
        echo json_encode(['error' => 'JSON encoding failed']);
        exit;
    }
    echo $json;
    exit;
}

function validateNumericField($value, string $fieldName): string
{
    if ($value === null || $value === '') {
        return '0';
    }
    
    // Convert to string if not already
    $strValue = (string)$value;
    $sanitized = filter_var(trim($strValue), FILTER_SANITIZE_NUMBER_FLOAT, FILTER_FLAG_ALLOW_FRACTION);
    
    if (!is_numeric($sanitized)) {
        throw new InvalidArgumentException("فیلد {$fieldName} باید عددی باشد.");
    }
    
    return $sanitized;
}

function validateStringField($value, string $fieldName, bool $required = true): string
{
    // Convert to string if not already
    $strValue = ($value === null) ? '' : (string)$value;
    $sanitized = htmlspecialchars(trim($strValue), ENT_QUOTES, 'UTF-8');
    
    if ($required && empty($sanitized)) {
        throw new InvalidArgumentException("فیلد {$fieldName} الزامی است.");
    }
    
    return $sanitized;
}

try {
    error_log("🔄 UPDATE REQUEST START");
    error_log("📥 REQUEST_METHOD: " . $_SERVER['REQUEST_METHOD']);
    
    // بررسی روش درخواست
    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
        throw new Exception('روش درخواست نامعتبر است. فقط POST مجاز است.');
    }

    // دریافت داده‌های JSON از body
    $jsonInput = file_get_contents('php://input');
    error_log("📦 Raw Input: " . substr($jsonInput, 0, 200));
    
    $data = json_decode($jsonInput, true);
    error_log("📋 Decoded Data: " . print_r($data, true));

    if (json_last_error() !== JSON_ERROR_NONE) {
        throw new Exception('فرمت JSON نامعتبر است.');
    }

    // بررسی session کاربر
    // Note: Session check temporarily disabled for API calls
    // TODO: Implement proper API authentication
    // $sessionManager = SessionManager::getInstance();
    // if (!$sessionManager->isLoggedIn()) {
    //     error_log("❌ Session check failed - not logged in");
    //     sendJsonResponse([
    //         'error' => true,
    //         'message' => 'برای انجام این عملیات باید وارد سیستم شوید.'
    //     ], 401);
    // }
    error_log("⚠️ Session check bypassed for API call");

    // اعتبارسنجی و پاکسازی داده‌های ورودی
    $id = filter_var($data['id'] ?? null, FILTER_VALIDATE_INT);
    error_log("🔑 ID: " . ($id ?: 'NULL'));
    
    if (!$id || $id <= 0) {
        error_log("❌ Invalid ID: $id");
        throw new InvalidArgumentException('شناسه رکورد نامعتبر است.');
    }

    $trackingNumber = validateStringField($data['trackingNumber'] ?? null, 'شماره حواله');
    $numberOfPeople = validateStringField($data['numberOfPeople'] ?? null, 'تعداد افراد', false);
    $username = validateStringField($data['username'] ?? null, 'نام کاربری', false);
    $userType = validateStringField($data['userType'] ?? null, 'نوع کاربر', false);
    $entryTime = validateStringField($data['entryTime'] ?? null, 'زمان ورود');
    $netWeight = validateNumericField($data['netWeight'] ?? null, 'وزن خالص');
    $scaleReceiptNumber = validateStringField($data['scaleReceiptNumber'] ?? null, 'شماره قبض باسکول');
    $shortageWeight = validateNumericField($data['shortageWeight'] ?? null, 'وزن کسری');
    $excessWeight = validateNumericField($data['excessWeight'] ?? null, 'وزن اضافی');
    $exitTime = validateStringField($data['exitTime'] ?? null, 'زمان خروج', false);
    $exitDate = validateStringField($data['exitDate'] ?? null, 'تاریخ خروج', false);
    $status = validateStringField($data['status'] ?? null, 'وضعیت');
    $confirm = validateStringField($data['confirm'] ?? null, 'تایید', false);

    // برقراری اتصال به پایگاه داده
    $conn = getDbConnection();

    // بررسی وجود رکورد با id مشخص شده
    $checkQuery = "SELECT id FROM CargoInfo WHERE id = ? LIMIT 1";
    $checkStmt = $conn->prepare($checkQuery);
    if (!$checkStmt) {
        throw new Exception("خطا در آماده‌سازی پرس و جوی بررسی: " . $conn->error);
    }

    $checkStmt->bind_param("i", $id);
    $checkStmt->execute();
    $checkResult = $checkStmt->get_result();

    if ($checkResult->num_rows === 0) {
        $checkStmt->close();
        sendJsonResponse([
            'error' => true,
            'message' => 'رکوردی با این شناسه یافت نشد.'
        ], 404);
    }
    $checkStmt->close();

    // بررسی تکراری نبودن قبض باسکول (برای رکوردهای دیگر)
    $duplicateQuery = "SELECT id FROM CargoInfo WHERE scaleReceiptNumber = ? AND id != ? LIMIT 1";
    $duplicateStmt = $conn->prepare($duplicateQuery);
    if (!$duplicateStmt) {
        throw new Exception("خطا در آماده‌سازی پرس و جوی تکراری: " . $conn->error);
    }

    $duplicateStmt->bind_param("si", $scaleReceiptNumber, $id);
    $duplicateStmt->execute();
    $duplicateResult = $duplicateStmt->get_result();

    if ($duplicateResult->num_rows > 0) {
        $duplicateStmt->close();
        sendJsonResponse([
            'error' => true,
            'message' => 'شماره قبض باسکول تکراری است.'
        ], 400);
    }
    $duplicateStmt->close();

    // بروزرسانی رکورد
    $updateQuery = "
        UPDATE CargoInfo 
        SET 
            trackingNumber = ?,
            numberOfPeople = ?,
            username = ?,
            userType = ?,
            entryTime = ?,
            netWeight = ?,
            scaleReceiptNumber = ?,
            shortageWeight = ?,
            excessWeight = ?,
            exitTime = ?,
            exitDate = ?,
            status = ?,
            confirm = ?
        WHERE id = ?
    ";

    $updateStmt = $conn->prepare($updateQuery);
    if (!$updateStmt) {
        throw new Exception("خطا در آماده‌سازی پرس و جوی بروزرسانی: " . $conn->error);
    }

    $updateStmt->bind_param(
        "sssssssssssssi",
        $trackingNumber,
        $numberOfPeople,
        $username,
        $userType,
        $entryTime,
        $netWeight,
        $scaleReceiptNumber,
        $shortageWeight,
        $excessWeight,
        $exitTime,
        $exitDate,
        $status,
        $confirm,
        $id
    );

    if ($updateStmt->execute()) {
        $affectedRows = $updateStmt->affected_rows;
        error_log("📊 Affected rows: $affectedRows");
        
        if ($affectedRows > 0) {
            error_log("✅ Update successful");
            $updateStmt->close();
            sendJsonResponse([
                'error' => false,
                'status' => 'success',
                'message' => 'اطلاعات با موفقیت بروزرسانی شد.'
            ]);
        } else {
            error_log("⚠️ No rows affected (no changes)");
            $updateStmt->close();
            sendJsonResponse([
                'error' => false,
                'status' => 'no_change',
                'message' => 'تغییری در اطلاعات ایجاد نشد.'
            ]);
        }
    } else {
        error_log("❌ Execute failed: " . $updateStmt->error);
        throw new Exception("خطا در اجرای پرس و جوی بروزرسانی: " . $updateStmt->error);
    }

} catch (InvalidArgumentException $e) {
    error_log("❌ Validation Error in updateCargoInfo: " . $e->getMessage());
    error_log("📍 Stack trace: " . $e->getTraceAsString());
    sendJsonResponse([
        'error' => true,
        'message' => $e->getMessage()
    ], 400);
} catch (Exception $e) {
    error_log("💥 Error in updateCargoInfo: " . $e->getMessage());
    error_log("📁 File: " . $e->getFile());
    error_log("📍 Line: " . $e->getLine());
    error_log("🔍 Stack trace: " . $e->getTraceAsString());
    sendJsonResponse([
        'error' => true,
        'message' => 'خطای داخلی سرور رخ داده است.',
        'debug' => [
            'error' => $e->getMessage(),
            'file' => basename($e->getFile()),
            'line' => $e->getLine()
        ]
    ], 500);
} finally {
    if (isset($updateStmt)) {
        $updateStmt->close();
    }
    if (isset($conn)) {
        $conn->close();
    }
}
?>
