<?php
// PHP/check_scale_receipt.php

declare(strict_types=1);

require_once __DIR__ . '/src/bootstrap.php';

use App\Core\Database;
use App\Core\Request;
use App\Core\Response;
use App\Core\Logger;

try {
    $request = new Request();
    $scaleReceiptNumber = $request->get('scaleReceiptNumber');

    if (!$scaleReceiptNumber) {
        Response::sendJson(['error' => 'شماره قبض باسکول الزامی است.'], 400);
    }

    $scaleReceiptNumber = htmlspecialchars(trim((string)$scaleReceiptNumber), ENT_QUOTES, 'UTF-8');

    // بررسی معتبر بودن قبض باسکول (عددی و ۸ رقمی بودن)
    if (!ctype_digit($scaleReceiptNumber) || strlen($scaleReceiptNumber) !== 8) {
        Response::sendJson(['error' => 'شماره قبض باسکول معتبر نیست. لطفاً دوباره اسکن کنید.'], 400);
    }

    $firstTwoDigits = substr($scaleReceiptNumber, 0, 2);
    if ($firstTwoDigits < '44' || $firstTwoDigits > '55') {
        Response::sendJson(['error' => 'شماره قبض باسکول معتبر نیست. لطفاً دوباره اسکن کنید.'], 400);
    }

    $db = Database::getInstance()->getMysqliConnection();
    
    // واکشی اطلاعات حواله، کوتاژ و وزن برای قبض باسکول تکراری با فیلتر کردن ردیف‌های نامعتبر و کثیف دیتابیس
    $stmt = $db->prepare("SELECT trackingNumber AS trackingNumber, netWeight AS netWeight, loadingQuotaNumber AS loadingQuotaNumber FROM CargoInfo WHERE scaleReceiptNumber = ? AND trackingNumber IS NOT NULL AND trackingNumber != '' ORDER BY id DESC LIMIT 1");
    if (!$stmt) {
        throw new Exception("خطا در آماده‌سازی دستور SQL");
    }

    $stmt->bind_param("s", $scaleReceiptNumber);
    if (!$stmt->execute()) {
        throw new Exception("خطا در اجرای دستور SQL");
    }

    $result = $stmt->get_result();
    if ($row = $result->fetch_assoc()) {
        Logger::getInstance()->info("Found scale receipt duplicate: " . json_encode($row, JSON_UNESCAPED_UNICODE));
        
        // تبدیل کلیدهای آرایه به حروف کوچک جهت انعطاف‌پذیری و مقاومت در برابر درایورهای مختلف MySQL
        $lowerRow = array_change_key_case($row, CASE_LOWER);
        
        $trackingNumberVal = $lowerRow['trackingnumber'] ?? '';
        $netWeightVal = $lowerRow['netweight'] ?? '';
        $loadingQuotaNumberVal = $lowerRow['loadingquotanumber'] ?? '';

        Response::sendJson([
            'exists' => true,
            'message' => 'شماره قبض باسکول تکراری است.',
            'trackingNumber' => (string)$trackingNumberVal,
            'netWeight' => (string)$netWeightVal,
            'loadingQuotaNumber' => (string)$loadingQuotaNumberVal
        ]);
    } else {
        Response::sendJson(['exists' => false, 'message' => 'شماره قبض باسکول معتبر است.']);
    }

} catch (Exception $e) {
    Logger::getInstance()->error("Error in check_scale_receipt.php: " . $e->getMessage());
    Response::sendJson(['error' => $e->getMessage()], 500);
}